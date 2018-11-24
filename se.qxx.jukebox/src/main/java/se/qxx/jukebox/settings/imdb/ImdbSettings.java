package se.qxx.jukebox.settings.imdb;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import se.qxx.jukebox.interfaces.IImdbSettings;

public class ImdbSettings implements IImdbSettings {
	private Imdb imdb;
	
	public ImdbSettings() {
	}
	
	public Imdb getImdb() {
		return imdb;
	}

	public void setImdb(Imdb imdb) {
		this.imdb = imdb;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.imdb.IImdbSettings#readSettings()
	 */
	@Override
	public void readSettings() throws IOException, JAXBException {
		readSettingFile();
	}
	
	private void readSettingFile() throws IOException, JAXBException {
		JAXBContext c = JAXBContext.newInstance(Imdb.class);
		Unmarshaller u = c.createUnmarshaller();
		u.setEventHandler(
			    new ValidationEventHandler() {
			        public boolean handleEvent(ValidationEvent event ) {
			            throw new RuntimeException(event.getMessage(),
			                                       event.getLinkedException());
			        }
			});
		
		JAXBElement<Imdb> root = u.unmarshal(new StreamSource(new File("imdb.xml")), Imdb.class);
		this.setImdb(root.getValue());
	}

}
