package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.google.inject.Inject;

import fr.noop.subtitle.model.SubtitleParsingException;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;
import se.qxx.jukebox.tools.Util;

public class TestSubsConverter {

	private IDatabase db;
	
	@Inject
	public TestSubsConverter(IDatabase db) {
		this.db = db;
		
	}
	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		try {
			if (args.length > 0) {
			}
			else {
				System.out.println("No arguments");
			}
		
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException e) {
			e.printStackTrace();
		} catch (SubtitleParsingException e) {
			e.printStackTrace();
		}

	}

	public void execute() {
		ProtoDB protoDB = DB.getProtoDBInstance();

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


}
