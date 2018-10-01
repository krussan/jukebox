package se.qxx.jukebox.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.probe.FFmpegStream.CodecType;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.JukeboxThread;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.converter.MediaConverterResult.State;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.Logger;

public class MediaConverter extends JukeboxThread {

	private static MediaConverter _instance;
	private Thread converterThread;

	private MediaConverter() {
		super("MediaConverter", 3000, LogType.CONVERTER);
	}

	public static MediaConverter get() {
		if (_instance == null)
			_instance = new MediaConverter();

		return _instance;
	}

	@Override
	protected void initialize() {
		Log.Info("Starting up converter thread [...]", LogType.CONVERTER);
		Log.Debug("Cleaning up converter queue ..", LogType.CONVERTER);
		DB.cleanupConverterQueue();

	}

	@Override
	protected void execute() {
		Log.Debug("Retrieving list to process", LogType.CONVERTER);
		List<Media> _listProcessing = DB.getConverterQueue();

		for (Media md : _listProcessing) {
			try {
				if (md != null) {
					FFmpegProbeResult probeResult = getProbeResult(md);
					ConversionProbeResult conversionCheckResult = checkConversion(md, probeResult);
					
					if (conversionCheckResult.getNeedsConversion()) {
						saveConvertedMedia(md, MediaConverterState.Converting);
						MediaConverterResult result = triggerConverter(md, probeResult, conversionCheckResult);
						
						if (result.getState() == MediaConverterResult.State.Completed) {
							saveConvertedMedia(md, result.getConvertedFilename());
						}
						else if (result.getState() == MediaConverterResult.State.Aborted) {
							saveConvertedMedia(md, MediaConverterState.Queued);
						}
						else {
							saveConvertedMedia(md, MediaConverterState.Failed);
						}
					}
					else {
						saveConvertedMedia(md, MediaConverterState.NotNeeded);
					}
				}
			} catch (Exception e) {
				Log.Error("Error when converting media", LogType.CONVERTER, e);
				saveConvertedMedia(md, MediaConverterState.Failed);
			}
			
			if (!this.isRunning())
				break;
		}
	}

	private FFmpegProbeResult getProbeResult(Media md) throws IOException {
		FFprobe ffprobe = new FFprobe();
		return ffprobe.probe(Util.getFullFilePath(md));
	}

	private ConversionProbeResult checkConversion(Media md, FFmpegProbeResult probeResult) {
		//TODO: Should be configurable
		//TODO: Use ffprobe to check container
		//for now all not mp4 extension
		//list of accepted video codecs
		List<String> acceptedVideoCodecs = Arrays.asList(new String[] {"h264"});
		List<String> acceptedAudioCodecs = Arrays.asList(new String[] {"aac", "mp3", "vorbis", "lcpm", "wav", "flac", "opus"});

		String audioCodec = "aac";
		String videoCodec = "h264";
		
		for (FFmpegStream stream : probeResult.getStreams()) {
			if (stream.codec_type == CodecType.AUDIO) {
				Log.Info(String.format("Audio codec :: %s", stream.codec_name), LogType.CONVERTER);
				if (findCodec(acceptedAudioCodecs, stream.codec_name))
					audioCodec = "copy";
			}
			else if (stream.codec_type == CodecType.VIDEO){
				Log.Info(String.format("Video codec :: %s", stream.codec_name), LogType.CONVERTER);
				if (findCodec(acceptedVideoCodecs, stream.codec_name))
					audioCodec = "copy";
			}
		}
		boolean needsConversion = 
				FilenameUtils.getExtension(md.getFilename()).equalsIgnoreCase("mp4") ||
				!StringUtils.equals(audioCodec, "copy") ||
				!StringUtils.equals(videoCodec, "copy");
		
		return new ConversionProbeResult(needsConversion, videoCodec, audioCodec);
	}

	private boolean findCodec(List<String> listCodecs, String codec_name) {
		for (String c : listCodecs) {
			if (StringUtils.equalsIgnoreCase(c, codec_name))
				return true;
		}
		
		return false;
	}

	private MediaConverterResult triggerConverter(Media md, FFmpegProbeResult probeResult, ConversionProbeResult checkResult) throws IOException {
		FFmpeg ffmpeg = new FFmpeg();
		FFprobe ffprobe = new FFprobe();

		String filename = Util.getFullFilePath(md);
		String newFilename = String.format("%s_[tazmo].mp4", FilenameUtils.getBaseName(md.getFilename()));
		String newFilepath = String.format("%s/%s", md.getFilepath(), newFilename);

		try {
			Log.Info(String.format("Probing :: %s", filename), LogType.CONVERTER);
		
			FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(filename)
				.addOutput(newFilepath)
				.setFormat("mp4")
				.setVideoCodec(checkResult.getTargetVideoCodec())
				.setAudioCodec(checkResult.getTargetAudioCodec())

				.done();

			Log.Debug(String.format("Starting converter on :: %s", filename), LogType.CONVERTER);
			Log.Debug(String.format(" --> new file :: %s", newFilepath), LogType.CONVERTER);
	
			FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
			FFmpegJob job = executor.createJob(builder, new ProgressListener() {
				// Using the FFmpegProbeResult determine the duration of the input
				final double duration_ns = probeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1);
				
				@Override
				public void progress(Progress progress) {
					double percentage = progress.out_time_ns / duration_ns;
					
					String logMessage =
						String.format(
							"[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
							percentage * 100,
							progress.status,
							progress.frame,
							FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
							progress.fps.doubleValue(),
							progress.speed);
					
					Log.Info(logMessage, LogType.CONVERTER);
				}
				
			});
		
		
			converterThread = new Thread(job);
			converterThread.start();
		
			converterThread.join();
			

			FFmpegJob.State state = job.getState();
			
			switch (state) {
			case FINISHED:
				Log.Debug(String.format("Conversion completed on :: %s", filename), LogType.CONVERTER);
				return new MediaConverterResult(filename, newFilepath, MediaConverterResult.State.Completed);
			case FAILED:
				Log.Debug(String.format("Conversion FAILED on :: %s", filename), LogType.CONVERTER);
				break;
			case RUNNING:
				Log.Debug(String.format("Conversion STILL RUNNING on :: %s", filename), LogType.CONVERTER);
				break;
			case WAITING:
				Log.Debug(String.format("Conversion WAITING on :: %s", filename), LogType.CONVERTER);
				break;
			}
			
			return new MediaConverterResult(filename, newFilename, MediaConverterResult.State.Error);
			
		}
		catch (InterruptedException iex) {
			Log.Info("Interrupt triggered",  LogType.CONVERTER);
			return new MediaConverterResult(filename, StringUtils.EMPTY, MediaConverterResult.State.Aborted);
		}
		catch (Exception e) {
			Log.Error("Error when converting file",  LogType.CONVERTER, e);
			return new MediaConverterResult(filename, StringUtils.EMPTY, MediaConverterResult.State.Error);
		}
	}

	private void saveConvertedMedia(Media md, String newFilename) {
		DB.saveConversion(md.getID(), newFilename, MediaConverterState.Completed_VALUE);
	}

	private void saveConvertedMedia(Media md, MediaConverterState result) {
		DB.saveConversion(md.getID(), result.getNumber());
	}

	@Override
	public void end() {
		if (converterThread != null) {
			Log.Debug("Stopping converter thread", LogType.CONVERTER);
			converterThread.interrupt();
		}

		super.end();
	}
	
	@Override
	public int getJukeboxPriority() {
		return 3;
	}


}
