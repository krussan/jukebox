package se.qxx.jukebox.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class NfoImdbBuilder extends MovieBuilder {

	@Override
	public Movie extractMovie(String filepath, String filename) {
		Movie m = null;
		try {
			File f = new File(String.format("%s/%s.nfo", filepath, filename));
			if (f.exists()) {
				BufferedReader bf = new BufferedReader(new FileReader(f));
				String line;
				while ((line = bf.readLine()) != null) {
					Pattern p = Pattern.compile("http://www.imdb.com/)
				}
			}
		} catch (Exception e) {
		}
		
		return m;
	}
}
