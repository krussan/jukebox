package se.qxx.jukebox.factories;

import org.jsoup.nodes.Document;

import se.qxx.jukebox.interfaces.IIMDBParser;


public interface IMDBParserFactory {
	IIMDBParser create(Document doc);
}
