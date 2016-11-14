package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFinderBase;
import se.qxx.jukebox.subtitles.Subscene;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WebRetriever.class)
public class TestSubscene {

	private SubFinderSettings subFinderSettings = null;
	private String searchResult = "";
	private String listResult = "";
	private String download1Result = "";
	private String download2Result = "";
	
	@Before
	public void init() throws IOException, JAXBException {
		Settings.initialize();
		initializeSettings();
		
		readResources();
	}


	private void initializeSettings() {
		for (SubFinder sf : Settings.get().getSubFinders().getSubFinder()) {
			if (StringUtils.equalsIgnoreCase(sf.getClazz(), "se.qxx.jukebox.subtitles.Subscene")) {
				subFinderSettings = sf.getSubFinderSettings();
			}
		}
	}
	
	
	private void readResources() {
		try {
			searchResult = readResource("TestSubscene_searchResult.txt");
			listResult = readResource("TestSubscene_listResult.txt");
			download1Result = readResource("TestSubscene_download1Result.txt");
			download2Result = readResource("TestSubscene_download2Result.txt");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	private String readResource(String resourceName) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourceName)));
		StringBuilder sb = new StringBuilder();
		
		String line;
		while ((line = in.readLine()) != null){
			sb.append(line).append("\n");
		}
		
		return sb.toString();	
	}
	
	@Test
	public void test() throws IOException {
		assertNotNull(subFinderSettings);
		
		PowerMockito.mockStatic(WebRetriever.class);

		Subscene ss = new Subscene(subFinderSettings, 0, 0);

		Movie m = Movie.newBuilder()
				.setID(1)
				.setIdentifiedTitle("Mockito")
				.setTitle("Mockito")
				.addMedia(Media.newBuilder()
						.setID(1)
						.setIndex(1)
						.setFilepath("/jukebox/")
						.setFilename("Mockito.2014.mp4")
						.build())
				.build();
		
		WebResult resultSearch = new WebResult(new URL("https://subscene.com/subtitles/title?q=Mockito"), searchResult, false);
		Mockito.when(WebRetriever.getWebResult("https://subscene.com/subtitles/title?q=Mockito")).thenReturn(resultSearch);
												
		WebResult resultList = new WebResult(new URL("https://subscene.com/subtitles/Mockito"), listResult, false);
		Mockito.when(WebRetriever.getWebResult("https://subscene.com/subtitles/Mockito")).thenReturn(resultList);

		WebResult resultDownload1 = new WebResult(new URL("https://subscene.com/subtitles/Mockito/1023456"), download1Result, false);
		Mockito.when(WebRetriever.getWebResult("https://subscene.com/subtitles/Mockito/1023456")).thenReturn(resultDownload1);

		WebResult resultDownload2 = new WebResult(new URL("https://subscene.com/subtitles/Mockito/1123456"), download2Result, false);
		Mockito.when(WebRetriever.getWebResult("https://subscene.com/subtitles/Mockito/1123456")).thenReturn(resultDownload2);
		
		String tempFilePath = SubFinderBase.createTempSubsPath(m);
		Mockito.when(
				WebRetriever.getWebFile("https://subscene.com/subtitle/download?mac=minYVuJCyMyRv2laUi3x4JRZpygROrUboH6SnmAZMHI0BjB2Ect5yTARB0a8KolifMxdZlSEtc-I5wEKPCP7OxtfvN6JYKOmrUtlONkO-4MWpvB-nqHp421TU8WptRiE0", 
						tempFilePath))
		.thenReturn(new File("temp1"));

		Mockito.when(
				WebRetriever.getWebFile("https://subscene.com/subtitle/download?mac=maxYVuJCyMyRv2laUi3x4JRZpygROrUboH6SnmAZMHI0BjB2Ect5yTARB0a8KolifMxdZlSEtc-I5wEKPCP7OxtfvN6JYKOmrUtlONkO-4MWpvB-nqHp421TU8WptRiE0", 
						tempFilePath))
		.thenReturn(new File("temp1"));

//		PowerMockito.verifyStatic();
//		Mockito.verify(WebRetriever.getWebResult("https://subscene.com/subtitles/title?q=Mockito"));
//		Mockito.verify(WebRetriever.getWebResult("https://subscene.com/subtitles/Mockito"));		
//		Mockito.verify(WebRetriever.getWebResult("https://subscene.com/subtitles/Mockito/1023456"));		
//		Mockito.verify(WebRetriever.getWebResult("https://subscene.com/subtitles/Mockito/1223456"));
//		Mockito.verify(WebRetriever.getWebResult("https://subscene.com/subtitle/download?mac=minYVuJCyMyRv2laUi3x4JRZpygROrUboH6SnmAZMHI0BjB2Ect5yTARB0a8KolifMxdZlSEtc-I5wEKPCP7OxtfvN6JYKOmrUtlONkO-4MWpvB-nqHp421TU8WptRiE0"));
//		Mockito.verify(WebRetriever.getWebResult("https://subscene.com/subtitle/download?mac=maxnYVuJCyMyRv2laUi3x4JRZpygROrUboH6SnmAZMHI0BjB2Ect5yTARB0a8KolifMxdZlSEtc-I5wEKPCP7OxtfvN6JYKOmrUtlONkO-4MWpvB-nqHp421TU8WptRiE0"));
		

		
		List<String> lang = new ArrayList<String>();
		lang.add("English");
		
		List<SubFile> files = ss.findSubtitles(m, lang);
		
		assertEquals(2, files.size());
	}

}
