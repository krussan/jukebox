package se.qxx.jukebox.watcher;

public class FileRepresentation {

	private String _path;
	private String _name;
	private long _lastModified;
	private long _fileSize;
	
	public void setName(String name) {
		this._name = name;
	}
	
	public String getName() {
		return this._name;
	}
	
	public String getPath() {
		return this._path;
	}
	
	public void setLastModified(long lastModified){
		this._lastModified = lastModified;
	}
	 
	public long getLastModified() {
		return this._lastModified;
	}
	
	public FileRepresentation(String path, String name, long l, long fileSize) {	
		this._path = path;
		this._name = name;
		this._lastModified = l;
		this._fileSize = fileSize;
	}

	public long getFileSize() {
		return _fileSize;
	}

	public void setFileSize(long _fileSize) {
		this._fileSize = _fileSize;
	}
}