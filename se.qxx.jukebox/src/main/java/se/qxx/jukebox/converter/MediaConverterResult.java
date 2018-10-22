package se.qxx.jukebox.converter;

public class MediaConverterResult {

	public enum State {
		Completed,
		Error,
		Aborted
	}
	
	private String filename;
	private String convertedFilename;
	private State state;
	
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
	
	public MediaConverterResult(String filename, String convertedFilename, State resultState) {
		this.setFilename(filename);
		this.setConvertedFilename(convertedFilename);
		this.setState(resultState);
	}
	
}
