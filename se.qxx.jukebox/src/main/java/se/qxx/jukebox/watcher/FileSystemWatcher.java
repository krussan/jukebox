package se.qxx.jukebox.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import se.qxx.jukebox.tools.Util;

public class FileSystemWatcher implements Runnable {

	private Boolean isRunning = false;

	public Boolean getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}

	private static Comparator<FileRepresentation> comparator = new Comparator<FileRepresentation>() {

		public int compare(FileRepresentation fr0, FileRepresentation fr1) {
			return fr0.getName().compareTo(fr1.getName());
		}

	};

	protected File directory;

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	protected List<INotifyClient> clients = new ArrayList<INotifyClient>();

	public List<INotifyClient> getClients() {
		return clients;
	}

	private TreeSet<FileRepresentation> files;

	public TreeSet<FileRepresentation> getFiles() {
		return files;
	}

	public void setFiles(TreeSet<FileRepresentation> files) {
		this.files = files;
	}

	private long sleepTime = 10000;

	private ExtensionFileFilter filter;

	private boolean watchCreated = false;

	public boolean isWatchCreated() {
		return watchCreated;
	}

	public void setWatchCreated(boolean watchCreated) {
		this.watchCreated = watchCreated;
	}

	private boolean watchModified = false;

	public boolean isWatchModified() {
		return watchModified;
	}

	public void setWatchModified(boolean watchModified) {
		this.watchModified = watchModified;
	}

	private boolean recurse = false;

	public FileSystemWatcher(String directoryName, ExtensionFileFilter filter, boolean watchCreated,
			boolean watchModified, boolean recurse) {
		File directoryToWatch = new File(directoryName);
		/*
		 * if (!directoryToWatch.isDirectory()) { throw new
		 * RuntimeException("It needs to be a directory"); }
		 */

		this.setDirectory(directoryToWatch);
		this.setFilter(filter);

		this.setFiles(getCurrentRepresentation());

		this.setWatchCreated(watchCreated);
		this.setWatchModified(watchModified);
		this.setRecurse(recurse);
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public long getSleepTime() {
		return this.sleepTime;
	}

	public boolean isRecurse() {
		return recurse;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	public void registerClient(INotifyClient client) {
		clients.add(client);
	}

	public java.util.TreeSet<FileRepresentation> getCurrentRepresentation() {
		TreeSet<FileRepresentation> rep = new TreeSet<FileRepresentation>(comparator);

		List<File> list = Util.getFileListing(this.getDirectory(), this.getFilter(), this.isRecurse());
		// List<File> list = Util.getFileListingWorkAround(directory, _filter);

		for (File f : list) {
			rep.add(new FileRepresentation(f.getParent(), f.getName(), f.lastModified(), f.length()));
		}

		return rep;
	}

	public void notifyCreated(FileRepresentation f) {
		for (INotifyClient client : this.getClients()) {
			Thread t = new Thread(new FileChangedThread(client, f, true, false));
			t.start();
		}
	}

	public void notifyModified(FileRepresentation f) {
		for (INotifyClient client : clients) {
			Thread t = new Thread(new FileChangedThread(client, f, false, true));
			t.start();
		}
	}

	public void run() {
		this.setIsRunning(true);
		TreeSet<FileRepresentation> currentRepresentation = getCurrentRepresentation();

		for (FileRepresentation f : currentRepresentation) {
			notifyCreated(f);
		}

		while (this.getIsRunning()) {
			currentRepresentation = getCurrentRepresentation();

			for (FileRepresentation f : currentRepresentation) {
				if (!this.getFiles().contains(f) && this.isWatchCreated())
					notifyCreated(f);

				for (FileRepresentation o : files) {
					if (o.getName() == f.getName()) {
						if (o.getLastModified() != f.getLastModified() && this.isWatchModified()) {
							notifyModified(f);
						}
					}
				}

			}

			files = currentRepresentation;

			try {
				Thread.sleep(getSleepTime());
			} catch (InterruptedException e) {
				return;
			}
		}

	}

	public void startListening() {
		Thread t = new Thread(this);
		t.start();
	}

	public void stopListening() {
		this.setIsRunning(false);
	}

	public ExtensionFileFilter getFilter() {
		return filter;
	}

	public void setFilter(ExtensionFileFilter filter) {
		this.filter = filter;
	}

}
