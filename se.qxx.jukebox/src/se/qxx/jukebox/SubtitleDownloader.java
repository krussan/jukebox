package se.qxx.jukebox;

import java.io.File;
import java.io.FilenameFilter;
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
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Logs;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFile.Rating;
import se.qxx.jukebox.subtitles.SubFinderBase;

public class SubtitleDownloader implements Runnable {

	// TODO: add event listeners that listens for that subtitles for a specific
	// movie
	// has been downloaded


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
		List<Movie> _listProcessing =  DB.getSubtitleQueue();
		this._isRunning = true;
		
		while (Settings.get() == null) {
			Log.Info("Settings has not been initialized. Sleeping for 10 seconds", Log.LogType.MAIN);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		
		while (this._isRunning = true) {
			int result = 0;
			try {
				for (Movie m : _listProcessing) {
					try {
						if (m != null) {
							getSubtitles(m);
							result = 1;
						}
					} catch (Exception e) {
						Log.Error("Error when downloading subtitles", Log.LogType.SUBS, e);
						result = -1;
					} finally {
						// TODO: Adding movie to done to remove it from the
						// list.
						// should actually add this to an error list and let the
						// user
						// decide if to continue
						DB.setSubtitleDownloaded(m, result);
					}
				}

				
				// wait for trigger
				synchronized (_instance) {
					_instance.wait(15000);
					_listProcessing = DB.getSubtitleQueue();					
				}
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}			
		}
		// this.wait();
	}

	public void addMovie(Movie m) {
		synchronized (_instance) {
			DB.addMovieToSubtitleQueue(m);
			_instance.notify();
		}

	}

	public void stop() {
		this._isRunning = false;
	}

	/**
	 * Wrapper function for getting subs for a specific movie
	 * @param m
	 */
	private void getSubtitles(Movie m) {
		List<SubFile> files = checkSubs(m);

		if (files.size() == 0) {
			// use reflection to get all subfinders
			// get files
			files = callSubtitleDownloaders(m);
		}

		// Extract files from rar/zip
		extractSubs(m, files);
	}

	/**
	 * Checks both movie path and jukebox subs path for subtitles for a specific movie
	 * @param m
	 * @return A list of subfiles for the movie
	 */
	private List<SubFile> checkSubs(Movie m) {
		// check if subs exist in directory. If so set that as the sub
		// check file that begins with the same filename and has extension of
		// sub, idx, srt
		// also check if a sub-directory exists thats named Subs or SubFiles if
		// it does check that for subtitles.
		// also check subs directory (if the movie has been in DB before)
		// to avoid downloading subs again
		
		List<SubFile> list = new ArrayList<SubFile>();
		File dir = new File(m.getFilepath());
		String movieFilenameWithoutExt = Util.getFilenameWithoutExtension(m.getFilename());
		
		String subsPath =  Settings.get().getSubFinders().getSubsPath();
		File subsPathDir =  new File(String.format("%s/%s", subsPath, movieFilenameWithoutExt));
		
		list.addAll(checkDirForSubs(movieFilenameWithoutExt,subsPathDir));

		Log.Debug(String.format("Found %s subs for movie %s in subs folder", list.size(), movieFilenameWithoutExt), Log.LogType.SUBS);
		
		// if subs exist in subs directory don't check the rest
		if (list.size() == 0) {
			list.addAll(checkDirForSubs(movieFilenameWithoutExt, dir));
	
			String[] dirs = dir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.equals("Subs") || name.equals("SubFiles");
				}
			});
			
			for (String newDir : dirs) {
				list.addAll(checkDirForSubs(movieFilenameWithoutExt, new File(newDir)));
			}
		}

		return list;
	}

	/**
	 * Checks if a directory contains subfiles for a movie. The subtitles has to start with
	 * the same name as the file of the movie and end with one of the subtitle extensions srt, idx or sub.
	 * 
	 * @param movieFilenameWithoutExt
	 * @param dir
	 * @return A list of subfiles. An empty list if none found.
	 */
	private List<SubFile> checkDirForSubs(String movieFilenameWithoutExt, File dir) {
		List<SubFile> list = new ArrayList<SubFile>();
		if (dir != null) {
			String[] subs = dir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File f, String name) {
					return name.endsWith("srt") || name.endsWith("idx") || name.endsWith("sub"); 
				}
			});
		
			if (subs != null) {
				for (String subFile : subs) {
					if (subFile.startsWith(movieFilenameWithoutExt))
						list.add(new SubFile(new File(subFile)));
				}
			}
		}
		
		return list;
	}

	/**
	 * 
	 * @param m
	 * @param files
	 */
	private void extractSubs(Movie m, List<SubFile> files) {
		int c = 0;
		for (SubFile subfile : files) {
			try {
				File f = subfile.getFile();
				File unpackedFile = Unpacker.unpackFile(f, SubFinderBase.createTempSubsPath());

				String filename = moveFileToSubsPath(m, c, f, unpackedFile);

				c++;

				// rate sub
				// subfile.setRating(Util.rateSub(m, subfile.getDescription()));

				// store filename of sub in database
				DB.addSubtitle(m, filename, subfile.getDescription(),
						subfile.getRating());

			} catch (Exception e) {
				Log.Error(
						"Error when downloading subtitles... Continuing with next one", Log.LogType.SUBS, e);
			}
		}
	}

	protected String moveFileToSubsPath(
			Movie m, 
			int c, 
			File f,
			File unpackedFile) {
		String filename = m.getFilename();
		filename = filename.substring(0, filename.lastIndexOf("."));

		String extension = unpackedFile.getName();
		extension = extension.substring(extension.lastIndexOf(".") + 1,
				extension.length());

		// rename file to filename_of_movie.iterator.srt/sub
		String path = f.getAbsolutePath();

		// replace backward slash with forward slash
		path = path.replace("\\", "/");
		path = path.substring(0, path.lastIndexOf("/"));
		filename = String.format("%s/%s.%s.%s", path, filename, c,
				extension);
		
		File newFile = new File(filename);
		unpackedFile.renameTo(newFile);
		return filename;
	}

	private List<SubFile> callSubtitleDownloaders(Movie m) {
		ArrayList<SubFile> subtitleFiles = new ArrayList<SubFile>();
		for (SubFinder f : Settings.get().getSubFinders().getSubFinder()) {
			String className = f.getClazz();

			try {
				Object[] args = new Object[] {f.getSubFinderSettings()};
				SubFinderBase finder = (SubFinderBase)Util.getInstance(className, args);

				ArrayList<String> languages = new ArrayList<String>();
				languages.add("Eng");
				languages.add("Swe");

				// add list of downloaded files to list
				subtitleFiles.addAll(finder.findSubtitles(m, languages));

			} catch (Exception e) {
				
				Log.Error(String.format("Error when loading subfinder :: %s", className), Log.LogType.SUBS, e);
			}
		}

		return subtitleFiles;
	}

}
