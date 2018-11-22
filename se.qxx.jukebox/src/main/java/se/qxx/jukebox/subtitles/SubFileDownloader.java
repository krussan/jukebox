package se.qxx.jukebox.subtitles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileDownloader;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.settings.Settings;

@Singleton
public class SubFileDownloader implements ISubFileDownloader {
	
	private ISettings settings;
	private boolean isRunning = true;

	private final int MAX_SUBS_DOWNLOADED = 15;
	private final int MIN_WAIT_SECONDS = 20;
	private final int MAX_WAIT_SECONDS = 30;

	private IWebRetriever webRetriever;
	
	@Inject
	public SubFileDownloader(ISettings settings, IWebRetriever webRetriever) {
		this.setWebRetriever(webRetriever);
		this.setSettings(settings);
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public IWebRetriever getWebRetriever() {
		return webRetriever;
	}

	public void setWebRetriever(IWebRetriever webRetriever) {
		this.webRetriever = webRetriever;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public List<SubFile> downloadSubs(String subFileClass, MovieOrSeries mos, List<SubFile> listSubs) {
		List<SubFile> files = new ArrayList<SubFile>();
		
		//Store downloaded files in temporary storage
		//SubtitleDownloader will move them to correct path
		String tempSubPath = createTempSubsPath(mos);
		
		int sizeCollection = listSubs.size();
		int c = 1;
		
		for (SubFile sf : listSubs) {
			try {
				SubFile sfi = downloadSubFile(subFileClass, sf, tempSubPath, sizeCollection, c);
				if (sfi != null)
					files.add(sfi);
				
				c++;
				
				if (c > MAX_SUBS_DOWNLOADED)
					break;
				
			}
			catch (IOException e) {
				Log.Error(String.format("%s :: Error when downloading subtitle :: %s", subFileClass, sf.getFile().getName()), LogType.SUBS, e);
			}
			
			if (listSubs.size() > 1) {
				waitRandomly();
			}

			if (!this.isRunning())
				return new ArrayList<SubFile>();
		}
		
		return files;
		
	}

	private SubFile downloadSubFile(String subFileClass, SubFile sf, String tempSubPath, int sizeCollection, int c)
			throws IOException {
		File file = this.getWebRetriever().getWebFile(sf.getUrl(), tempSubPath);

		if (file != null) {
			sf.setFile(file);
			
			Log.Debug(String.format("%s :: [%s/%s] :: File downloaded: %s"
					, subFileClass
					, c
					, sizeCollection
					, sf.getFile().getName())
				, Log.LogType.SUBS);
			
			return sf;

		}
		return null;
	}

	private void waitRandomly() {
		try {
			Random r = new Random();
			int n = r.nextInt((MAX_WAIT_SECONDS - MIN_WAIT_SECONDS) * 1000 + 1) + MIN_WAIT_SECONDS * 1000;
			
			Log.Info(String.format("Sleeping for %s seconds", n), LogType.SUBS);
			// sleep randomly to avoid detection (from 10 sec to 30 sec)
			Thread.sleep(n);
			
		} catch (InterruptedException e) {
			Log.Error("Subtitle downloader interrupted", Log.LogType.SUBS, e);
		}
		
	}
	
	/**
	 * Returns a temporary path to download subtitles to
	 * @return
	 */
	public String createTempSubsPath(MovieOrSeries mos) {
		String tempPath = 
			FilenameUtils.normalize(
				String.format("%s/temp/%s"
					, this.getSettings().getSettings().getSubFinders().getSubsPath()
					, mos.getID()));

		File path = new File(tempPath);
		if (!path.exists())
			path.mkdirs();
		
		return tempPath;
	}
	

	@Override
	public void exit() {
		this.setRunning(false);
	}

}
