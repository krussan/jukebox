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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
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
		cleanupTempDirectory();
		
		long threadWaitSeconds = Settings.get().getSubFinders().getThreadWaitSeconds() * 1000;
		
		while (Settings.get() == null) {
			Log.Info("Settings has not been initialized. Sleeping for 10 seconds", Log.LogType.MAIN);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		
		initializeSubsDatabase();
		
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
					_instance.wait(threadWaitSeconds);
					_listProcessing = DB.getSubtitleQueue();					
				}
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}			
		}
		// this.wait();
	}

	private void initializeSubsDatabase() {
		//TODO: Initialize subtitles by scanning subs directory. 
		// If an unclean purge has been performed then there could be subs in the directory
		// but not in the database		
	}

	private void cleanupTempDirectory() {
		String path = FilenameUtils.normalize(String.format("%s/temp", Settings.get().getSubFinders().getSubsPath()));
		Log.Info(String.format("Removing temporary directory :: %s", path), LogType.SUBS);
		File tempDir = new File(path);
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			Log.Error("Error when deleting temp directory", LogType.SUBS, e);
		}
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
		
		List<SubFile> files = checkSubsDirForSubs(m);
		
		if (files.size() == 0) {
			files = checkMovieDirForSubs(m);
			if (files.size() == 0) {
				// use reflection to get all subfinders
				// get files
				files = callSubtitleDownloaders(m);
			}
	
			// Extract files from rar/zip
			extractSubs(m, files);
		}

	}
	
	private List<SubFile> checkSubsDirForSubs(Movie m) {
		List<SubFile> list = new ArrayList<SubFile>();

		String movieFilenameWithoutExt = FilenameUtils.getBaseName(m.getFilename());
		
		String subsPath =  Settings.get().getSubFinders().getSubsPath();
		File subsPathDir = new File(FilenameUtils.normalize(String.format("%s/%s", subsPath, movieFilenameWithoutExt)));
		
		list.addAll(checkDirForSubs(movieFilenameWithoutExt,subsPathDir));

		Log.Debug(String.format("Found %s subs for movie %s in subs folder", list.size(), movieFilenameWithoutExt), Log.LogType.SUBS);

		return list;
	}

	/**
	 * Checks both movie path and jukebox subs path for subtitles for a specific movie
	 * @param m
	 * @return A list of subfiles for the movie
	 */
	private List<SubFile> checkMovieDirForSubs(Movie m) {
		// check if subs exist in directory. If so set that as the sub
		// check file that begins with the same filename and has extension of
		// sub, idx, srt
		// also check if a sub-directory exists thats named Subs or SubFiles if
		// it does check that for subtitles.
		// also check subs directory (if the movie has been in DB before)
		// to avoid downloading subs again
		String movieFilenameWithoutExt = FilenameUtils.getBaseName(m.getFilename());
		File dir = new File(m.getFilepath());
		
		List<SubFile> list = checkDirForSubs(movieFilenameWithoutExt, dir);
		
		String[] dirs = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("Subs") || name.equals("SubFiles");
			}
		});
		
		for (String newDir : dirs) {
			list.addAll(checkDirForSubs(movieFilenameWithoutExt, new File(newDir)));
		}
		
		Log.Debug(String.format("Found %s subs for movie %s in movie folder", list.size(), movieFilenameWithoutExt), Log.LogType.SUBS);
			

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
					if (StringUtils.startsWithIgnoreCase(subFile, movieFilenameWithoutExt)) {
						SubFile sf = new SubFile(new File(String.format("%s/%s", dir.getAbsolutePath(), subFile)));
						sf.setRating(Rating.SubsExist);
						list.add(sf);
					}
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
		String unpackPath = getUnpackedPath(m);
		File unpackPathFile = new File(unpackPath);
		
		Log.Debug(String.format("Unpack path :: %s", unpackPath), LogType.SUBS);
		
		int c = 0;
		for (SubFile subfile : files) {
			try {
				File f = subfile.getFile();
				
				if (unpackPathFile.exists())
					FileUtils.cleanDirectory(new File(unpackPath));
				else
					unpackPathFile.mkdirs();
				
				File unpackedFile = Unpacker.unpackFile(f, unpackPath);

				if (unpackedFile != null) {
					String filename = moveFileToSubsPath(m, c, unpackedFile);
	
					c++;
	
					// store filename of sub in database
					DB.addSubtitle(m, filename, subfile.getDescription(), subfile.getRating());
				}
			} catch (Exception e) {
				Log.Error(
						"Error when downloading subtitles... Continuing with next one", Log.LogType.SUBS, e);
			}
		}
		
		try {
			FileUtils.deleteDirectory(new File(SubFinderBase.createTempSubsPath(m)));
		} catch (IOException e) {
		}
	}

	private String getUnpackedPath(Movie m) {
		String tempBase = SubFinderBase.createTempSubsPath(m);
		return String.format("%s/unpack", tempBase);
	}

	protected String moveFileToSubsPath(
			Movie m, 
			int c, 
			File subFile) {
		
		// rename file to filename_of_movie.iterator.srt/sub		
		String movieFilename = FilenameUtils.getBaseName(m.getFilename());		
		String subExtension = FilenameUtils.getExtension(subFile.getName());
		String movieSubsPath = String.format("%s/%s", Settings.get().getSubFinders().getSubsPath(), movieFilename);
		
		String finalSubfileName = FilenameUtils.normalize(String.format("%s/%s.%s.%s", movieSubsPath, movieFilename, c, subExtension));
		
		File newFile = new File(finalSubfileName);
		try {
			FileUtils.moveFile(subFile, newFile);
		} catch (IOException e) {
			Log.Debug(String.format("Failed to move file %s to %s", subFile.getName(), finalSubfileName), LogType.SUBS	);
		}
		
		return finalSubfileName;
	}
	

	private List<SubFile> callSubtitleDownloaders(Movie m) {
		ArrayList<SubFile> subtitleFiles = new ArrayList<SubFile>();
		for (SubFinder f : Settings.get().getSubFinders().getSubFinder()) {
			String className = f.getClazz();

			try {
				SubFinderSettings[] args = new SubFinderSettings[] {f.getSubFinderSettings()};
				SubFinderBase finder = (SubFinderBase)Util.getInstance(className, new Class[] {SubFinderSettings.class}, args);

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
