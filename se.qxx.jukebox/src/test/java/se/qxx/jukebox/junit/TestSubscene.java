package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.IRandomWaiter;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.interfaces.ISubFileUtilHelper;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.subtitles.Language;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubFileDownloaderHelper;
import se.qxx.jukebox.subtitles.Subscene;
import se.qxx.jukebox.tools.WebResult;

public class TestSubscene {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	private String searchResult = "";
	private String listResult = "";
	private String download1Result = "";
	private String download2Result = "";
	
	private Settings settings;
	private IJukeboxLogger log;
	private MovieBuilderFactory movieBuilderFactory;
	
	@Mock private IWebRetriever webRetrieverMock;
	@Mock private LoggerFactory loggerFactoryMock;
	@Mock private IRandomWaiter waiterMock;
	@Mock private ISubFileUtilHelper fileUtilHelperMock;

	
	@Before
	public void init() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		log = new Log(settings, LogType.NONE);
		
		when(loggerFactoryMock.create(any(Log.LogType.class))).thenReturn(log);
		
		new ParserBuilder(settings, log);
		movieBuilderFactory = new MovieBuilderFactory(settings, loggerFactoryMock);

		readResources();
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
		ISubFileDownloaderHelper helper = createHelper();
		Subscene ss = new Subscene(helper);

		Movie m = Movie.newBuilder()
				.setID(1)
				.setIdentifiedTitle("Mockito")
				.setTitle("Mockito")
				.addMedia(Media.newBuilder()
						.setID(1)
						.setIndex(1)
						.setFilepath("/jukebox/")
						.setFilename("Mockito.2014.mp4")
						.setDownloadComplete(false)
						.build())
				.build();

		Mockito.when(fileUtilHelperMock.createTempSubsPath(Mockito.anyObject())).thenReturn(".");

		WebResult resultSearch = new WebResult(new URL("https://subscene.com/subtitles/title?q=Mockito"), searchResult, false);
		Mockito.when(webRetrieverMock.getWebResult("https://subscene.com/subtitles/title?q=Mockito")).thenReturn(resultSearch);

		//WebResult resultSearchFilename = new WebResult(new URL("https://subscene.com/subtitles/title?q=Mockito.2014"), searchResult, false);
		Mockito.when(webRetrieverMock.getWebResult("https://subscene.com/subtitles/title?q=Mockito.2014")).thenReturn(resultSearch);

		WebResult resultList = new WebResult(new URL("https://subscene.com/subtitles/Mockito"), listResult, false);
		Mockito.when(webRetrieverMock.getWebResult("https://subscene.com/subtitles/Mockito")).thenReturn(resultList);
		Mockito.when(webRetrieverMock.getWebResult("https://subscene.com/subtitles/Mockito.2014")).thenReturn(resultList);

		WebResult resultDownload1 = new WebResult(new URL("https://subscene.com/subtitles/Mockito/1023456"), download1Result, false);
		Mockito.when(webRetrieverMock.getWebResult("https://subscene.com/subtitles/Mockito/1023456")).thenReturn(resultDownload1);

		WebResult resultDownload2 = new WebResult(new URL("https://subscene.com/subtitles/Mockito/1123456"), download2Result, false);
		Mockito.when(webRetrieverMock.getWebResult("https://subscene.com/subtitles/Mockito/1123456")).thenReturn(resultDownload2);
		
		Mockito.when(
				webRetrieverMock.getWebFile("https://subscene.com/subtitle/download?mac=minYVuJCyMyRv2laUi3x4JRZpygROrUboH6SnmAZMHI0BjB2Ect5yTARB0a8KolifMxdZlSEtc-I5wEKPCP7OxtfvN6JYKOmrUtlONkO-4MWpvB-nqHp421TU8WptRiE0", 
						"."))
		.thenReturn(new File("temp1"));
		

		Mockito.when(
				webRetrieverMock.getWebFile("https://subscene.com/subtitle/download?mac=maxYVuJCyMyRv2laUi3x4JRZpygROrUboH6SnmAZMHI0BjB2Ect5yTARB0a8KolifMxdZlSEtc-I5wEKPCP7OxtfvN6JYKOmrUtlONkO-4MWpvB-nqHp421TU8WptRiE0", 
						"."))
		.thenReturn(new File("temp1"));
		
		List<Language> lang = new ArrayList<Language>();
		lang.add(Language.English);
		
		List<SubFile> files = ss.findSubtitles(new MovieOrSeries(m), lang);
		
		assertEquals(2, files.size());
	}

	public ISubFileDownloaderHelper createHelper() {
		return new SubFileDownloaderHelper(settings, webRetrieverMock, movieBuilderFactory, loggerFactoryMock, waiterMock, fileUtilHelperMock);
	}
}
