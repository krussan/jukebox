package se.qxx.jukebox.watcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

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

	public FileRepresentation(File f) {
		this._path = FilenameUtils.getFullPathNoEndSeparator(f.getAbsolutePath());
		this._name = f.getName();
		this._lastModified = f.lastModified();
		this._fileSize = f.length();
	}

	public FileRepresentation(String path, String name, long lastModified, long fileSize) {
		this._path = path;
		this._name = name;
		this._lastModified = lastModified;
		this._fileSize = fileSize;
	}

	public long getFileSize() {
		return _fileSize;
	}

	public void setFileSize(long _fileSize) {
		this._fileSize = _fileSize;
	}
	
	public String getFullPath() {
		return String.format("%s/%s", this.getPath(), this.getName());
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FileRepresentation))
			return false;
		
		return StringUtils.equalsIgnoreCase(
				((FileRepresentation)o).getFullPath(), 
				this.getFullPath());
	}
}