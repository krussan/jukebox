package se.qxx.jukebox.converter;

import se.qxx.jukebox.watcher.FileRepresentation;

public class FileRepresentationState extends FileRepresentation {

	private FileChangedState state = FileChangedState.INIT;
	
	public FileRepresentationState(String path, String name, long l, long fileSize) {
		super(path, name, l, fileSize);
	}

	public FileRepresentationState(FileRepresentation f) {
		super(f.getPath(), f.getName(), f.getLastModified(), f.getFileSize());
	}

	public FileChangedState getState() {
		return state;
	}


	public void setState(FileChangedState state) {
		this.state = state;
	}

	
}
