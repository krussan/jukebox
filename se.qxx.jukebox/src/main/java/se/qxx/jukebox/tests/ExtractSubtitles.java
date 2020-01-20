package se.qxx.jukebox.tests;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;

public class ExtractSubtitles {

	public static void main(String[] args) {
		if (args.length > 0) {
			
			try {
				
				Media md = null;
				Injector injector = Binder.setupBindings(args);
				IDatabase db = injector.getInstance(IDatabase.class);
				ISubtitleFileWriter fileWriter = injector.getInstance(ISubtitleFileWriter.class);
				
				if (StringUtils.isNumeric(args[0])) {
					int id = Integer.parseInt(args[0]);
					md = db.getMediaById(id);
					
 					if (md != null) { 
						for (Subtitle sub : md.getSubsList()) {
							fileWriter.writeSubtitleToFile(sub, fileWriter.getTempFile(sub, FilenameUtils.getExtension(sub.getFilename())));
							fileWriter.writeSubtitleToFileConvert(sub, fileWriter.getTempFile(sub, "vtt"));
						}
 					}
					else 
						System.out.println("Nothing found!");					
				}
				else {
					System.out.println("Please supply the media ID!");
				}
				
				
			} catch (Exception e) {
				System.out.println("failed to get information from database");
				System.out.println(e.toString());

			}
		}
		else {
			System.out.println("No arguments");
		}
	}


}
