package se.qxx.jukebox.converter;

import java.io.IOException;
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
					if (needsConversion(md)) {
						saveConvertedMedia(md, MediaConverterState.Converting);
						MediaConverterResult result = triggerConverter(md);
						
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

	private boolean needsConversion(Media md) {
		//TODO: Should be configurable
		//TODO: Use ffprobe to check container
		//for now all not mp4 extension
		return !FilenameUtils.getExtension(md.getFilename()).equalsIgnoreCase("mp4");
	}

	private MediaConverterResult triggerConverter(Media md) throws IOException {
		FFmpeg ffmpeg = new FFmpeg();
		FFprobe ffprobe = new FFprobe();

		String filename = Util.getFullFilePath(md);
		String newFilename = String.format("%s_[tazmo].mp4", FilenameUtils.getBaseName(md.getFilename()));

		String newFilepath = String.format("%s/%s", md.getFilepath(), newFilename);

		FFmpegProbeResult in = ffprobe.probe(filename);
		
		FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(filename)
				.addOutput(newFilepath)
				.setFormat("mp4")
				.setVideoCodec("copy")
				.setAudioCodec("copy")

				.done();

		Log.Debug(String.format("Starting converter on :: %s", filename), LogType.CONVERTER);
		Log.Debug(String.format(" --> new file :: %s", newFilepath), LogType.CONVERTER);

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
		FFmpegJob job = executor.createJob(builder, new ProgressListener() {
			// Using the FFmpegProbeResult determine the duration of the input
			final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);
			
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
		
		try {
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
			
			return new MediaConverterResult(filename, newFilepath, MediaConverterResult.State.Error);
			
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
		Media o = Media.newBuilder(md).setConvertedFileName(newFilename)
				.setConverterState(MediaConverterState.Completed).build();

		DB.save(o);
	}

	private void saveConvertedMedia(Media md, MediaConverterState result) {
		Media o = Media.newBuilder(md).setConverterState(result).build();

		DB.save(o);

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
