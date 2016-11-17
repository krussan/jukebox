package se.qxx.jukebox.builders;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.exceptions.SeriesNotSupportedException;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.watcher.ExtensionFileFilter;
import se.qxx.jukebox.domain.MovieOrSeries;

public class NfoBuilder extends MovieBuilder {
	
	
	
	@Override
	public MovieOrSeries extract(String filepath, String filename) {
		Movie m = null;
		Log.Debug(String.format("NfoBuilder :: filepath :: %s", filepath), LogType.FIND);
		Log.Debug(String.format("NfoBuilder :: filename :: %s", filename), LogType.FIND);

		try {
			File nfoFile = findNfoFile(filepath, filename);
			
			if (nfoFile != null) {			
				Log.Debug(String.format("NfoBuilder :: Opening file :: %s", nfoFile.getAbsolutePath()), LogType.FIND);
			
				NFOScanner scanner = new NFOScanner(nfoFile);
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
				
				for (NFOLine line : lines) {
					if (!isEmptyLine(line)) {
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
						case IMDBLink:
							if (StringUtils.isEmpty(imdbUrl)) {
								imdbUrl = line.getValue();
							}
						}
					}
				}				
				idRating = Math.round(100 * matches / getGroupsToCheck().size());
								
				m = Movie.newBuilder()
						.setID(-1)
						.addMedia(getMedia(filepath, filename))
						.setTitle(title)
						.setYear(year)
						.setType(type)
						.setSound(sound)
						.setLanguage(language)
						.setIdentifier(Identifier.NFO)
						.setIdentifierRating(idRating)
						.setImdbUrl(imdbUrl)
						.setIdentifiedTitle(title)
//						.setSubtitleRetreiveResult(0)
						.build();
				
			}
		} 
		catch (SeriesNotSupportedException sns) {
			Log.Debug("Series found. Ignoring!", LogType.FIND);
		}
		catch (Exception e) {
			Log.Error("NfoBuilder :: Error", LogType.FIND, e);
		}
		
		if (m==null)
			Log.Debug("No movie found", LogType.FIND);
		
		return new MovieOrSeries(m);
	}

	protected File findNfoFile(String filepath, String filename) {
		String filenameWithoutExt = FilenameUtils.getBaseName(filename);
		ExtensionFileFilter eff = new ExtensionFileFilter();
		eff.addExtension("nfo");
		Log.Debug(String.format("NfoBuilder :: finding files in :: %s", filepath), LogType.FIND);
		
		File dir = new File(filepath);
		File[] files = dir.listFiles(eff);
		
		String nfoFilename = StringUtils.EMPTY;
		
		if (files != null) {
			for (File f : files) {
				String nfoFullPathFilename = f.getAbsolutePath();
				
				if (FilenameUtils.getExtension(nfoFullPathFilename).equalsIgnoreCase("nfo")) {
					nfoFilename = FilenameUtils.getBaseName(nfoFullPathFilename);
					if (StringUtils.startsWithIgnoreCase(nfoFilename, filenameWithoutExt))
						return f;
				}
			}
		}
		
		
		return null;
	}
	
	private int extractYear(String releaseDate) {
		Pattern p = Pattern.compile("(20|19)\\d{2}");
		Matcher m = p.matcher(releaseDate);
		
		if (m.find())			
			return Integer.parseInt(m.group());
		else 
			return 0;
		
	}
	
	private boolean isEmptyLine(NFOLine line) {
		Pattern p = Pattern.compile("(\\-+)|(\\s+)|(\\*+)");
		Matcher m = p.matcher(line.getValue());
		
		return m.matches();
	}
}
