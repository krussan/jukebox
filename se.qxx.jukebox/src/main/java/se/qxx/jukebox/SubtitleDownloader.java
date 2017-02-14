package se.qxx.jukebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import com.google.protobuf.ByteString;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFinderBase;
import se.qxx.jukebox.subtitles.Subs;
import se.qxx.jukebox.tools.Unpacker;
import se.qxx.jukebox.tools.Util;

public class SubtitleDownloader implements Runnable {

	// TODO: total rewrite. ! The thing to download all offline maybe not the best.
	// let the user decide?
	// TODO: store them in the database to avoid discrepancies
	// TODO: add event listeners that listens for that subtitles for a specific
	
	// movie
	// has been downloaded


	private String subsPath = StringUtils.EMPTY;
	private static SubtitleDownloader _instance;
	private boolean isRunning;

	private SubtitleDownloader() {
	}

	public static SubtitleDownloader get() {
		if (_instance == null)
			_instance = new SubtitleDownloader();

		return _instance;
	}

	@Override
	public void run() {
		this.setRunning(true);
		cleanupTempDirectory();
		
		Util.waitForSettings();
				
		subsPath = Settings.get().getSubFinders().getSubsPath();
			
//		initializeSubsDatabase();

		mainLoop();
		// this.wait();
	}

	protected void mainLoop() {
		long threadWaitSeconds = Settings.get().getSubFinders().getThreadWaitSeconds() * 1000;
		
		Log.Debug("Retrieving list to process", LogType.SUBS);
		List<MovieOrSeries> _listProcessing =  DB.getSubtitleQueue();				
		
		while (isRunning()) {
			int result = 0;
			try {
				for (MovieOrSeries mos : _listProcessing) {
					try {
						if (mos != null) {
							getSubtitles(mos);
							result = 1;
						}
					} catch (Exception e) {
						Log.Error("Error when downloading subtitles", Log.LogType.SUBS, e);
						result = -1;

					}
					
					saveSubtitleQueue(mos, result);
				}
				
				// wait for trigger
				synchronized (_instance) {
					_instance.wait(threadWaitSeconds);
					_listProcessing = DB.getSubtitleQueue();					
				}
				
			} catch (InterruptedException e) {
				Log.Error("SUBS :: SubtitleDownloader is going down ...", LogType.SUBS, e);
			}			
		}
	}


	


	private void saveSubtitleQueue(MovieOrSeries mos, int result) {
		
		if (mos.isSeries()) {
			SubtitleQueue q = mos.getEpisode().getSubtitleQueue();
			
			DB.save(Episode.newBuilder(mos.getEpisode())
				.setSubtitleQueue(
					SubtitleQueue.newBuilder(q)
						.setSubtitleRetreivedAt(DB.getCurrentUnixTimestamp())
						.setSubtitleRetreiveResult(result)
						.build()
					).build());
		}
		else {
			SubtitleQueue q = mos.getMovie().getSubtitleQueue();
			
			DB.save(Movie.newBuilder(mos.getMovie())
				.setSubtitleQueue(
					SubtitleQueue.newBuilder(q)									
						.setSubtitleRetreivedAt(DB.getCurrentUnixTimestamp())
						.setSubtitleRetreiveResult(result)
						.build()
					).build());
		}
		
	}

	/**
	 * Empties the temp directory. 
	 * The temp directory should be empty except when an unclean shutdown has been performed.
	 */
	private void cleanupTempDirectory() {
		Log.Info(String.format("Removing temporary directory :: %s", subsPath), LogType.SUBS);
		File tempDir = new File(subsPath);
		try {
			if (tempDir.exists()) 
				FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			Log.Error("Error when deleting temp directory", LogType.SUBS, e);
		}
	}

	/**
	 * Add a movie to the subtitile download queue.
	 * @param m The movie to add
	 */
	public Movie addMovie(Movie m) {
		synchronized (_instance) {
			Movie mm = DB.addMovieToSubtitleQueue(m);
			_instance.notify();
			
			return mm;
		} 
	}
	
