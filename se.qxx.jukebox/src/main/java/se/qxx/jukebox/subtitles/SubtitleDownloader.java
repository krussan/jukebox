package se.qxx.jukebox.subtitles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;

import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IMkvSubtitleReader;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.interfaces.ISubFileUtilHelper;
import se.qxx.jukebox.interfaces.ISubFinder;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.IUnpacker;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;

@Singleton
public class SubtitleDownloader extends JukeboxThread implements ISubtitleDownloader {
	ReentrantLock lock = new ReentrantLock();
	private String subsPath = StringUtils.EMPTY;
	private List<ISubFinder> subFinders = new ArrayList<ISubFinder>();
	private IDatabase database;
	private ISettings settings;
	private IMovieBuilderFactory movieBuilderFactory;
	private ISubFileDownloaderHelper helper;
	private IMkvSubtitleReader mkvSubtitleReader;
	private IUnpacker unpacker;
	private ISubFileUtilHelper fileUtilHelper;
	private IUtils utils;
	
	@Inject
	public SubtitleDownloader(IDatabase database, 
			IExecutor executor, 
			ISettings settings,
			IMovieBuilderFactory movieBuilderFactory,
			ISubFileDownloaderHelper helper,
			IMkvSubtitleReader mkvSubtitleReader,
			LoggerFactory loggerFactory,
			IUnpacker unpacker,
			ISubFileUtilHelper fileUtilHelper,
			IUtils utils) {
		super(
			"Subtitle", 
			settings.getSettings().getSubFinders().getThreadWaitSeconds() * 1000,
			loggerFactory.create(LogType.SUBS),
			executor);
		this.setUtils(utils);
		this.setFileUtilHelper(fileUtilHelper);
		this.setUnpacker(unpacker);
		this.setMkvSubtitleReader(mkvSubtitleReader);
		this.setDatabase(database);
		this.setSettings(settings);
		this.setMovieBuilderFactory(movieBuilderFactory);
		this.setHelper(helper);
	}

	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	public ISubFileUtilHelper getFileUtilHelper() {
		return fileUtilHelper;
	}

	public void setFileUtilHelper(ISubFileUtilHelper fileUtilHelper) {
		this.fileUtilHelper = fileUtilHelper;
	}

	public IUnpacker getUnpacker() {
		return unpacker;
	}

	public void setUnpacker(IUnpacker unpacker) {
		this.unpacker = unpacker;
	}

	public IMkvSubtitleReader getMkvSubtitleReader() {
		return mkvSubtitleReader;
	}

	public void setMkvSubtitleReader(IMkvSubtitleReader mkvSubtitleReader) {
		this.mkvSubtitleReader = mkvSubtitleReader;
	}

	public ISubFileDownloaderHelper getHelper() {
		return helper;
	}

	public void setHelper(ISubFileDownloaderHelper helper) {
		this.helper = helper;
	}

	public List<ISubFinder> getSubFinders() {
		return subFinders;
	}
	
	public IMovieBuilderFactory getMovieBuilderFactory() {
		return movieBuilderFactory;
	}

