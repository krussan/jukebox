package se.qxx.jukebox.converter;

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

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.subtitles.Language;
import se.qxx.jukebox.subtitles.MkvSubtitleReader;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFinderBase;
import se.qxx.jukebox.subtitles.Subs;
import se.qxx.jukebox.tools.Unpacker;
import se.qxx.jukebox.tools.Util;

public class MediaConverter implements Runnable {

	// TODO: total rewrite. ! The thing to download all offline maybe not the best.
	// let the user decide?
	// TODO: store them in the database to avoid discrepancies
	// TODO: add event listeners that listens for that subtitles for a specific
	
	// movie
	// has been downloaded


	private String subsPath = StringUtils.EMPTY;
	private static MediaConverter _instance;
	private boolean isRunning;
	private static final int THREAD_WAIT_SECONDS = 3000;
	private static boolean converting = false; 

	private MediaConverter() {
	}

	public static MediaConverter get() {
		if (_instance == null)
			_instance = new MediaConverter();

		return _instance;
	}

	@Override
	public void run() {
		this.setRunning(true);
		
		Util.waitForSettings();

		mainLoop();
	}

	protected void mainLoop() {
		Log.Debug("Retrieving list to process", LogType.SUBS);
		
		List<Media> _listProcessing =  DB.getConverterQueue();				
		
		while (isRunning()) {
			MediaConverterState result = MediaConverterState.Failed;
			
			try {
				for (Media md : _listProcessing) {
					try {
						if (md != null) {
							triggerConverter(md);
							result = MediaConverterState.Completed;
						}
					} catch (Exception e) {
						Log.Error("Error when converting media", Log.LogType.SUBS, e);
						result = MediaConverterState.Failed;
					}
					
					saveConvertedMedia(md, result);
				}
				
				// wait for trigger
				synchronized (_instance) {
					_instance.wait(THREAD_WAIT_SECONDS);
					_listProcessing = DB.getConverterQueue();					
				}
				
			} catch (InterruptedException e) {
				Log.Error("SUBS :: MediaConverter is going down ...", LogType.SUBS, e);
			}			
		}
	}


	


	private void triggerConverter(Media md) {
//		String filename = md.getFilename();
//		String newFilename = String.format("%s_jukebox.mp4",
//				FilenameUtils.getBaseName(md.getFilename()));
//				
//		String cmd = String.format("ffmpeg -i %s -copy %s", )
//		Process p = Runtime.getRuntime().exec("ffmpeg");
//		//ffmpeg -i Atomic.Blonde.2017.720p.HC.HDRip.850MB.MkvCage.mkv -c:a copy -c:v copy Atomic.Blonde.2017.720p.HC.HDRip.850MB.MkvCage_jukebox.mp4
//		p.waitFor();
	}

	private void saveConvertedMedia(Media md, MediaConverterState result) {
		Media o = Media.newBuilder(md)
				.setConverterState(result)
				.build();

		DB.save(o);
		
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
