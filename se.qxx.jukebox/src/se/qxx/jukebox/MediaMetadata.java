package se.qxx.jukebox;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.filebot.mediainfo.MediaInfo;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

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
	
	
	public static Movie addMediaMetadata(Movie m) {
		List<Media> mediaList = new ArrayList<Media>();
		
		for (Media md : m.getMediaList()) {
			String fullFilePath = String.format("%s/%s", md.getFilepath(), md.getFilename());
			Log.Debug(String.format("Finding media meta data for file %s", md.getFilename()), LogType.FIND);

			try {
				MediaMetadata mm = MediaMetadata.getMediaMetadata(fullFilePath);
				Media newMedia = Media.newBuilder(md)
						.setMetaDuration(mm.getDurationSeconds())
						.setMetaFramerate(mm.getFramerate())
						.build();
				
				mediaList.add(newMedia);
				
			} catch (Exception e) {
	    		Log.Error(String.format("Error when retreiving media info from file %s", fullFilePath), LogType.FIND, e);
			}

		}

		return Movie.newBuilder(m)
				.clearMedia()
				.addAllMedia(mediaList)
				.build();
	}
	
}
