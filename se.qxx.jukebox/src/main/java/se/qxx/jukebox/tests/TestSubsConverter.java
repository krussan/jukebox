package se.qxx.jukebox.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Injector;

import fr.noop.subtitle.model.SubtitleParsingException;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;

public class TestSubsConverter {

	private IDatabase db;
	private ISubtitleFileWriter subFileWriter;
	
	@Inject
	public TestSubsConverter(IDatabase db, ISubtitleFileWriter subFileWriter) {
		this.db = db;
		this.subFileWriter = subFileWriter;
		
	}
	public static void main(String[] args) throws IOException, DatabaseNotSupportedException, ClassNotFoundException, SQLException, SearchFieldNotFoundException, SubtitleParsingException {
		Injector injector = Binder.setupBindings(args);
		TestSubsConverter prog = injector.getInstance(TestSubsConverter.class);
		
		if (args.length > 0)
			prog.execute(args[0]);
	}

	public void execute(String title) throws DatabaseNotSupportedException, ClassNotFoundException, SQLException, SearchFieldNotFoundException, FileNotFoundException, IOException, SubtitleParsingException {
		ProtoDB protoDB = db.getProtoDBInstance();

		List<Movie> movies = protoDB.find(Movie.getDefaultInstance(), "title", title, true);
		
		if (movies != null && movies.size() > 0) {
			Movie m = movies.get(0);
			System.out.println(String.format("Found movie :: %s", m.getTitle()));
			
			if (m.getMedia(0).getSubsCount() > 0) {
				Subtitle sub = m.getMedia(0).getSubs(0);
				
				try {
					
					File originalFile = new File("original.srt");
					if (originalFile.exists())
						originalFile.delete();
					
						
					
					originalFile = subFileWriter.writeSubtitleToFile(sub, originalFile);
					System.out.println(String.format("Wrote original :: %s", originalFile.getAbsolutePath()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				File f = subFileWriter.writeSubtitleToFileConvert(sub, subFileWriter.getTempFile(sub, "vtt"));
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
