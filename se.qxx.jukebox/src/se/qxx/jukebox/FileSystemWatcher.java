package se.qxx.jukebox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class FileSystemWatcher implements Runnable {

	private Boolean isRunning = false;
	
	  private static Comparator<FileRepresentation> comparator = new Comparator<FileRepresentation>() {

	    public int compare(FileRepresentation fr0, FileRepresentation fr1) {
	      return fr0.getName().compareTo(fr1.getName());
	    }

	  };

	  protected File directory;
	  protected List<INotifyClient> clients;

	  private TreeSet<FileRepresentation> files;
	  private long sleepTime = 10000;

	  private ExtensionFileFilter _filter;

  	  private boolean watchCreated = false;
  	  private boolean watchModified = false;
  	  
	  public FileSystemWatcher(String directoryName, ExtensionFileFilter filter, boolean watchCreated, boolean watchModified) {
	    File directoryToWatch = new File(directoryName);
	    if (!directoryToWatch.isDirectory()) {
	      throw new RuntimeException("It needs to be a directory");
	    }
	    directory = directoryToWatch;
	    files = getCurrentRepresentation();
	    clients = new ArrayList<INotifyClient>();
	    _filter = filter;
	    this.watchCreated = watchCreated;
	    this.watchModified = watchModified;
	  }

	  public void setSleepTime(long sleepTime) {
	    this.sleepTime = sleepTime;
	  }

	  public long getSleepTime() {
	    return this.sleepTime;
	  }

	  public void registerClient(INotifyClient client) {
	    clients.add(client);
	  }

	  public java.util.TreeSet<FileRepresentation> getCurrentRepresentation() {
		  TreeSet<FileRepresentation> rep = new TreeSet<FileRepresentation>(comparator);
		  
		  List<File> list = Util.getFileListing(directory, _filter);
		  for (File f : list) {
			  rep.add(new FileRepresentation(f.getParent(), f.getName(), f.lastModified()));
		  }
		  
		  return rep;		
	  }
	  


	  private class FileChangedThread implements Runnable {

		  private INotifyClient client;
		  private FileRepresentation file;
		  private boolean isCreated = false;
		  private boolean isModified = false;
		  
		  public FileChangedThread(INotifyClient client, FileRepresentation f, boolean isCreated, boolean isModified) {
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
	  
	  public void notifyCreated(FileRepresentation f) {
	    for (INotifyClient client : clients) {
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
		  isRunning = true;
		  TreeSet<FileRepresentation> currentRepresentation = getCurrentRepresentation();
		  
		 for (FileRepresentation f : currentRepresentation) {
			 notifyCreated(f);
		 }	
		  
	    while (isRunning) {
	      currentRepresentation = getCurrentRepresentation();
	      
	      for (FileRepresentation f : currentRepresentation) {
	    	  if (!files.contains(f) && this.watchCreated)
	    		  notifyCreated(f);

	    	  for (FileRepresentation o : files) {
	    		  if (o.getName() == f.getName()) {
	    			  if (o.getLastModified() != f.getLastModified() && this.watchModified) {
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
		  isRunning = false;
	  }
	}

