package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;
import se.qxx.protodb.SearchOptions;
import se.qxx.protodb.model.ProtoDBSearchOperator;

public class TestSeriesFinder {

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
					List<Series> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
								.addFieldName("title")
								.addOperator(ProtoDBSearchOperator.Like)
								.addSearchArgument(args[0])
								.setShallow(false));

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
