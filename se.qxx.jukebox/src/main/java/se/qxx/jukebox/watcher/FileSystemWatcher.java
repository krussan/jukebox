package se.qxx.jukebox.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFileCreatedHandler;
import se.qxx.jukebox.interfaces.IFileSystemWatcher;
import se.qxx.jukebox.interfaces.IUtils;

public class FileSystemWatcher extends JukeboxThread implements IFileSystemWatcher {

	Map<String, FileRepresentation> currentRepresentation ;
	
	private static Comparator<FileRepresentation> comparator = Comparator.comparing(FileRepresentation::getName);

	protected File directory;

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	protected List<IFileCreatedHandler> clients = new ArrayList<IFileCreatedHandler>();

	public List<IFileCreatedHandler> getClients() {
		return clients;
	}

	private Map<String, FileRepresentation> files;

	public Map<String, FileRepresentation> getFiles() {
		return files;
	}

	public void setFiles(Map<String, FileRepresentation> files) {
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

	private IUtils utils;

	@Inject
	public FileSystemWatcher(LoggerFactory loggerFactory,
			IExecutor executor,
			IUtils utils,
			@Assisted("Name") String name, 
			@Assisted("Directory") String directoryName, 
			@Assisted ExtensionFileFilter filter, 
			@Assisted("WatchCreated") boolean watchCreated,
			@Assisted("WatchModified") boolean watchModified, 
			@Assisted("Recurse") boolean recurse, 
			@Assisted int waitTime) {
		super(name, waitTime, loggerFactory.create(LogType.FIND), executor);
		this.setUtils(utils);
		
		File directoryToWatch = new File(directoryName);

		this.setDirectory(directoryToWatch);
		this.setFilter(filter);

		this.setFiles(getCurrentRepresentation());

		this.setWatchCreated(watchCreated);
		this.setWatchModified(watchModified);
		this.setRecurse(recurse);
	}

	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	public boolean isRecurse() {
		return recurse;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.watcher.IFileSystemWatcher#registerClient(se.qxx.jukebox.watcher.IFileCreatedHandler)
	 */
	@Override
	public void registerClient(IFileCreatedHandler client) {
		clients.add(client);
	}

	public Map<String, FileRepresentation> getCurrentRepresentation() {
		return this.getUtils().getFileListing(
			this.getDirectory(), 
			this.getFilter(), 
			this.isRecurse());
	}

	public void notifyCreated(FileRepresentation f) {
		for (IFileCreatedHandler client : this.getClients()) {
			this.getExecutor().start(() -> {
				client.fileCreated(f);
			});
		}
	}

	public void notifyModified(FileRepresentation f) {
		for (IFileCreatedHandler client : clients) {
			this.getExecutor().start(() -> {
				client.fileModified(f);
			});
		}
	}

	@Override
	public void initialize() {
		currentRepresentation = getCurrentRepresentation();

		for (String key: currentRepresentation.keySet()) {
			notifyCreated(currentRepresentation.get(key));
			if (!this.isRunning())
				break;
		}		
	}
	
	@Override
	public void execute() {
		currentRepresentation = getCurrentRepresentation();

		for (String key: currentRepresentation.keySet()) {
			FileRepresentation f = currentRepresentation.get(key);

			if (!fileExistsInCurrentRepresentation(f)) {
				notifyCreated(f);
			}

			if (!this.isRunning())
				break;
		}

		files = currentRepresentation;		
	}

	private boolean fileExistsInCurrentRepresentation(FileRepresentation f) {
		FileRepresentation o = this.files.get(f.getName().toLowerCase());
		if (o != null) {
			if (o.getLastModified() != f.getLastModified() && this.isWatchModified()) {
				notifyModified(f);
			}

			return true;
		}

		return false;
	}

	public ExtensionFileFilter getFilter() {
		return filter;
	}

	public void setFilter(ExtensionFileFilter filter) {
		this.filter = filter;
	}

	@Override
	public Runnable getRunnable() {
		return this;
	}
	
	@Override
	public int getJukeboxPriority() {
		return 10;
	}

}