	/**
	 * Add an episode to the subtitile download queue.
	 * The episode and series have to be saved separately
	 * @param episode The episode to add
	 */
	public Episode addEpisode(Episode episode) {
		synchronized (_instance) {
			episode = DB.addEpisodeToSubtitleQueue(episode);
			_instance.notify();
			
			return episode;
		}  
	}

	/**
	 * Stops the subtitle download thread
	 */
	public void stop() {
		this.setRunning(false);
	}

	/**
	 * Wrapper function for getting subs for a specific movie
	 * @param m
	 */
	private void getSubtitles(MovieOrSeries mos) {
		// We only check if there exist subs for the first media file.
		// If it does then it should exist from the others as well.
		Media md = mos.getMedia();
		List<SubFile> files = checkMovieDirForSubs(md);
		
		if (files.size() == 0) {
			// use reflection to get all subfinders
			// get files
			files = callSubtitleDownloaders(mos);
		}

		// Extract files from rar/zip
		extractSubs(mos, files);
	}

	/**
	 * Checks movie path for subtitles for a specific movie
	 * @param m
	 * @return A list of subfiles for the movie
	 */
	private List<SubFile> checkMovieDirForSubs(Media md) {
		// check if subs exist in directory. If so set that as the sub
		// check file that begins with the same filename and has extension of
		// sub, idx, srt
		// also check if a sub-directory exists thats named Subs or SubFiles if
		// it does check that for subtitles.
		// also check subs directory (if the movie has been in DB before)
		// to avoid downloading subs again
		
		// We check for a srt/idx/sub file for the first media only
		String mediaFilename = md.getFilename();
		String mediaPath = md.getFilepath();

		File dir = new File(mediaPath);
		List<SubFile> list = new ArrayList<SubFile>();
		
		if (dir != null && dir.exists()) {
			list = checkDirForSubs(mediaFilename, dir);
		
			String[] dirs = dir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.equals("Subs") || name.equals("SubFiles");
				}
			});
			
			if (dirs != null) {
				for (String newDir : dirs) {
					File subsDir = new File(newDir);
					
					if (subsDir.exists() && subsDir.isDirectory())
						list.addAll(checkDirForSubs(mediaFilename, subsDir));
				}
			}
			
			Log.Debug(String.format("Found %s subs for movie %s in movie folder", list.size(), mediaFilename), Log.LogType.SUBS);
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
		if (dir != null && dir.exists()) {
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
	 * If the downloaded file is a zip- or rar-file then this method extracts the sub and moves it to the subs directory.
	 * If it is not then it justs moves it to the subs directory.
	 * @param m The movie for these sub files
	 * @param files The files downloaded
	 */
	private void extractSubs(MovieOrSeries mos, List<SubFile> files) {
		String unpackPath = getUnpackedPath(mos);
		String tempFilepath = SubFinderBase.createTempSubsPath(mos);
		Log.Debug(String.format("Unpack path :ö: %s", unpackPath), LogType.SUBS);
		
		int c = 0;
		for (SubFile subfile : files) {
			try {
				File f = subfile.getFile();
				clearPath(unpackPath);
				
				List<File> unpackedFiles = Unpacker.unpackFiles(f, unpackPath);

				for (File unpackedFile : unpackedFiles) {
					if (unpackedFile != null) {
						Media md = matchFileToMedia(mos, unpackedFile);
						
						if (md != null) {
							c++;
	
							// read file
							String textdata = readSubFile(unpackedFile);
							
							// store filename of sub in database
							saveSubtitle(subfile, unpackedFile, md, textdata);
							
						}
						else {
							Log.Debug(String.format("Failed to match sub %s against media for movie %s", unpackedFile.getName(), mos.getTitle()), LogType.SUBS);
						}
					}
				}

			} catch (Exception e) {
				Log.Error("Error when downloading subtitles... Continuing with next one", Log.LogType.SUBS, e);
			}
		}
		
		cleanupTempDirectory(tempFilepath);
	}

	private void cleanupTempDirectory(String tempFilepath) {
		try {
			FileUtils.deleteDirectory(new File(tempFilepath));
		} catch (IOException e) {
			Log.Error(String.format("Error while deleting temporary dir :: %s", tempFilepath), LogType.SUBS);
		}
	}

	private void saveSubtitle(SubFile subfile, File unpackedFile, Media md, String textdata) {
		Log.Debug(String.format("Saving subtitle %s - %s bytes", subfile.getDescription(), textdata.length()), LogType.SUBS);
		
		DB.save(
			Media.newBuilder(md).addSubs(
				Subtitle.newBuilder()
					.setID(-1)
					.setMediaIndex(md.getIndex())
					.setFilename(unpackedFile.getName())
					.setDescription(subfile.getDescription())
					.setRating(subfile.getRating())
					.setLanguage(subfile.getLanguage().toString())
					
					.setTextdata(ByteString.copyFromUtf8(textdata))
					.build())
				.build());
	}

	private String readSubFile(File unpackedFile) {
		BufferedReader br = null;
		String result = StringUtils.EMPTY;
		try {
			br = new BufferedReader(new FileReader(unpackedFile));

			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    result = sb.toString();
		} 
		catch (IOException e) {
			Log.Error(String.format("Error while reading sub file %s", unpackedFile.getName()), LogType.SUBS);
		} 
		finally {
		    try {
				br.close();
			} catch (IOException e) {
				Log.Error(String.format("Error while closing stream on sub file %s", unpackedFile.getName()), LogType.SUBS);
			}
		}
		
		return result;
	}
	/**
	 * Matches the filename of an unpacked sub file against the media in the movie
	 * @param m
	 * @param unpackedFile
	 * @return The media matching the sub filename
	 */
	private Media matchFileToMedia(MovieOrSeries mos, File unpackedFile) {
		MovieOrSeries unpackedMos = MovieBuilder.identify("", unpackedFile.getName());
		
		int subIndex = unpackedMos.getMedia().getIndex();
		
		for (Media md : mos.getMediaList()) {
			if (md.getIndex() == subIndex)
				return md;
		}
				
		// else return the first media found...
		return mos.getMedia();
	}

	/**
	 * If the path exist then clear all files and directories from it. If it does not then create it.
	 * @param unpackPath
	 * @throws IOException
	 */
	private void clearPath(String path)
			throws IOException {
		File f = new File(path);

		if (f.exists())
			FileUtils.cleanDirectory(new File(path));
		else
			f.mkdirs();
	}

	/**
	 * Gets a temporary directory in the subs folder for storing temporary unpacked subs.
	 * @param m The movie for which the subs are destined for
	 * @return The path
	 */
	private String getUnpackedPath(MovieOrSeries mos) {
		String tempBase = SubFinderBase.createTempSubsPath(mos);
		return String.format("%s/unpack", tempBase);
	}

	/**
	 * Iterates through all sub finders declared in the settings file.
	 * Each sub finder is called in sequence and asked to download files corresponding to a movie.
	 * Returns a list of sub files downloaded from each sub finder
	 * @param m The movie to download subs for
	 * @return A list of sub files downloaded. These files could be in raw format as delivered by the sub finder. I.e. they
	 * 		   may have to be decompressed.
	 */
	private List<SubFile> callSubtitleDownloaders(MovieOrSeries mos) {
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
				subtitleFiles.addAll(finder.findSubtitles(mos, languages));

			} catch (Exception e) {
				
				Log.Error(String.format("Error when loading subfinder :: %s", className), Log.LogType.SUBS, e);
			}
		}

		return subtitleFiles;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
