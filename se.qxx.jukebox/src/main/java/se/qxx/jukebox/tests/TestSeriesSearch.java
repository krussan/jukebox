package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;

public class TestSeriesSearch {

	public static void main(String[] args) throws IOException, JAXBException {
		Settings.initialize();
		
		if (args.length > 0) {
			try {
				String driver = Settings.get().getDatabase().getDriver();
				String connectionString = Settings.get().getDatabase().getConnectionString();
				ProtoDB db = ProtoDBFactory.getInstance(driver, connectionString, "protodb_test.log");
				
				Series s = null;
				
				if (StringUtils.isNumeric(args[0]))
					s = db.get(Integer.parseInt(args[0]), JukeboxDomain.Series.getDefaultInstance());
				else {
					List<Series> result = DB.searchSeriesByTitle(args[0], 15, 0);
					
//						db.find(JukeboxDomain.Series.getDefaultInstance(), 
//							"title", 
//							args[0], 
//							true);
					
					if (result.size() > 0)
						s = result.get(0);
				}
				
				if (s != null) {
					System.out.println(s);
				}
				else 
					System.out.println("Nothing found!");
				
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
