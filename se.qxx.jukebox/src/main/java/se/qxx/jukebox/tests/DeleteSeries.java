package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.protodb.ProtoDB;

public class DeleteSeries {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			try {
				ProtoDB db = new ProtoDB("jukebox_proto.db");
				
				Series s = null;
				
				if (StringUtils.isNumeric(args[0])) {
					s = db.get(Integer.parseInt(args[0]), JukeboxDomain.Series.getDefaultInstance());
					
					if (s != null) 
						db.delete(s);
					else 
						System.out.println("Nothing found!");					
				}
				else {
					System.out.println("Please supply the series ID!");
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
