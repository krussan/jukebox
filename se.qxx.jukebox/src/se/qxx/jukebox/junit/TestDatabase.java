package se.qxx.jukebox.junit;

import org.junit.Before;
import org.junit.Test;

import se.qxx.jukebox.DB;

public class TestDatabase {
	
	@Before public void setup() {
		DB.setDatabase("jukebox_test.db");
	}
	
	@Test public void getMovieByFilename() {
		
	}
	
	@Test public void addMovie() {
		
	}
	
	@Test public void getMovieById() {
		
	}
	
	@Test public void addSubtitles() {
		
	}
	
	@Test public void addMovieToSubtitleQueue() {
		
	}
	
	@Test public void setSubtitleDownloaded() {
		
	}
	
	@Test public void getSubtitleQueue() {
		
	}
	
	@Test public void getSubtitles() {
		
	}
	
	@Test public void subFileExistsInDb() {
		
	}
	
	@Test public void getVersion() {
		
	}
	
	@Test public void setVersion() {
		
	}
	
	@Test public void purge() {
		
	}
	
	@Test public void purgeSubs() {
		
	}
}
