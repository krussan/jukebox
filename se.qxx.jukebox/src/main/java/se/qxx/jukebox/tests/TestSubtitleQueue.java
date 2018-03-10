package se.qxx.jukebox.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import fr.noop.subtitle.model.SubtitleObject;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.model.SubtitleWriter;
import fr.noop.subtitle.srt.SrtParser;
import fr.noop.subtitle.vtt.VttWriter;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.SubtitleDownloader;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.Subscene;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;
import se.qxx.jukebox.tools.Util;

public class TestSubtitleQueue {

	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		try {
			Settings.initialize();
			
			DB.getSubtitleQueue();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


}
