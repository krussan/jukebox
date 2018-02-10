package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.ProtoDB;

public class ExtractSubtitles {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			try {
				
				Media md = null;
				
				if (StringUtils.isNumeric(args[0])) {
					int id = Integer.parseInt(args[0]);
					md = DB.getMediaById(id);
					
 					if (md != null) { 
						for (Subtitle sub : md.getSubsList()) {
							Util.writeSubtitleToTempFile(sub);
							Util.writeSubtitleToTempFileVTT(sub);
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
