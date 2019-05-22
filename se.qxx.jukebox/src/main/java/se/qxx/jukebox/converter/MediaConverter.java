package se.qxx.jukebox.converter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
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
import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.factories.LoggerFactory;
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
	private MediaConverter(IExecutor executor, IDatabase database, ISettings settings, LoggerFactory loggerFactory) {
		super("MediaConverter", 3000, loggerFactory.create(LogType.CONVERTER), executor);
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
		this.getLog().Info("Starting up converter thread [...]");
		this.getLog().Info("Cleaning up converter queue ..");
		this.getDatabase().cleanupConverterQueue();

	}

	@Override
	protected void execute() {
		this.getLog().Info("Retrieving list to process");
		List<Media> _listProcessing = this.getDatabase().getConverterQueue();

		for (Media md : _listProcessing) {
			try {
				if (md != null) {
					String fullFilePath = Util.getFullFilePath(md);
					File f = new File(fullFilePath);
					
					if (f.exists()) {
						FFmpegProbeResult probeResult = getProbeResult(fullFilePath);
						ConversionProbeResult conversionCheckResult = checkConversion(md, probeResult);
						
						if (conversionCheckResult.getNeedsConversion()) {
							saveConvertedMedia(md, MediaConverterState.Converting);
							
							MediaConverterResult result = triggerConverter(md, probeResult, conversionCheckResult);
							
							this.getLog().Info(String.format("Conversion done on %s. Status :: %s !", md.getFilepath()));

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
					else {
						this.getLog().Info(String.format("File does not exist :: %s", fullFilePath));
						saveConvertedMedia(md, MediaConverterState.Failed);
					}
				}
			} catch (Exception e) {
				this.getLog().Error("Error when converting media", e);
				saveConvertedMedia(md, MediaConverterState.Failed);
			}
			
			if (!this.isRunning())
				break;
		}
	}

	private FFmpegProbeResult getProbeResult(String fullFilePath) throws IOException {
		FFprobe ffprobe = new FFprobe();
		this.getLog().Info(String.format("Probing :: %s", fullFilePath));
		return ffprobe.probe(fullFilePath);
	}

	private ConversionProbeResult checkConversion(Media md, FFmpegProbeResult probeResult) {
		List<Codec> acceptedVideoCodecs = this.getSettings().getSettings().getConverter().getAcceptedVideoCodecs().getCodec();
		List<Codec> acceptedAudioCodecs = this.getSettings().getSettings().getConverter().getAcceptedAudioCodecs().getCodec();

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
		
		this.getLog().Info(String.format("Target video codec :: %s", videoCodec));
		this.getLog().Info(String.format("Target audio codec :: %s", audioCodec));
		boolean needsConversion = 
				!FilenameUtils.getExtension(md.getFilename()).equalsIgnoreCase("mp4") ||
				!StringUtils.equals(audioCodec, "copy") ||
				!StringUtils.equals(videoCodec, "copy") ||
				md.getConverterState() == MediaConverterState.Forced;
		
		return new ConversionProbeResult(needsConversion, videoCodec, audioCodec);
	}

	private void logCodec(FFmpegStream stream) {
		this.getLog().Info(String.format("%s codec name       :: %s", stream.codec_type, stream.codec_name));
		this.getLog().Info(String.format("%s codec tag        :: %s", stream.codec_type, stream.codec_tag));
		this.getLog().Info(String.format("%s codec long name  :: %s", stream.codec_type, stream.codec_long_name));
		this.getLog().Info(String.format("%s codec tag string :: %s", stream.codec_type, stream.codec_tag_string));
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

		if (checkFileExists(newFilepath) && md.getConverterState() != MediaConverterState.Forced && md.getConverterState() != MediaConverterState.Failed) {
			this.getLog().Info(String.format("Conversion already exist on :: %s", filename));
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

			this.getLog().Info(String.format("Starting converter on :: %s", filename));
			this.getLog().Info(String.format(" --> new file :: %s", newFilepath));
	
			final double duration_ns = getDurationNs(probeResult);			
			FFmpegJob job = runConversion(ffmpeg, ffprobe, builder, duration_ns);

			// check 
			return checkConverterResult(filePath, filename, newFilename, job).cleanupOnError();
			
			
		}
		catch (Exception e) {
			this.getLog().Error("Error when converting file", e);
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
				
				getLog().Debug(logMessage);
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
			this.getLog().Info(String.format("Conversion completed on :: %s", filename));
			return new MediaConverterResult(filepath, filename, newFilename, MediaConverterResult.State.Completed);
		case FAILED:
			this.getLog().Info(String.format("Conversion FAILED on :: %s", filename));
			break;
		case RUNNING:
			this.getLog().Info(String.format("Conversion STILL RUNNING on :: %s", filename));
			break;
		case WAITING:
			this.getLog().Info(String.format("Conversion WAITING on :: %s", filename));
			break;
		}
		
		return new MediaConverterResult(filepath, filename, newFilename, MediaConverterResult.State.Error);
	}

	private boolean checkFileExists(String newFilepath) {
		File f = new File(newFilepath);
		return f.exists();
	}

	private void saveConvertedMedia(Media md, String newFilename) {
		try {
			this.getDatabase().saveConversion(md.getID(), newFilename, MediaConverterState.Completed_VALUE);
		}
		catch (Exception e) {
			this.getLog().Error("Error when saving converted media!!", e);
		}
	}

	private void saveConvertedMedia(Media md, MediaConverterState result) {
		try {
			this.getDatabase().saveConversion(md.getID(), result.getNumber());
		}
		catch (Exception e) {
			this.getLog().Error("Error when saving converted media!!", e);
		}
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
