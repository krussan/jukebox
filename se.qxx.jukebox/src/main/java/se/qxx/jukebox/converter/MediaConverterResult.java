package se.qxx.jukebox.converter;

import java.io.File;

import org.apache.commons.io.IOUtils;

import se.qxx.jukebox.interfaces.IConvertedFile;
import se.qxx.jukebox.tools.Util;

public class MediaConverterResult {

	public enum State {
		Completed,
		Error,
		Aborted
	}

	private IConvertedFile convertedFile;
	private State state;

	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	public IConvertedFile getConvertedFile() {
		return convertedFile;
	}
	public void setConvertedFile(IConvertedFile convertedFile) {
		this.convertedFile = convertedFile;
	}
	
	public MediaConverterResult(IConvertedFile convertedFile, State resultState) {
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
