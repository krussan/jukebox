package se.qxx.jukebox;

public class FileRepresentation {

	private String _name;
	private long _lastModified;
	
	public void setName(String name) {
		this._name = name;
	}
	
	public String getName() {
		return this._name;
	}
	
	public void setLastModified(long lastModified){
		this._lastModified = lastModified;
	}
	
	public long getLastModified() {
		return this._lastModified;
	}
	
	public FileRepresentation(String name, long l) {	
		this._name = name;
		this._lastModified = l;
	}
}