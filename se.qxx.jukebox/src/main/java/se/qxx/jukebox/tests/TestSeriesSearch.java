package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;

public class TestSeriesSearch {

	private IDatabase db;
	private ISettings settings;

	@Inject
	public TestSeriesSearch(IDatabase db, ISettings settings, LoggerFactory factory) {
		this.db = db;
		this.settings = settings;
		factory.create(LogType.FIND);
	}
	
	public static void main(String[] args) throws IOException, JAXBException {
		Injector injector = Binder.setupBindings(args);
		TestSeriesSearch prog = injector.getInstance(TestSeriesSearch.class);
		prog.execute(args[0]);
		
		if (args.length > 0) {
			prog.execute(args[0]);
		}
		else {
			System.out.println("No arguments");
		}
	}

	public void execute(String seriesNameOrId) {
		try {
			String driver = settings.getSettings().getDatabase().getDriver();
			String connectionString = settings.getSettings().getDatabase().getConnectionString();
			ProtoDB protoDB = ProtoDBFactory.getInstance(driver, connectionString, "protodb_test.log");
			
			Series s = null;
			
			if (StringUtils.isNumeric(seriesNameOrId))
				s = protoDB.get(Integer.parseInt(seriesNameOrId), JukeboxDomain.Series.getDefaultInstance());
			else {
				List<Series> result = db.searchSeriesByTitle(seriesNameOrId, 15, 0);
				
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

}
