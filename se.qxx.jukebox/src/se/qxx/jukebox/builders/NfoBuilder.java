package se.qxx.jukebox.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class NfoBuilder extends MovieBuilder {
	
	
	@Override
	public Movie extractMovie(String filepath, String filename) {
		Movie m = null;
		try {
			String filenameWithoutExt = FilenameUtils.getBaseName(filename);
			File f = new File(String.format("%s/%s.nfo", filepath, filenameWithoutExt));
			if (f.exists()) {
				NFOScanner scanner = new NFOScanner(f);
				List<NFOLine> lines = scanner.scan();			
				
				
				String 	title = "",  	
						type = "",
						format = "", 
						sound = "", 
						language = "", 
						imdbUrl = "";
				
				int year = 0;
				int idRating = 0;
				int matches = 0;
				int maxGroupMatch = 0;
				
				for (NFOLine line : lines) {
					switch (line.getType()) {
					case Title:
						if (StringUtils.isEmpty(title))  {
							title = line.getValue();
							matches++;
						}
						break;
					case Release:
						if (year == 0) {
							year = extractYear(line.getValue());
							matches++;
						}
						break;
					case Audio:
						if (StringUtils.isEmpty(sound)) {
							sound = line.getValue();
							matches++;
						}
						break;
					case Video:
						if (StringUtils.isEmpty(type)) {
							type = line.getValue();
							matches++;
						}
						break;
					case Language:
						if (StringUtils.isEmpty(language)) {
							language = line.getValue();
							matches++;
						}
						break;
					}
				}
				
				idRating = Math.round(100 * matches / 7);
				
				//If we find an imdb url then increase rating
				if (!StringUtils.isEmpty(imdbUrl))
					idRating *= 10;
				
				Movie movie = Movie.newBuilder()
						.setID(-1)
						.setFilename(filename)
						.setFilepath(filepath)
						.setTitle(title)
						.setYear(year)
						.setType(type)
						.setSound(sound)
						.setLanguage(language)
						.setIdentifier(Identifier.NFO)
						.setIdentifierRating(idRating)
						.setImdbUrl(imdbUrl)
						.build();

				return movie;
			}
		} catch (Exception e) {
		}
		
		return m; 
	}
	
	private int extractYear(String releaseDate) {
		Pattern p = Pattern.compile("[20|19]\\d{2}");
		Matcher m = p.matcher(releaseDate);
		
		if (m.find())
			return Integer.parseInt(m.group());
		else
			return 0;
	}
}
