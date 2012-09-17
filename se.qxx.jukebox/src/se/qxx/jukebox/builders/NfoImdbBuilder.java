package se.qxx.jukebox.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class NfoImdbBuilder extends MovieBuilder {
	
	public final String[] releaseKeywords = {"release", "release name"};
	public final String[] titleKeywords = {"title"};
	public final String[] audioKeywords = {"audio"};
	public final String[] languageKeywords = {"language"};
	public final String[] videoKeywords = {"video"};
	public final String[] resolutionKeywords = {"resolution"};
	public final String[] aspectRatioKeywords = {"aspect ratio"};
	public final String[] subtitlesKeywords = {"subs", "subtitles"};
	public final String[] durationKeywords = {"runtime", "duration", "run time"};
	public final String[] genreKeywords = {"genre"};
	public final String[] formatKeywords = {"format"};
	public final String[] framerateKeywords = {"framerate", "frame rate"};
	
	@Override
	public Movie extractMovie(String filepath, String filename) {
		Movie m = null;
		/*try {
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
		*/
		return m; 
	}
}
