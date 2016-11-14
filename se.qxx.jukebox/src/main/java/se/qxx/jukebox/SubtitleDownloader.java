package se.qxx.jukebox;

import java.io.File;
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
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
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
	private String subsXmlFilename = StringUtils.EMPTY;
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
		subsXmlFilename = String.format("%s/%s", FilenameUtils.normalize(subsPath), "subs.xml");
		
//		initializeSubsDatabase();

		mainLoop();
		// this.wait();
	}

	protected void mainLoop() {
		long threadWaitSeconds = Settings.get().getSubFinders().getThreadWaitSeconds() * 1000;
		List<Movie> _listProcessing =  DB.getSubtitleQueue();				
		
		while (isRunning()) {
			int result = 0;
			try {
				for (Movie m : _listProcessing) {
					try {
						if (m != null) {
							getSubtitles(m);
//							writeSubsXmlFile();
							result = 1;
						}
					} catch (Exception e) {
						Log.Error("Error when downloading subtitles", Log.LogType.SUBS, e);
						result = -1;
					} finally {
						
						DB.save(Movie.newBuilder(m)
							.setSubtitleQueue(
								SubtitleQueue.newBuilder()									
									.setSubtitleRetreivedAt(DB.getCurrentUnixTimestamp())
									.setSubtitleRetreiveResult(result)
									.build()
								).build());
					}
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

	/**
	 * Initialize subtitles by scanning subs directory. 
	   If subs.xml exist then use the xml file for identifying subs. Otherwise scan directory. 
	   If an unclean purge has been performed then there could be subs in the directory
	   but not in the database		
	*/
//	private void initializeSubsDatabase() {
//		if (!initSubsDatabaseFromXmlFile())
//			initSubsDatabaseFromDisk();
//	}
	
	/**
	 * Initialize subs database by scanning the subs xml file.
	 * If it exist then save all the data in it to database.
	 * Leaves existing entries in the xml file that do not match an existing movie as these
	 * could be movies that have not been identified yet
	 * @return true if subs database has been initialized from xml file
	 */
//	private boolean initSubsDatabaseFromXmlFile() {
//		try {				
//			Subs subs = getSubsFile();
//			if (subs != null) {
//				if (saveXmlFileToSubsDatabase(subs));
//					return true;
//			}
//		}
//		catch (Exception e) {
//			Log.Error("Error occured when parsing subs Xml file", LogType.SUBS, e);
//		}
//		
//		return false;		
//	}
	
	/**
	 * Saves a subs xml file to database.
	 * Leaves existing entries in the xml file that do not match an existing movie as these
	 * could be movies that have not been identified yet
	 * @param subs The parsed subs xml file
	 * @return True if all entries has been saved to database.
	 */
//	private boolean saveXmlFileToSubsDatabase(Subs subs) {
//		Log.Debug("INITSUBS_XML :: Saving subs xml file to database", LogType.SUBS);
//		
//		try {
//			for (se.qxx.jukebox.subtitles.Subs.Movie movie : subs.getMovie()) {
//				String movieFilename = movie.getFilename();
//				for (se.qxx.jukebox.subtitles.Subs.Movie.Sub sub : movie.getSub()) {
//					Media md = DB.getMediaByStartOfFilename(movieFilename);
//					if (md != null) {
//						String subsFilename = String.format("%s/%s/%s", subsPath, movieFilename, sub.getFilename());
//
//						Movie m = DB.getMovieBySubfilename(subsFilename);
//						
//						if (m == null) {
//								
//							Rating r = Rating.valueOf(sub.getRating());
//							Log.Debug(String.format("INITSUBS_XML :: Adding to subs database :: %s", sub.getFilename()), LogType.SUBS);
//							DB.addSubtitle(md, subsFilename, sub.getDescription(), r, sub.getLanguage());
//						}
//						else {
//							Log.Debug(String.format("INITSUBS_XML :: Sub exists in DB :: %s", sub.getFilename()), LogType.SUBS);							
//						}
//					}
//				}
//			}
//			
//			return true;
//		}
//		catch (Exception e) {
//			Log.Error("Error occured when parsing subs Xml file", LogType.SUBS, e);			
//		}
//		
//		return false;
//	}

	/**
	 * Parses the subs xml file into an object structure.
	 * The subs xml file should be in the root directory of the subs directory as indicated by settings.
	 * @return The parsed xml file as an object structure.
	 */
	private Subs getSubsFile() {
		try {
			Log.Debug(String.format("INITSUBS :: Looking for subs xml file :: %s", subsXmlFilename), LogType.SUBS);
			File subsFile = new File(subsXmlFilename);
			
			if (subsFile.exists()) {
				JAXBContext c = JAXBContext.newInstance(Subs.class);
				Unmarshaller u = c.createUnmarshaller();
				
				JAXBElement<Subs> root = u.unmarshal(new StreamSource(subsFile), Subs.class);
				Subs subs = root.getValue();		
				
				return subs;				
			}
			

		} catch (JAXBException e) {
			Log.Debug("Error occured when parsing subs Xml file", LogType.SUBS);
			Log.Error("Error occured when parsing subs Xml file", LogType.MAIN, e);
		}
		
		Log.Debug("INITSUBS :: Subs xml file not found or error occured", LogType.SUBS);
		
		return null;
	}
	
	
	/**
	 * Loads all existing subs from subs directory into database if a movie with the correct filename
	 * can be found.
	 * Also writes and merges a subs xml file.s
	 */
//	private void initSubsDatabaseFromDisk() {
//		ExtensionFileFilter filter = new ExtensionFileFilter();
//		filter.addExtension("srt");
//		filter.addExtension("sub");
//		filter.addExtension("idx");
//
//		File path = new File(subsPath);
//		
//		if (path.exists()) {
//			List<File> list =  Util.getFileListing(path, filter);
//			
//			for (File subFile : list) {
//				String subFilename = subFile.getAbsolutePath();
//				Log.Debug(String.format("INITSUBS :: Initializing subtitles for %s", subFilename), LogType.SUBS);
//				String mediaName = FilenameUtils.getBaseName(subFile.getParentFile().getAbsolutePath());
//				Media md = DB.getMediaByStartOfFilename(mediaName);
//				
//				if (md != null) {
//					Log.Debug(String.format("INITSUBS :: Movie Found :: %s", md.getFilename()), LogType.SUBS);
//					
//					boolean found = false;
//					for (Subtitle sub : md.getSubsList()) {
//						if (StringUtils.equalsIgnoreCase(sub.getFilename(), subFilename)) {
//							found = true;
//							break;
//						}
//					}
//					
//					if (!found){
//						Log.Debug(String.format("INITSUBS :: Sub not found ... Adding %s", subFilename), LogType.SUBS);
//												
//						DB.save(
//							Media.newBuilder(md).addSubs(
//								Subtitle.newBuilder()
//								.setID(-1)
//								.setDescription("")
//								.setMediaIndex(0)
//								.setLanguage("Default")
//								.setFilename(subFilename)
//								.setRating(Rating.NotMatched)
//								.build())
//							.build());
//						
////						DB.addSubtitle(md, subFilename, StringUtils.EMPTY, Rating.NotMatched, Language.Unknown.toString());						
//					}
//				}
//			}
//			
////			writeSubsXmlFile();
//		}
//	}
	
	/**
	 * Saves database entries to subs xml file.
	 * Leaves existing entries in the xml file that do not match an existing movie as these
	 * could be movies that have not been identified yet
	 */
//	private void writeSubsXmlFile() {
//		try {
//			
//			//TODO: <HIGH> This needs to actually MERGE the information in xml file
//			// and that in the database. Otherwise we will start deleting entries old entries that don't
//			// yet have a movie reference
//						
//			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
//			Document doc = docBuilder.newDocument();
//
//			Element subs = doc.createElement("subs");
//			
//			List<Movie> movies = DB.searchMoviesByTitle(StringUtils.EMPTY);
//			for (Movie m : movies) {
//				for (Media md : m.getMediaList()) {
//					Element movie = doc.createElement("movie");
//					movie.getAttributes().setNamedItem(getAttribute(doc, "filename", FilenameUtils.getBaseName(md.getFilename())));
//					
//					List<Subtitle> subtitles = DB.getSubtitles(md);
//					for (Subtitle s : subtitles) {
//						String subsFilename = FilenameUtils.getName(s.getFilename());
//						Element sub = doc.createElement("sub");
//						NamedNodeMap attrs = sub.getAttributes();
//						attrs.setNamedItem(getAttribute(doc, "filename", subsFilename));
//						attrs.setNamedItem(getAttribute(doc, "description", s.getDescription()));
//						attrs.setNamedItem(getAttribute(doc, "rating", s.getRating().toString()));
//						
//						movie.appendChild(sub);
//					}
//					
//					if (subtitles.size() > 0)
//						subs.appendChild(movie);
//				}
//			}
//			
//			
//			Subs subsFile = getSubsFile();
//			
//			if (subsFile != null) {
//				for (se.qxx.jukebox.subtitles.Subs.Movie subsMovie : subsFile.getMovie()) {
//					Media md = DB.getMediaByStartOfFilename(subsMovie.getFilename());
//					if (md == null) {
//						Log.Debug(String.format("SUBS :: Adding subs to xml file even if movie does not exist yet :: %s", subsMovie.getFilename()), LogType.SUBS);
//	
//						JAXBContext c = JAXBContext.newInstance(se.qxx.jukebox.subtitles.Subs.Movie.class);
//						Marshaller m = c.createMarshaller();
//					
//						m.marshal(subsMovie, subs);
//					}
//				}
//	
//				doc.appendChild(subs);
//			}
//			
//			Log.Debug(String.format("SUBS :: Writing xml file :: %s", subsXmlFilename), LogType.SUBS);
//			writeXmlDocument(doc, subsXmlFilename);
//		} catch (Exception e) {
//			Log.Error("Error when writing xml to file", LogType.SUBS, e);
//		}
//	}
	
	/**
	 * Helper method for writing xml document to file.
	 * @param doc The xml document
	 * @param filename Filename to write to.
	 */
	private void writeXmlDocument(Document doc, String filename) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Result output = new StreamResult(new File(filename));
			Source input = new DOMSource(doc);

			transformer.transform(input, output);
		} catch (Exception e) {
			Log.Error("Error when writing xml to file", LogType.SUBS, e);
		}		
	}
	
	/**
	 * Helper method for initializing an attribute in xml document
	 * @param doc The xml document
	 * @param name The name of the attribute
	 * @param value The value of the attribute
	 * @return An Attr object with the name and value.
	 */
	private Attr getAttribute(Document doc, String name, String value) {
		Attr a = doc.createAttribute(name);
		a.setValue(value);
		return a;
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
	 * @param m The movie to add
	 */
	public Episode addEpisode(Episode episode) {
		return Episode.newBuilder(episode).setSubtitleQueue(
				SubtitleQueue.newBuilder()
					.setID(-1)
					.setSubtitleRetreiveResult(0)				
					.setSubtitleQueuedAt(DB.getCurrentUnixTimestamp())
					.build())
				.build();	
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
	private void getSubtitles(Movie m) {
		// We only check if there exist subs for the first media file.
		// If it does then it should exist from the others as well.
		Media md = m.getMedia(0);
		List<SubFile> files = checkSubsDirForSubs(md);
		
		if (files.size() == 0) {
			files = checkMovieDirForSubs(md);
			if (files.size() == 0) {
				// use reflection to get all subfinders
				// get files
				files = callSubtitleDownloaders(m);
			}
	
			// Extract files from rar/zip
			extractSubs(m, files);
		}			
	}
	
	/**
	 * Checks if subs exists in subs directory for a specific movie.
	 * @param m The movie to find subtitles for
	 * @return Returns a list of subtitles, present in the subs directory, for the specific movie. 
	 */
	private List<SubFile> checkSubsDirForSubs(Media md) {
		List<SubFile> list = new ArrayList<SubFile>();

		String movieFilenameWithoutExt = FilenameUtils.getBaseName(md.getFilename());
		File subsPathDir = new File(getSubsPath(md));
		
		list.addAll(checkDirForSubs(movieFilenameWithoutExt,subsPathDir));

		Log.Debug(String.format("Found %s subs for movie %s in subs folder", list.size(), movieFilenameWithoutExt), Log.LogType.SUBS);

		return list;
	}

	/**
	 * Checks both movie path and jukebox subs path for subtitles for a specific movie
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
		List<SubFile> list = checkDirForSubs(mediaFilename, dir);
		
		String[] dirs = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("Subs") || name.equals("SubFiles");
			}
		});
		
		for (String newDir : dirs) {
			list.addAll(checkDirForSubs(mediaFilename, new File(newDir)));
		}
		
		Log.Debug(String.format("Found %s subs for movie %s in movie folder", list.size(), mediaFilename), Log.LogType.SUBS);
			
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
	 * If the downloaded file is a zip- or rar-file then this method extracts the sub and moves it to the subs directory.
	 * If it is not then it justs moves it to the subs directory.
	 * @param m The movie for these sub files
	 * @param files The files downloaded
	 */
	private void extractSubs(Movie m, List<SubFile> files) {
		String unpackPath = getUnpackedPath(m);
		
		Log.Debug(String.format("Unpack path :: %s", unpackPath), LogType.SUBS);
		
		int c = 0;
		for (SubFile subfile : files) {
			try {
				File f = subfile.getFile();
				clearPath(unpackPath);
				
				List<File> unpackedFiles = Unpacker.unpackFiles(f, unpackPath);

				for (File unpackedFile : unpackedFiles) {
					if (unpackedFile != null) {
						Media md = matchFileToMedia(m, unpackedFile);
						
						if (md != null) {
							String filename = moveFileToSubsPath(md, c, unpackedFile);
			
							c++;
			
							// store filename of sub in database
							DB.save(
								Media.newBuilder(md).addSubs(
									Subtitle.newBuilder()
										.setID(-1)
										.setMediaIndex(0)
										.setFilename(filename)
										.setDescription(subfile.getDescription())
										.setRating(subfile.getRating())
										.setLanguage(subfile.getLanguage().toString())
										.build())
									.build());
								
//							DB.addSubtitle(md, filename, subfile.getDescription(), subfile.getRating(), subfile.getLanguage().toString());
							
						}
						else {
							Log.Debug(String.format("Failed to match sub %s against media for movie %s", unpackedFile.getName(), m.getTitle()), LogType.SUBS);
						}
					}
				}

			} catch (Exception e) {
				Log.Error("Error when downloading subtitles... Continuing with next one", Log.LogType.SUBS, e);
			}
		}
		
		try {
			FileUtils.deleteDirectory(new File(SubFinderBase.createTempSubsPath(m)));
		} catch (IOException e) {
		}
	}

	/**
	 * Matches the filename of an unpacked sub file against the media in the movie
	 * @param m
	 * @param unpackedFile
	 * @return The media matching the sub filename
	 */
	private Media matchFileToMedia(Movie m, File unpackedFile) {
		MovieOrSeries mos = MovieBuilder.identify("", unpackedFile.getName());
		
		int subIndex = mos.getMedia().getIndex();
		
		for (Media md : m.getMediaList()) {
			if (md.getIndex() == subIndex)
				return md;
		}
				
		return null;
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
	private String getUnpackedPath(Movie m) {
		String tempBase = SubFinderBase.createTempSubsPath(m);
		return String.format("%s/unpack", tempBase);
	}

	/**
	 * Moves and renames a sub file to the subs folder corresponding to the movie.
	 * The name of the sub becomes moviename.iterator.extension
	 * 
	 * @param m The movie
	 * @param c Iterator
	 * @param subFile The original sub file
	 * @return A path string pointing to the new sub file
	 */
	protected String moveFileToSubsPath(
			Media md, 
			int c, 
			File subFile) {
	
		String subExtension = FilenameUtils.getExtension(subFile.getName());		
		// rename file to filename_of_movie.iterator.srt/sub		
		
		String finalSubfileName = FilenameUtils.normalize(
			String.format("%s/%s.%s.%s"
				, getSubsPath(md)
				, FilenameUtils.getBaseName(md.getFilename())
				, c
				, subExtension));
		
		File newFile = new File(finalSubfileName);
		try {
			FileUtils.moveFile(subFile, newFile);
		} catch (IOException e) {
			Log.Debug(String.format("Failed to move file %s to %s", subFile.getName(), finalSubfileName), LogType.SUBS	);
		}
		
		return finalSubfileName;
	}
	
	private String getSubsPath(Media md) {
		String movieFilename = FilenameUtils.getBaseName(md.getFilename());	
		//PartPattern pp = new PartPattern(movieFilename);
		String movieSubsPath = String.format("%s/%s", subsPath, movieFilename);
		return movieSubsPath;		
	}
	

	/**
	 * Iterates through all sub finders declared in the settings file.
	 * Each sub finder is called in sequence and asked to download files corresponding to a movie.
	 * Returns a list of sub files downloaded from each sub finder
	 * @param m The movie to download subs for
	 * @return A list of sub files downloaded. These files could be in raw format as delivered by the sub finder. I.e. they
	 * 		   may have to be decompressed.
	 */
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

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
