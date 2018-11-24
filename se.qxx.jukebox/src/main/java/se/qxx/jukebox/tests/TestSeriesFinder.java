package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;
import se.qxx.protodb.SearchOptions;
import se.qxx.protodb.model.ProtoDBSearchOperator;

public class TestSeriesFinder {

	private ISettings settings;
	
	@Inject
	public TestSeriesFinder(ISettings settings) {
		this.settings = settings;
	}
	
	public static void main(String[] args) throws IOException, JAXBException {
		Injector injector = Binder.setupBindings(args);
		TestSeriesFinder prog = injector.getInstance(TestSeriesFinder.class);
		
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
			ProtoDB db = ProtoDBFactory.getInstance(driver, connectionString, "protodb_test.log");
			
			Series s = null;
			
			if (StringUtils.isNumeric(seriesNameOrId))
				s = db.get(Integer.parseInt(seriesNameOrId), JukeboxDomain.Series.getDefaultInstance());
			else {
				List<Series> result =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
							.addFieldName("title")
							.addOperator(ProtoDBSearchOperator.Like)
							.addSearchArgument(seriesNameOrId)
							.setShallow(false));

				
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
