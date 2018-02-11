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

public class TestSubsConverter {

	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		try {
			if (args.length > 0) {
				ProtoDB db = DB.getProtoDBInstance();
				int id = Integer.parseInt(args[2]);

				List<Movie> movies = db.find(Movie.getDefaultInstance(), "title", args[0], true);
				
				if (movies != null && movies.size() > 0) {
					Movie m = movies.get(0);
					System.out.println(String.format("Found movie :: %s", m.getTitle()));
					
					if (m.getMedia(0).getSubsCount() > 0) {
						Subtitle sub = m.getMedia(0).getSubs(0);
						
						try {
							
							File originalFile = new File("original.srt");
							if (originalFile.exists())
								originalFile.delete();
							
								
							
							originalFile = Util.writeSubtitleToFile(sub, originalFile);
							System.out.println(String.format("Wrote original :: %s", originalFile.getAbsolutePath()));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						
						File f = Util.writeSubtitleToTempFileVTT(m.getMedia(0).getSubs(0));
						System.out.println(String.format("Wrote file :: %s", f.getAbsolutePath()));
					}
					else {
						System.out.println("Movie has no subtitles!");
					}
					
				}
				else {
					System.out.println("No movie found");
				}
			}
			else {
				System.out.println("No arguments");
			}
		
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SubtitleParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


}
