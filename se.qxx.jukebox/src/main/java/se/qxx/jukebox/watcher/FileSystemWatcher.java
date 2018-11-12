package se.qxx.jukebox.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import se.qxx.jukebox.JukeboxThread;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.tools.Util;

public class FileSystemWatcher extends JukeboxThread {

	TreeSet<FileRepresentation> currentRepresentation ;
	
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

	public FileSystemWatcher(String name, String directoryName, ExtensionFileFilter filter, boolean watchCreated,
			boolean watchModified, boolean recurse, int waitTime) {
		super(name, waitTime, LogType.FIND);
		
		File directoryToWatch = new File(directoryName);

		this.setDirectory(directoryToWatch);
		this.setFilter(filter);

		this.setFiles(getCurrentRepresentation());

		this.setWatchCreated(watchCreated);
		this.setWatchModified(watchModified);
		this.setRecurse(recurse);
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

		List<File> list = Util.getFileListing(
			this.getDirectory(), 
			this.getFilter(), 
			this.isRecurse());

		for (File f : list) {
			rep.add(
				new FileRepresentation(
					f.getParent(),
					f.getName(), 
					f.lastModified(), 
					f.length()));
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

	@Override
	public void initialize() {
		currentRepresentation = getCurrentRepresentation();

		for (FileRepresentation f : currentRepresentation) {
			notifyCreated(f);
			if (!this.isRunning())
				break;
		}		
	}
	
	@Override
	public void execute() {
		currentRepresentation = getCurrentRepresentation();

		for (FileRepresentation f : currentRepresentation) {
			if (!fileExistsInCurrentRepresentation(f)) {				
				notifyCreated(f);
			}

			if (!this.isRunning())
				break;
		}

		files = currentRepresentation;		
	}

	private boolean fileExistsInCurrentRepresentation(FileRepresentation f) {
		for (FileRepresentation o : files) {
			if (o.getName().equals(f.getName())) {
				if (o.getLastModified() != f.getLastModified() && this.isWatchModified()) {
					notifyModified(f);
				}
				
				return true;
			}
		}
		
		return false;
	}

	public ExtensionFileFilter getFilter() {
		return filter;
	}

	public void setFilter(ExtensionFileFilter filter) {
		this.filter = filter;
	}

}
