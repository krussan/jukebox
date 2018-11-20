package se.qxx.jukebox.imdb;

import org.jsoup.nodes.Document;

import se.qxx.jukebox.interfaces.IIMDBParser;

public interface IMDBParserFactory {
	IIMDBParser create(Document doc);
}
