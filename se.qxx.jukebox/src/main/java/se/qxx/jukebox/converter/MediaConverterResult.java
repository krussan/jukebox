package se.qxx.jukebox.converter;

import java.io.File;

import org.apache.commons.io.IOUtils;

import se.qxx.jukebox.tools.Util;

public class MediaConverterResult {

	public enum State {
		Completed,
		Error,
		Aborted
	}

	private ConvertedFile convertedFile;
	private State state;

	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	public ConvertedFile getConvertedFile() {
		return convertedFile;
	}
	public void setConvertedFile(ConvertedFile convertedFile) {
		this.convertedFile = convertedFile;
	}
	
	public MediaConverterResult(ConvertedFile convertedFile, State resultState) {
		this.setConvertedFile(convertedFile);
		this.setState(resultState);
	}
	
	public MediaConverterResult cleanupOnError() {
		if (this.getState() == MediaConverterResult.State.Aborted || 
				this.getState() == MediaConverterResult.State.Error) {
			deleteFile(this.getConvertedFile().getConvertedFullFilepath());
		}
		
		return this;
	}
	
	private void deleteFile(String newFilepath) {
		File f = new File(newFilepath);
		if (f.exists())
			f.delete();
	}

	
}
