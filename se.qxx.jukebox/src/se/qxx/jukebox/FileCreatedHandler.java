package se.qxx.jukebox;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFile.Rating;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import se.qxx.jukebox.subtitles.SubFile;

public class FileCreatedHandler implements INotifyClient {

	@Override
	public void fileModified(FileRepresentation f) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileCreated(FileRepresentation f)  {
		Log.Debug(String.format("New file found :: %s", f.getName()));
		Movie m = Util.extractMovie(f.getPath(), f.getName());
		
		m = getImdbInformation(m);
		m = addMovieToDB(m);
		
		getSubtitles(m);
	}
	
	private void getSubtitles(Movie m) {
		// use reflection to get all subfinders
		// get files
		List<SubFile> files = callSubtitleDownloaders(m);
		
		// exract files from rar/zip
		extractSubs(m, files);
	}

	private void extractSubs(Movie m, List<SubFile> files) {
		int c = 0;
		for (SubFile subfile : files) {
			File f = subfile.getFile();
			File unpackedFile = Unpacker.unpackFile(f);
			
			String filename = m.getFilename();
			filename = filename.substring(0, filename.lastIndexOf("."));
			
			String extension = unpackedFile.getName();
			extension = extension.substring(extension.lastIndexOf(".") + 1, extension.length());
		
			// rename file to filename_of_movie.iterator.srt/sub
			String path = f.getAbsolutePath();
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

	
	private Movie addMovieToDB(Movie m) {
		try {
			m = DB.addMovie(m);
			Log.Info("Movie added to database");
		}
		catch (Exception e) {
			Log.Error("failed to store to database", e);
		}
		
		return m;
	}

	private Movie getImdbInformation(Movie m) {
		//find imdb link
		try {
			m = IMDBFinder.Search(m);
			Log.Info(String.format("Movie identified as :: %s", m.getTitle()));
		}
		catch (IOException e) {
			Log.Error("Error occured when finding IMDB link", e);
		}
		
		return m;
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