	public void setMovieBuilderFactory(IMovieBuilderFactory movieBuilderFactory) {
		this.movieBuilderFactory = movieBuilderFactory;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	@Override
	protected void initialize() {
		cleanupTempDirectory();
		subsPath = this.getSettings().getSettings().getSubFinders().getSubsPath();
		
		this.getLog().Debug("Cleaning up subtitle queue");
		this.getDatabase().cleanSubtitleQueue();
		
		setupSubFinders();
	}

	private void setupSubFinders() {

		for (SubFinder f : this.getSettings().getSettings().getSubFinders().getSubFinder()) {
			String className = f.getClazz();

			try {
				Object[] args = new Object[] { this.getHelper(), f };
				
				getSubFinders().add(
					(ISubFinder)this.getUtils().getInstance(className, new Class[] {ISubFileDownloaderHelper.class, SubFinder.class}, args));
				
			} catch (Exception e) {
				this.getLog().Error(String.format("Error when loading subfinder :: %s", className), e);
			}
		}
	}

	@Override
	protected void execute() {
		int result = 0;
		
		this.getLog().Debug("Retrieving list to process");
		List<MovieOrSeries> _listProcessing =  this.getDatabase().getSubtitleQueue();
		
		List<Language> languages = getPreferredLanguages();
		
		for (MovieOrSeries mos : _listProcessing) {
			try {
				if (mos != null) {
					List<SubFile> files = getSubtitles(mos, languages);
					
					if (!this.isRunning())
						break;
					
					// Extract files from rar/zip
					List<Subtitle> subtitleList = extractSubs(mos, files);
					
					if (!this.isRunning())
						break;
			
					if (subtitleList.size() > 0)
						saveSubtitles(mos.getMedia(), subtitleList);
					
					result = 1;
				}
				
				
			} catch (Exception e) {
				this.getLog().Error("Error when downloading subtitles", e);
				result = -1;
			}
			
			saveSubtitleQueue(mos, result);
			
			if (!this.isRunning())
				break;
			
		}
	}


	@Override
	public List<Language> getPreferredLanguages() {
		List<Language> result = new ArrayList<Language>();
		result.add(Language.English);
		result.add(Language.Swedish);
		return result;
	}

	private void saveSubtitleQueue(MovieOrSeries mos, int result) {
		if (mos.isSeries()) {
			saveSubtitleQueue(mos.getEpisode(), result);
		}
		else {
			saveSubtitleQueue(mos.getMovie(), result);
		}
		
	}

	private void saveSubtitleQueue(Episode ep, int result) {
		SubtitleQueue q = ep.getSubtitleQueue();
		
		// Be sure to get the whole object before saving
		ep = this.getDatabase().getEpisode(ep.getID());
		
		this.getDatabase().save(Episode.newBuilder(ep)
			.setSubtitleQueue(
				SubtitleQueue.newBuilder(q)
					.setSubtitleRetreivedAt(this.getDatabase().getCurrentUnixTimestamp())
					.setSubtitleRetreiveResult(result)
					.build()
				).build());
	}
	
	private void saveSubtitleQueue(Movie m, int result) {
		SubtitleQueue q = m.getSubtitleQueue();
		
		// Be sure to get the whole object before saving
		m = this.getDatabase().getMovie(m.getID());
		
		this.getDatabase().save(Movie.newBuilder(m)
				.setSubtitleQueue(
					SubtitleQueue.newBuilder(q)									
						.setSubtitleRetreivedAt(this.getDatabase().getCurrentUnixTimestamp())
						.setSubtitleRetreiveResult(result)
						.build()
					).build());
	}
	

	/**
	 * Empties the temp directory. 
	 * The temp directory should be empty except when an unclean shutdown has been performed.
	 */
	private void cleanupTempDirectory() {
		this.getLog().Info(String.format("Removing temporary directory :: %s", subsPath));
		File tempDir = new File(subsPath);
		try {
			if (tempDir.exists()) 
				FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			this.getLog().Error("Error when deleting temp directory", e);
		}
	}

	/**
	 * Add a movie to the subtitile download queue.
	 * @param m The movie to add
	 */
	public void addMovie(Movie m) {
		lock.lock();
		try {
			this.getDatabase().addMovieToSubtitleQueue(m);
			this.signal();
		}
		finally {
			lock.unlock();
		}
	}
	
	public void reenlistMovie(Movie m) {
		m = clearSubs(m);
		addMovie(m);
	}

	public void reenlistEpisode(Episode ep) {
		ep = clearSubs(ep);
		addEpisode(ep);
	}

	private Movie clearSubs(Movie m) {
		Movie.Builder mb = Movie.newBuilder(m);
		
		List<Media> newMedia = clearSubsFromMediaList(m.getMediaList());
		mb.clearMedia().addAllMedia(newMedia);

		return this.getDatabase().save(mb.build());
	}

	private List<Media> clearSubsFromMediaList(List<Media> medialist) {
		List<Media> mlist = new ArrayList<>();
		for (Media md : medialist) {
			mlist.add(
					Media.newBuilder(md)
					.clearSubs()
					.build());		
		}
		
		return mlist;
	}

	private Episode clearSubs(Episode ep) {
		Episode.Builder epb = Episode.newBuilder(ep);
		
		List<Media> newMedia = clearSubsFromMediaList(ep.getMediaList());
		epb.clearMedia().addAllMedia(newMedia);
		
		return this.getDatabase().save(
			epb.build());
	}

	/**
	 * Add an episode to the subtitile download queue.
	 * The episode and series have to be saved separately
	 * @param episode The episode to add
	 */
	public void addEpisode(Episode episode) {
		lock.lock();
		try {
			episode = this.getDatabase().addEpisodeToSubtitleQueue(episode);
			this.signal();
		}  
		finally {
			lock.unlock();
		}
	}

	
	/**
	 * Wrapper function for getting subs for a specific movie
	 * @param mos
	 */
	private List<SubFile> getSubtitles(MovieOrSeries mos, List<Language> languages) {
		// We only check if there exist subs for the first media file.
		// If it does then it should exist from the others as well.
		
		//BUG; MOS object expects only 1 episode in the series object.
		// it was designed for the watcher only. We need to find the correct media
		// if we have an episode
		// -- This should be taken care of by the decouple method in DB but it seems off
		Media md = mos.getMedia();

		List<SubFile> files;
		
		boolean hasMatroskaSubtitles = checkMatroskaFile(md);
		files = checkMovieDirForSubs(md);
		
		if (files.size() == 0 && !hasMatroskaSubtitles) {
			// use reflection to get all subfinders
			// get files
			files = callSubtitleDownloaders(mos, languages);
		}
		
		return files;
	}
	
	/***
	 * Checks if the media is a MKV file and has embedded subs
	 * If so then save the subs in database for streaming use
	 * @param md
	 * @return
	 */
	private boolean checkMatroskaFile(Media md) {
		if (this.getUtils().isMatroskaFile(md)) {
			if (md.getDownloadComplete()) {
				this.getLog().Debug(String.format("Checking mkv container for media %s",  md.getFilename()));
				
				List<Subtitle> subs = 
					this.getMkvSubtitleReader().extractSubs(this.getUtils().getFullFilePath(md));
				
				if (subs == null || subs.size() == 0)
					return false;
	
				this.getLog().Debug(String.format("%s subs found in container. Saving...", subs.size()));
				
				md = this.getDatabase().getMediaById(md.getID());
				
				if (md != null) {
					this.getDatabase().save(
						Media.newBuilder(md)
							.addAllSubs(subs)
							.build());
				}
			}
			return true;
		}
		
		return false;
	}

	/**
	 * Checks movie path for subtitles for a specific movie
	 * @param md
	 * @return A list of subfiles for the movie
	 */
	public List<SubFile> checkMovieDirForSubs(Media md) {
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
			list = checkDirForSubs(FilenameUtils.getBaseName(mediaFilename), dir);
		
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
			
			this.getLog().Debug(String.format("Found %s subs for movie %s in movie folder", list.size(), mediaFilename));
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
						sf.setDescription(movieFilenameWithoutExt);
						sf.setLanguage(Language.Unknown);
						
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
	 * @param mos The movie for these sub files
	 * @param files The files downloaded
	 */
	private List<Subtitle> extractSubs(MovieOrSeries mos, List<SubFile> files) {
		String unpackPath = getUnpackedPath(mos);
		String tempFilepath = this.getFileUtilHelper().createTempSubsPath(mos);
		this.getLog().Debug(String.format("Unpack path :: %s", unpackPath));
		
		List<Subtitle> subtitleList = new ArrayList<Subtitle>();
		
		for (SubFile subfile : files) {
			try {
				File f = subfile.getFile();
				clearPath(unpackPath);
				
				List<File> unpackedFiles = this.getUnpacker().unpackFiles(f, unpackPath);
				subtitleList.addAll(
					constructSubtitles(mos, subfile, unpackedFiles));

			} catch (Exception e) {
				this.getLog().Error("Error when downloading subtitles... Continuing with next one", e);
			}
		}
		
		cleanupTempDirectory(tempFilepath);
		
		return subtitleList;
	}

	private List<Subtitle>  constructSubtitles(MovieOrSeries mos, SubFile subfile, List<File> unpackedFiles) {
		List<Subtitle> subtitleList = new ArrayList<Subtitle>();
		
		for (File unpackedFile : unpackedFiles) {
			if (unpackedFile != null) {
				Media md = matchFileToMedia(mos, unpackedFile);
				
				if (md != null) {
					// read file
					String textdata = readSubFile(unpackedFile);
					
					// store filename of sub in database
					subtitleList.add(
						Subtitle.newBuilder()
							.setID(-1)
							.setMediaIndex(md.getIndex())
							.setFilename(unpackedFile.getName())
							.setDescription(subfile.getDescription())
							.setRating(subfile.getRating())
							.setLanguage(subfile.getLanguage().toString())
							
							.setTextdata(ByteString.copyFromUtf8(textdata))
							.build());
					
				}
				else {
					this.getLog().Debug(String.format("Failed to match sub %s against media for movie %s", unpackedFile.getName(), mos.getTitle()));
				}
			}
		}
		
		return subtitleList;
	}

	private void cleanupTempDirectory(String tempFilepath) {
		try {
			FileUtils.deleteDirectory(new File(tempFilepath));
		} catch (IOException e) {
			this.getLog().Error(String.format("Error while deleting temporary dir :: %s", tempFilepath));
		}
	}
	
	

	private void saveSubtitles(Media md, List<Subtitle> subtitleList) {
		// Get media to get the whole object
		md = this.getDatabase().getMediaById(md.getID());
		this.getDatabase().save(
			Media.newBuilder(md)
				.addAllSubs(subtitleList)
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
			this.getLog().Error(String.format("Error while reading sub file %s", unpackedFile.getName()));
		} 
		finally {
		    try {
				br.close();
			} catch (IOException e) {
				this.getLog().Error(String.format("Error while closing stream on sub file %s", unpackedFile.getName()));
			}
		}
		
		return result;
	}
	/**
	 * Matches the filename of an unpacked sub file against the media in the movie
	 * @param mos
	 * @param unpackedFile
	 * @return The media matching the sub filename
	 */
	private Media matchFileToMedia(MovieOrSeries mos, File unpackedFile) {
		MovieOrSeries unpackedMos = this.getMovieBuilderFactory()
				.identify("", unpackedFile.getName());
		
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
	 * @param path
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
	 * @param mos The movie for which the subs are destined for
	 * @return The path
	 */
	private String getUnpackedPath(MovieOrSeries mos) {
		String tempBase = this.getFileUtilHelper().createTempSubsPath(mos);
		return String.format("%s/unpack", tempBase);
	}

	/**
	 * Iterates through all sub finders declared in the settings file.
	 * Each sub finder is called in sequence and asked to download files corresponding to a movie.
	 * Returns a list of sub files downloaded from each sub finder
	 * @param mos The movie to download subs for
	 * @return A list of sub files downloaded. These files could be in raw format as delivered by the sub finder. I.e. they
	 * 		   may have to be decompressed.
	 */
	private List<SubFile> callSubtitleDownloaders(MovieOrSeries mos, List<Language> languages) {
		ArrayList<SubFile> subtitleFiles = new ArrayList<SubFile>();
		for (ISubFinder finder : this.getSubFinders()) {
			// add list of downloaded files to list
			subtitleFiles.addAll(
				finder.findSubtitles(mos, languages));
		}

		return subtitleFiles;
	}
	
	@Override
	public int getJukeboxPriority() {
		return 3;
	}

	@Override
	public void end() {
		this.getHelper().exit();
		
		super.end();
		
	}

	@Override
	public Runnable getRunnable() {
		return this;
	}
}
