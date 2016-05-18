package se.qxx.jukebox.settings.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;


public class ParserSettings {
	private static ParserSettings _instance;
	private Parser settings;
	
//	Hashtable<ParserType, List<String>> keywords = new Hashtable<ParserType, List<String>>();

	private ParserSettings() {
//		keywords.put(ParserType.TYPE, new ArrayList<String>());
//		keywords.put(ParserType.FORMAT, new ArrayList<String>());
//		keywords.put(ParserType.SOUND, new ArrayList<String>());
//		keywords.put(ParserType.LANGUAGE, new ArrayList<String>());
//		keywords.put(ParserType.OTHER, new ArrayList<String>());
//		keywords.put(ParserType.PART, new ArrayList<String>());
//		keywords.put(ParserType.SEASON, new ArrayList<String>());
//		keywords.put(ParserType.EPISODE, new ArrayList<String>());
	}
	
	public static ParserSettings getInstance() {
		if (_instance == null) {
			_instance = new ParserSettings();
		}
			
		return _instance;
	}
	
	public static void readSettings() throws IOException, JAXBException {
		getInstance().readSettingFile();
	}
		
//	private void loadList(ParserType type, List<WordType> values) {
//		for (WordType wt : values)
//			keywords.get(type).add(wt.getKey());
//	}
	
//	public ParserType checkToken(String searchString) {
//		for (ParserType type : ParserType.values()) 
//			if (listContains(type, searchString))
//				return type;
//		
//		return ParserType.UNKNOWN;
//	}
//	
//	public boolean listContains(ParserType type, String searchString) {
//		if (this.keywords.containsKey(type)) {
//			for (String listItem : this.keywords.get(type))
//				if (StringUtils.equalsIgnoreCase(listItem, searchString))
//					return true;
//		}
//		return false;
//	}

	private void readSettingFile() throws IOException, JAXBException {
		JAXBContext c = JAXBContext.newInstance(Parser.class);
		Unmarshaller u = c.createUnmarshaller();
		u.setEventHandler(
			    new ValidationEventHandler() {
			        public boolean handleEvent(ValidationEvent event ) {
			            throw new RuntimeException(event.getMessage(),
			                                       event.getLinkedException());
			        }
			});
		
		JAXBElement<Parser> root = u.unmarshal(new StreamSource(new File("parser.xml")), Parser.class);
		this.setSettings(root.getValue());
		
//		this.loadList(ParserType.TYPE, p.getKeywords().getType().getWord());
//		this.loadList(ParserType.FORMAT, p.getKeywords().getFormat().getWord());
//		this.loadList(ParserType.SOUND, p.getKeywords().getSound().getWord());
//		this.loadList(ParserType.LANGUAGE, p.getKeywords().getLanguage().getWord());
//		this.loadList(ParserType.OTHER, p.getKeywords().getOther().getWord());
		
	}

	public Parser getSettings() {
		return settings;
	}

	private void setSettings(Parser settings) {
		this.settings = settings;
	}

}
