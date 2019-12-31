package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.protobuf.ByteString;

import fr.noop.subtitle.model.SubtitleParsingException;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.ImdbSettings;
import se.qxx.jukebox.settings.ParserSettings;
import se.qxx.jukebox.subtitles.SubtitleFileWriter;

public class TestSubtitleFileWriter {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	ISettings settings;
	@Mock IDatabase dbMock;
	IJukeboxLogger log;
	
	@Before
	public void initialize() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		
		log = new Log(settings, LogType.NONE);
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		
	}
	
	@After
	public void tearDown() {
	}

	@Test
	public void TestGetStreamFile() throws UnsupportedEncodingException {
		SubtitleFileWriter writer = new SubtitleFileWriter(loggerFactoryMock);

		Subtitle sub1 = Subtitle.newBuilder()
				.setID(100)
				.setFilename("ACB1.srt")
				.setMediaIndex(0)
				.setLanguage("SE")
				.setRating(Rating.ExactMatch)
				.setTextdata(ByteString.copyFrom("This is a subtitle test AAA", "iso-8859-1"))
				.setDescription("ABC")
				.build();

		File file = writer.getTempFile(sub1, "vtt");
		
		assertEquals("sub100.ACB1.vtt", file.getName());
	}
	
	
	@Test
	public void TestWriteSrtFile_does_not_create_double_newlines() throws IOException, SubtitleParsingException {
		SubtitleFileWriter writer = new SubtitleFileWriter(loggerFactoryMock);

		String subtitleData = readResource("TestSubtitle.txt");
		
		Subtitle sub1 = Subtitle.newBuilder()
				.setID(100)
				.setFilename("ACB1.srt")
				.setMediaIndex(0)
				.setLanguage("SE")
				.setRating(Rating.ExactMatch)
				.setTextdata(ByteString.copyFrom(subtitleData, "iso-8859-1"))
				.setDescription("ABC")
				.build();
		
		File file = writer.getTempFile(sub1, "srt");
		
		writer.writeSubtitleToFileConvert(sub1, file);
		
		try (FileInputStream fis = new FileInputStream(file)) {
			try (Reader r = new InputStreamReader(fis)) {
				try (BufferedReader br = new BufferedReader(r)) {
					String line = null;
					int c = 0;
					while((line = br.readLine()) != null) {
						if (StringUtils.isEmpty(line)) {
							c++;
							if (c==2)
								fail("More than one empty lines found");
						}
						else {
							c = 0;
						}
					}
				}
			}
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

}
