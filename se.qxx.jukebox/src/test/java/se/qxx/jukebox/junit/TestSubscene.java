package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import se.qxx.jukebox.WebResult;
import se.qxx.jukebox.WebRetriever;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.Subscene;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WebRetriever.class)
public class TestSubscene {

	private SubFinderSettings subFinderSettings = null;
	private final String searchResult = "";
	private final String listResult = "";
	private final String download1Result = "";
	private final String download2Result = "";
	
	@Before
	public void init() throws IOException, JAXBException {
		Settings.initialize();
		initializeSettings();
	}
	
	private void initializeSettings() {
		for (SubFinder sf : Settings.get().getSubFinders().getSubFinder()) {
			if (StringUtils.equalsIgnoreCase(sf.getClazz(), "se.qxx.jukebox.subtitles.Subscene")) {
				subFinderSettings = sf.getSubFinderSettings();
			}
		}
	}
	
	@Test
	public void test() throws IOException {
		assertNotNull(subFinderSettings);
		
		PowerMockito.mockStatic(WebRetriever.class);

		Subscene ss = new Subscene(subFinderSettings);
		
		WebResult result = new WebResult(new URL("http://localhost/subtitiles/english/mockito"), "", false);
		Mockito.when(WebRetriever.getWebResult("https://subscene.com/subtitles/title?q=Mockito")).thenReturn(result);
	
		Movie m = Movie.newBuilder()
				.setID(1)
				.setIdentifiedTitle("Mockito")
				.setTitle("Mockito")
				.build();
		
		List<String> lang = new ArrayList<String>();
		lang.add("English");
		
		List<SubFile> files = ss.findSubtitles(m, lang);
		
		assertEquals(2, files.size());
	}

}
