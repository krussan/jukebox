package se.qxx.jukebox.converter;

import java.io.File;

import se.qxx.jukebox.tools.Util;

public class MediaConverterResult {

	public enum State {
		Completed,
		Error,
		Aborted
	}
	
	private String filepath;
	private String filename;
	private String convertedFilename;
	private State state;

	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getConvertedFilename() {
		return convertedFilename;
	}
	public void setConvertedFilename(String convertedFilename) {
		this.convertedFilename = convertedFilename;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	public MediaConverterResult(String filePath, String filename, String convertedFilename, State resultState) {
		this.setFilepath(filePath);
		this.setFilename(filename);
		this.setConvertedFilename(convertedFilename);
		this.setState(resultState);
	}
	
	public MediaConverterResult cleanupOnError() {
		if (this.getState() == MediaConverterResult.State.Aborted || 
				this.getState() == MediaConverterResult.State.Error) {
			deleteFile(Util.getFullFilePath(this.getFilepath(), this.getFilename()));
		}
		
		return this;
	}
	
	private void deleteFile(String newFilepath) {
		File f = new File(newFilepath);
		if (f.exists())
			f.delete();
	}

	
}
