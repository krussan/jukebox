package se.qxx.jukebox.tools;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.filebot.mediainfo.MediaInfo;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;

public class MediaMetadata {

	private String framerate = StringUtils.EMPTY;
	private long duration = 0;
	private List<String> subtitles = new ArrayList<String>();
	
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
	
	public List<String> getSubtitles() {
		return this.subtitles;
	}
	
	private void addSubtitle(String language) {
		this.subtitles.add(language);
	}
	
	public void clearSubtitles() {
		this.subtitles.clear();
	}
	
	private MediaMetadata() {
	}
	
	private MediaMetadata(long duration, String frameRate) {
		this.setDuration(duration);
		this.setFramerate(frameRate);
	}
	
	public static MediaMetadata getMediaMetadata(Media md) {
		String fullFilePath = Util.getFullFilePath(md);
		Log.Debug(String.format("Finding media meta data for file %s", md.getFilename()), LogType.FIND);

		try {
			MediaMetadata mm = MediaMetadata.getMediaMetadata(fullFilePath);
			return mm;
							
		} catch (Exception e) {
    		Log.Error(String.format("Error when retreiving media info from file %s", fullFilePath), LogType.FIND, e);
		}

		return null;		
	}
	
	public static MediaMetadata getMediaMetadata(String fullFilePath) throws FileNotFoundException {
	    MediaInfo MI = new MediaInfo();
	    
	    MediaMetadata md = new MediaMetadata();
	    
	    if (MI.Open(fullFilePath)>0) {
	    	try {
			    String duration = MI.Get(MediaInfo.StreamKind.General, 0, "Duration");
			    if (StringUtils.isNumeric(duration))
			    	md.setDuration(Long.parseLong(duration));
			    
			    md.setFramerate(MI.Get(MediaInfo.StreamKind.Video, 0, "FrameRate"));
			    
			    int numberOfTextStreams = MI.Count_Get(MediaInfo.StreamKind.Text);
			    for (int i=0; i< numberOfTextStreams; i++) {
			    	md.addSubtitle(MI.Get(MediaInfo.StreamKind.Text, i, "Language"));
			    }
	    	}
	    	catch (Exception e) {
	    		Log.Error(String.format("Error when retreiving media info from file %s", fullFilePath), LogType.FIND, e);
	    	}
	    }
	    else {
	    	throw new FileNotFoundException();
	    }
	    
	    return md;
	}
	
	
	public static Media addMediaMetadata(Media md) {
		MediaMetadata mm = MediaMetadata.getMediaMetadata(md);
		if (mm == null)
			return md;
		
		return Media.newBuilder(md)
				.setMetaDuration(mm.getDurationSeconds())
				.setMetaFramerate(mm.getFramerate())
				.build();
	}
}
