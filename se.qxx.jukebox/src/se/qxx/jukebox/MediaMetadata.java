package se.qxx.jukebox;

import java.io.FileNotFoundException;

import net.sourceforge.filebot.mediainfo.MediaInfo;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;

public class MediaMetadata {

	private String framerate;
	private long duration;
	
	public String getFramerate() {
		return framerate;
	}
	private void setFramerate(String framerate) {
		this.framerate = framerate;
	}
	public long getDuration() {
		return duration;
	}
	public int getDurationMinutes(){
		return Math.round(this.getDuration() / 1000 / 60);
	}
	public int getDurationSeconds(){
		return Math.round(this.getDuration() / 1000);
	}	
	private void setDuration(long duration) {
		this.duration = duration;
	}
	
	private MediaMetadata(long duration, String frameRate) {
		this.setDuration(duration);
		this.setFramerate(frameRate);
	}
	
	public static MediaMetadata getMediaMetadata(String fullFilePath) throws FileNotFoundException {
	    MediaInfo MI = new MediaInfo();
	    long durationMs = 0;
	    String frameRate = StringUtils.EMPTY;
	    
	    if (MI.Open(fullFilePath)>0) {
	    	try {
			    String duration = MI.Get(MediaInfo.StreamKind.General, 0, "Duration");
			    if (StringUtils.isNumeric(duration))
			    	durationMs = Long.parseLong(duration);
			    frameRate = MI.Get(MediaInfo.StreamKind.Video, 0, "FrameRate");		    
	    	}
	    	catch (Exception e) {
	    		Log.Error(String.format("Error when retreiving media info from file %s", fullFilePath), LogType.FIND, e);
	    	}
	    }
	    else {
	    	throw new FileNotFoundException();
	    }
	    
	    return new MediaMetadata(durationMs, frameRate);
	}
	
	
	public static Media addMediaMetadata(Media md) {
		String fullFilePath = String.format("%s/%s", md.getFilepath(), md.getFilename());
		Log.Debug(String.format("Finding media meta data for file %s", md.getFilename()), LogType.FIND);

		try {
			MediaMetadata mm = MediaMetadata.getMediaMetadata(fullFilePath);
			return Media.newBuilder(md)
					.setMetaDuration(mm.getDurationSeconds())
					.setMetaFramerate(mm.getFramerate())
					.build();
							
		} catch (Exception e) {
    		Log.Error(String.format("Error when retreiving media info from file %s", fullFilePath), LogType.FIND, e);
		}

		return md;
	}
}
