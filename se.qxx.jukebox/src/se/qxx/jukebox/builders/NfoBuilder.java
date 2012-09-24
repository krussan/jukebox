package se.qxx.jukebox.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class NfoBuilder extends MovieBuilder {
	
	
	@Override
	public Movie extractMovie(String filepath, String filename) {
		Movie m = null;
		try {
			String filenameWithoutExt = FilenameUtils.getBaseName(filename);
			File f = new File(String.format("%s/%s.nfo", filepath, filename));
			if (f.exists()) {
				NFOScanner scanner = new NFOScanner(f);
				List<NFOLine> lines = scanner.scan();

			}
		} catch (Exception e) {
		}
		
		return m; 
	}
}
