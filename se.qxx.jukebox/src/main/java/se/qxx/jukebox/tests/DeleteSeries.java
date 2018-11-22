package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class DeleteSeries {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			try {
				Series s = null;
				
				if (StringUtils.isNumeric(args[0])) {
					int id = Integer.parseInt(args[0]);
					s = DB.getSeries(id);
					
					if (s != null) 
						DB.delete(s);
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
