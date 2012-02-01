package se.qxx.jukebox;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFile.Rating;

public class SubtitleDownloader implements Runnable {

	//TODO: add event listeners that listens for that subtitles for a specific movie
	// has been downloaded
		
	private LinkedList<Movie> _listToDownload = new LinkedList<Movie>();
	private List<Movie> _listProcessing = new ArrayList<Movie>();
	private List<Movie> _listDone = new ArrayList<Movie>();
	
	private static SubtitleDownloader _instance;
	private boolean _isRunning;
	
	private SubtitleDownloader() {
		
	}
	
	public static SubtitleDownloader get() {
		if (_instance == null)
			_instance = new SubtitleDownloader();
		
		return _instance;
	}

	@Override
	public void run() {
		this._isRunning = true;
				
		while (this._isRunning = true) {
			try {
				synchronized(_listToDownload) {
					_listToDownload.wait();
					_listProcessing.addAll(_listToDownload);
					_listToDownload.clear();
				}
				
				for(Movie m : _listProcessing) {
					try {
						getSubtitles(m);
						
						_listDone.add(m);
					}
					catch (Exception e) {
						Log.Error("Error when downloading subtitles", e);
					}
					finally {
						//TODO: Adding movie to done to remove it from the list.
						// should actually add this to an error list and let the user
						// decide if to continue
						_listDone.add(m);
						_listProcessing.removeAll(_listDone);
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//this.wait();
	}
	
	public void addMovie(Movie m) {
		synchronized(_listToDownload) {
			_listToDownload.add(m);
			_listToDownload.notify();
		}
	}
	
	public void stop() {
		
	}
	
	private void getSubtitles(Movie m) {
		// use reflection to get all subfinders
		// get files
		List<SubFile> files = callSubtitleDownloaders(m);
		
		// Extract files from rar/zip
		extractSubs(m, files);
	}

	private void extractSubs(Movie m, List<SubFile> files) {
		int c = 0;
		for (SubFile subfile : files) {
			try {
				File f = subfile.getFile();
				File unpackedFile = Unpacker.unpackFile(f);
				
				String filename = m.getFilename();
				filename = filename.substring(0, filename.lastIndexOf("."));
				
				String extension = unpackedFile.getName();
				extension = extension.substring(extension.lastIndexOf(".") + 1, extension.length());
			
				// rename file to filename_of_movie.iterator.srt/sub
				String path = f.getAbsolutePath();
				
				// replace backward slash with forward slash
				path = path.replace("\\", "/");
			
				path = path.substring(0, path.lastIndexOf("/"));
				filename = String.format("%s/%s.%s.%s", path, filename, c, extension);
				File newFile = new File(filename);
				unpackedFile.renameTo(newFile);
			
				c++;
	
				// rate sub
				subfile.setRating(Util.rateSub(m, subfile.getDescription()));
				
				// store filename of sub in database
				storeSubToDB(m, filename, subfile.getDescription(), subfile.getRating());
			}
			catch (Exception e) {
				Log.Error("Error when downloading subtitles... Continuing with next one", e);
			}
		}
	}
	
	private List<SubFile> callSubtitleDownloaders(Movie m) {
		ArrayList<SubFile> subtitleFiles = new ArrayList<SubFile>();
		for (SubFinder f : Settings.get().getSubFinders().getSubFinder()) {
			String className = f.getClazz();
			
			try {
				Class<?> c = Class.forName(className);
				Class<?>[] interfaces = c.getInterfaces();
				for (Class<?> i : interfaces) {
					if (i.getName().equals("se.qxx.jukebox.subtitles.ISubtitleFinder")) {
						Log.Debug(String.format("%s implements ISubtitleFinder", className));
						
						Class<?>[] parTypes = new Class<?>[] {};
						Object[] args = new Object[] {};
						Constructor<?> con = c.getConstructor(parTypes);
						Object o = con.newInstance(args);
						
						ArrayList<String> languages = new ArrayList<String>();
						languages.add("Eng");
						languages.add("Swe");
						
						// add list of downloaded files to list
						subtitleFiles.addAll( 
							((se.qxx.jukebox.subtitles.ISubtitleFinder)o).findSubtitles(
								m, 
								languages, 
								Settings.get().getSubFinders().getSubsPath(),
								f.getSubFinderSettings())
						);
						
						
					}
					
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.Error(String.format("Error when loading subfinder :: %s", className), e);
			}
		}
		
		return subtitleFiles;
	}
	
	private void storeSubToDB(Movie m, String filename, String description, Rating r) {
		try {
			DB.addSubtitle(m, filename, description, r);
		} catch (SQLException e) {
			Log.Error("Failed to store subtitle file to database", e);
		} catch (ClassNotFoundException e) {
			Log.Error("Failed to store subtitle file to database", e);
		}
	}	
}
