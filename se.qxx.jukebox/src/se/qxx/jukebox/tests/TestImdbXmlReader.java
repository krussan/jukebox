package se.qxx.jukebox.tests;

import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;


public class TestImdbXmlReader {
	public static void main(String[] args) throws Exception {
			Settings.initialize();
			
			System.out.println(Settings.imdb().getSearchPatterns());
			
			System.out.println(Settings.imdb().getSearchUrl());
			
			System.out.println(ImdbSettings.get());
			
	}
}
