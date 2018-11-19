package se.qxx.jukebox.converter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IMediaConverter;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.CodecsType.Codec;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;

@Singleton
public class MediaConverter extends JukeboxThread implements IMediaConverter {

	private IDatabase database;
	private ISettings settings;
	
	@Inject
	private MediaConverter(IExecutor executor, IDatabase database, ISettings settings) {
		super("MediaConverter", 3000, LogType.CONVERTER, executor);
		this.setDatabase(database);
		this.setSettings(settings);
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	@Override
	protected void initialize() {
		Log.Info("Starting up converter thread [...]", LogType.CONVERTER);
		Log.Debug("Cleaning up converter queue ..", LogType.CONVERTER);
		this.getDatabase().cleanupConverterQueue();

	}

	@Override
	protected void execute() {
		Log.Debug("Retrieving list to process", LogType.CONVERTER);
		List<Media> _listProcessing = this.getDatabase().getConverterQueue();

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
		Log.Info(String.format("Probing :: %s", md.getFilename()), LogType.CONVERTER);
		return ffprobe.probe(Util.getFullFilePath(md));
	}

	private ConversionProbeResult checkConversion(Media md, FFmpegProbeResult probeResult) {
		//TODO: Should be configurable
		//TODO: Use ffprobe to check container
		//for now all not mp4 extension
		//list of accepted video codecs
		
		List<Codec> acceptedVideoCodecs = this.getSettings().getSettings().getConverter().getAcceptedVideoCodecs().getCodec();
		List<Codec> acceptedAudioCodecs = this.getSettings().getSettings().getConverter().getAcceptedAudioCodecs().getCodec();
		//List<String> acceptedVideoCodecs = Settings.get().getConverter().getAcceptedAudioCodecs().getCodec();
		//List<String> acceptedAudioCodecs = Arrays.asList(new String[] {"aac", "mp3", "vorbis", "lcpm", "wav", "flac", "opus"});

		String audioCodec = "aac";
		String videoCodec = "h264";
		
		for (FFmpegStream stream : probeResult.getStreams()) {
			if (stream.codec_type == CodecType.AUDIO) {
				logCodec(stream);

				if (findCodec(acceptedAudioCodecs, stream.codec_name))
					audioCodec = "copy";
			}
			else if (stream.codec_type == CodecType.VIDEO){
				logCodec(stream);
				if (findCodec(acceptedVideoCodecs, stream.codec_name))
					videoCodec = "copy";
			}
		}
		
		Log.Info(String.format("Target video codec :: %s", videoCodec), LogType.CONVERTER);
		Log.Info(String.format("Target audio codec :: %s", audioCodec), LogType.CONVERTER);
		boolean needsConversion = 
				FilenameUtils.getExtension(md.getFilename()).equalsIgnoreCase("mp4") ||
				!StringUtils.equals(audioCodec, "copy") ||
				!StringUtils.equals(videoCodec, "copy");
		
		return new ConversionProbeResult(needsConversion, videoCodec, audioCodec);
	}

	private void logCodec(FFmpegStream stream) {
		Log.Info(String.format("%s codec name       :: %s", stream.codec_type, stream.codec_name), LogType.CONVERTER);
		Log.Info(String.format("%s codec tag        :: %s", stream.codec_type, stream.codec_tag), LogType.CONVERTER);
		Log.Info(String.format("%s codec long name  :: %s", stream.codec_type, stream.codec_long_name), LogType.CONVERTER);
		Log.Info(String.format("%s codec tag string :: %s", stream.codec_type, stream.codec_tag_string), LogType.CONVERTER);
	}

	private boolean findCodec(List<Codec> listCodecs, String codec_name) {
		for (Codec c : listCodecs) {
			if (StringUtils.equalsIgnoreCase(c.getTag(), codec_name))
				return true;
		}
		
		return false;
	}

	private MediaConverterResult triggerConverter(Media md, FFmpegProbeResult probeResult, ConversionProbeResult checkResult) throws IOException {
		FFmpeg ffmpeg = new FFmpeg();
		FFprobe ffprobe = new FFprobe();

		String filename = Util.getFullFilePath(md);
		String newFilename = String.format("%s_[tazmo].mp4", FilenameUtils.getBaseName(md.getFilename()));
		String filePath = md.getFilepath();
		String newFilepath = Util.getFullFilePath(filePath, newFilename);

		if (checkFileExists(newFilepath)) {
			Log.Debug(String.format("Conversion already exist on :: %s", filename), LogType.CONVERTER);
			return new MediaConverterResult(filePath, filename, newFilename, MediaConverterResult.State.Completed);
		}
			
		try {
		
			FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(filename)
				.addOutput(newFilepath)
				.setFormat("mp4")
				.setVideoCodec(checkResult.getTargetVideoCodec())
				.setAudioCodec(checkResult.getTargetAudioCodec())
				.done();

			Log.Debug(String.format("Starting converter on :: %s", filename), LogType.CONVERTER);
			Log.Debug(String.format(" --> new file :: %s", newFilepath), LogType.CONVERTER);
	
			final double duration_ns = getDurationNs(probeResult);			
			FFmpegJob job = runConversion(ffmpeg, ffprobe, builder, duration_ns);

			// check 
			return checkConverterResult(filePath, filename, newFilename, job).cleanupOnError();
			
			
		}
		catch (Exception e) {
			Log.Error("Error when converting file",  LogType.CONVERTER, e);
			return new MediaConverterResult(filePath, filename, StringUtils.EMPTY, MediaConverterResult.State.Error).cleanupOnError();
		}
	}


	private FFmpegJob runConversion(FFmpeg ffmpeg, FFprobe ffprobe, FFmpegBuilder builder, final double duration_ns) {
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);			
		FFmpegJob job = executor.createJob(builder, new ProgressListener() {
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

		// execute conversion
		job.run();
		return job;
	}
	
	private double getDurationNs(FFmpegProbeResult probeResult) {
		// Using the FFmpegProbeResult determine the duration of the input
		return probeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1);
	}

	private MediaConverterResult checkConverterResult(String filepath, String filename, String newFilename, FFmpegJob job) {
		FFmpegJob.State state = job.getState();
		
		switch (state) {
		case FINISHED:
			Log.Debug(String.format("Conversion completed on :: %s", filename), LogType.CONVERTER);
			return new MediaConverterResult(filepath, filename, newFilename, MediaConverterResult.State.Completed);
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
		
		return new MediaConverterResult(filepath, filename, newFilename, MediaConverterResult.State.Error);
	}

	private boolean checkFileExists(String newFilepath) {
		File f = new File(newFilepath);
		return f.exists();
	}

	private void saveConvertedMedia(Media md, String newFilename) {
		this.getDatabase().saveConversion(md.getID(), newFilename, MediaConverterState.Completed_VALUE);
	}

	private void saveConvertedMedia(Media md, MediaConverterState result) {
		this.getDatabase().saveConversion(md.getID(), result.getNumber());
	}

	@Override
	public void end() {
		super.end();
	}
	
	@Override
	public int getJukeboxPriority() {
		return 3;
	}

	@Override
	public Runnable getRunnable() {
		return this;
	}


}
