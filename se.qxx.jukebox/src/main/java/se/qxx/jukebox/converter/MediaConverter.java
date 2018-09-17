package se.qxx.jukebox.converter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.apache.commons.io.FilenameUtils;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.tools.Util;

public class MediaConverter implements Runnable {

	private static MediaConverter _instance;
	private boolean isRunning;
	private static final int THREAD_WAIT_SECONDS = 3000;
	private static boolean converting = false; 

	private MediaConverter() {
	}

	public static MediaConverter get() {
		if (_instance == null)
			_instance = new MediaConverter();

		return _instance;
	}

	@Override
	public void run() {
		this.setRunning(true);
		
		Util.waitForSettings();

		mainLoop();
	}

	protected void mainLoop() {
		Log.Debug("-- CONVERT -- Retrieving list to process", LogType.FIND);
		
		List<Media> _listProcessing =  DB.getConverterQueue();				
		
		while (isRunning()) {
			MediaConverterState result = MediaConverterState.Failed;
			
			try {
				for (Media md : _listProcessing) {
					try {
						if (md != null) {
							String newFilename = triggerConverter(md);							
							saveConvertedMedia(md, newFilename);							
						}
					} catch (Exception e) {
						Log.Error("-- CONVERT -- Error when converting media", LogType.FIND, e);
						saveConvertedMedia(md, MediaConverterState.Failed);
					}
				}
				
				// wait for trigger
				synchronized (_instance) {
					_instance.wait(THREAD_WAIT_SECONDS);
					_listProcessing = DB.getConverterQueue();					
				}
				
			} catch (InterruptedException e) {
				Log.Error("SUBS :: MediaConverter is going down ...", LogType.FIND, e);
			}			
		}
	}


	


	private String triggerConverter(Media md) throws IOException {
		FFmpeg ffmpeg = new FFmpeg();
		FFprobe ffprobe = new FFprobe();
		
		String filename = md.getFilename();
		String newFilename = String.format("%s_[tazmo].mp4",
				FilenameUtils.getBaseName(md.getFilename()));
		
		String newFilepath = String.format("%s/%s", md.getFilepath(), md.getFilename());
		
		Log.Debug(String.format("-- CONVERT -- Starting converter on :: %s", filename), LogType.FIND);
		
		FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(filename)
				.addOutput(newFilepath)
				.setFormat("mp4")
				.setVideoCodec("libx264")
				.setAudioCodec("aac")
				.done();

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
		FFmpegJob job = executor.createJob(builder);
		
		RunnableFuture<Void> task = new FutureTask<>(job, null);
		try {
			task.get();
		}
		catch (InterruptedException | ExecutionException e) {
			Log.Error("Error when converting file", LogType.FIND);
		}
		//String cmd = String.format("ffmpeg -i %s -copy %s", )
		//ffmpeg -i Atomic.Blonde.2017.720p.HC.HDRip.850MB.MkvCage.mkv -c:a copy -c:v copy Atomic.Blonde.2017.720p.HC.HDRip.850MB.MkvCage_jukebox.mp4
		
		Log.Debug(String.format("-- CONVERT -- Conversion completed on :: %s", filename), LogType.FIND);
		return newFilename;
	}

	private void saveConvertedMedia(Media md, String newFilename) {
		Media o = Media.newBuilder(md)
				.setConvertedFileName(newFilename)
				.setConverterState(MediaConverterState.Completed)
				.build();

		DB.save(o);
	}
	
	private void saveConvertedMedia(Media md, MediaConverterState result) {
		Media o = Media.newBuilder(md)
				.setConverterState(result)
				.build();

		DB.save(o);
		
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
