package se.qxx.jukebox.settings;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.interfaces.IParserSettings;


@Singleton
public class ParserSettings implements IParserSettings {
	private Parser settings;
	
	@Inject
	public ParserSettings() {
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.IParserSettings#readSettings()
	 */
	@Override
	public void readSettings() throws IOException, JAXBException {
		readSettingFile();
	}
		

	private void readSettingFile() throws JAXBException {
		JAXBContext c = JAXBContext.newInstance(Parser.class);
		Unmarshaller u = c.createUnmarshaller();
		u.setEventHandler(
				event -> {
					throw new RuntimeException(event.getMessage(),
											   event.getLinkedException());
				});
		
		JAXBElement<Parser> root = u.unmarshal(new StreamSource(new File("parser.xml")), Parser.class);
		this.setSettings(root.getValue());
		
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.IParserSettings#getSettings()
	 */
	@Override
	public Parser getSettings() {
		return settings;
	}

	private void setSettings(Parser settings) {
		this.settings = settings;
	}

}
