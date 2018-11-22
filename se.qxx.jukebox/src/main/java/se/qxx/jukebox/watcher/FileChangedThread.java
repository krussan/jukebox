package se.qxx.jukebox.watcher;

public class FileChangedThread implements Runnable {

	private IFileCreatedHandler client;
	private FileRepresentation file;
	private boolean isCreated = false;
	private boolean isModified = false;

	public FileChangedThread(IFileCreatedHandler client, FileRepresentation f, boolean isCreated, boolean isModified) {
		this.client = client;
		this.file = f;
		this.isCreated = isCreated;
		this.isModified = isModified;
	}

	@Override
	public void run() {
		if (this.isCreated)
			client.fileCreated(this.file);

		if (this.isModified)
			client.fileModified(this.file);
	}

}