package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcController;

import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleRequestType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.servercomm.JukeboxRpcServerConnection;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.webserver.StreamingFile;

public class TestServerComm {
	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	private Settings settings;
	private Log log;
	
	@Mock LoggerFactory loggerFactoryMock;
	@Mock IDatabase dbMock;
	@Mock IDistributor distributorMock;
	@Mock ISubtitleDownloader subtitleDownloaderMock;
	@Mock IMovieIdentifier movieIdentifierMock;
	@Mock IStreamingWebServer webServerMock;
	@Mock RpcController controller;
	@Mock IExecutor executor;
	@Mock IUtils utilsMock;
	
	@Before
	public void initialize() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		
		log = new Log(settings, LogType.NONE);
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		
	}
	
	@Test
	public void TestStartMovieChromecast() throws UnsupportedEncodingException {
		JukeboxRpcServerConnection conn = 
			new JukeboxRpcServerConnection(settings, dbMock, distributorMock, 
					subtitleDownloaderMock, movieIdentifierMock, loggerFactoryMock, executor, utilsMock, webServerMock);

		Subtitle sub1 = Subtitle.newBuilder()
				.setID(100)
				.setFilename("ACB1.srt")
				.setMediaIndex(0)
				.setLanguage("SE")
				.setRating(Rating.ExactMatch)
				.setTextdata(ByteString.copyFrom("This is a subtitle test AAA", "iso-8859-1"))
				.setDescription("ABC")
				.build();
		
		Subtitle sub2 = Subtitle.newBuilder()
				.setID(110)
				.setFilename("ACB2.srt")
				.setMediaIndex(0)
				.setLanguage("SE")
				.setRating(Rating.ExactMatch)
				.setTextdata(ByteString.copyFrom("This is a subtitle test BBB", "iso-8859-1"))
				.setDescription("ABC")
				.build();
		
		Media media = Media.newBuilder()
				.setID(10)
				.setFilename("ABC.mp4")
				.setFilepath(".")
				.setIndex(0)
				.addSubs(sub1)
				.addSubs(sub2)
				.setDownloadComplete(true)
				.build();
		
		Movie movie = Movie.newBuilder()
				.setID(1)
				.setTitle("This is a movie title")
				.setIdentifiedTitle("This is a movie title identified")
				.addMedia(media)
				.build();
		
		
		//when(webServerMock.getIpAddress()).thenReturn("127.0.0.1");
		//when(webServerMock.getListeningPort()).thenReturn(8001);
		
		when(dbMock.getMovie(1)).thenReturn(movie);
		when(webServerMock.registerFile(any(Media.class))).thenReturn(new StreamingFile("http://127.0.0.1:8001/stream10.mp4", "media/mp4"));
		when(webServerMock.registerSubtitle(sub1, SubtitleRequestType.WebVTT)).thenReturn(new StreamingFile("http://127.0.0.1:8001/sub100.vtt", "media/vtt"));
		when(webServerMock.registerSubtitle(sub2, SubtitleRequestType.WebVTT)).thenReturn(new StreamingFile("http://127.0.0.1:8001/sub110.vtt", "media/vtt"));
		
		JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
				.setMovieOrEpisodeId(1)
				.setPlayerName("Chromecast")
				.setRequestType(RequestType.TypeMovie)
				.build();
		
		conn.startMovie(controller, request, (response) -> {
			assertEquals(2, response.getSubtitleList().size());
		});
	}
}
