package se.qxx.jukebox.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MediaMetadata {

	private String framerate = StringUtils.EMPTY;
	private long duration = 0;
	private List<String> subtitles = new ArrayList<String>();

	public MediaMetadata() {
		
	}
	public MediaMetadata(long duration, String frameRate) {
		this.setDuration(duration);
		this.setFramerate(frameRate);
	}

	public String getFramerate() {
		return framerate;
	}
	public void setFramerate(String framerate) {
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
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public List<String> getSubtitles() {
		return this.subtitles;
	}
	
	public void addSubtitle(String language) {
		this.subtitles.add(language);
	}
	
	public void clearSubtitles() {
		this.subtitles.clear();
	}
	
	
}
