package se.qxx.jukebox.builders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import se.qxx.jukebox.builders.exceptions.SeriesNotSupportedException;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.INFOScanner;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.watcher.ExtensionFileFilter;

public class NfoBuilder extends MovieBuilder {
	
	public NfoBuilder(ISettings settings, IJukeboxLogger log) {
		super(settings, log);
	}

	@Override
	public MovieOrSeries extract(String filepath, String filename) {
		Movie m = null;
		this.getLog().Debug(String.format("NfoBuilder :: filepath :: %s", filepath));
		this.getLog().Debug(String.format("NfoBuilder :: filename :: %s", filename));

		try {
			File nfoFile = findNfoFile(filepath, filename);
			
			if (nfoFile != null) {			
				this.getLog().Debug(String.format("NfoBuilder :: Opening file :: %s", nfoFile.getAbsolutePath()));
			
				INFOScanner scanner = new NFOScanner(this.getLog(), nfoFile);
				List<NFOLine> lines = scanner.scan();
				
				String 	title = "",  	
						type = "",
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
						case AspectRatio:
							break;
						case Duration:
							break;
						case Format:
							break;
						case FrameRate:
							break;
						case Genre:
							break;
						case Ignore:
							break;
						case Presents:
							break;
						case Resolution:
							break;
						case SeriesInfo:
							break;
						case Subtitles:
							break;
						default:
							break;
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
			this.getLog().Debug("Series found. Ignoring!");
		}
		catch (Exception e) {
			this.getLog().Error("NfoBuilder :: Error", e);
		}
		
		if (m==null)
			this.getLog().Debug("No movie found");
		
		return new MovieOrSeries(m);
	}

	protected File findNfoFile(String filepath, String filename) {
		String filenameWithoutExt = FilenameUtils.getBaseName(filename);
		ExtensionFileFilter eff = new ExtensionFileFilter();
		eff.addExtension("nfo");
		this.getLog().Debug(String.format("NfoBuilder :: finding files in :: %s", filepath));
		
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
	
	
	private ArrayList<String> getGroupsToCheck() {
		ArrayList<String> groupsToCheck = new ArrayList<String>();
		groupsToCheck.add("title");
		groupsToCheck.add("year");
		groupsToCheck.add("type");
		groupsToCheck.add("format");
		groupsToCheck.add("sound");
		groupsToCheck.add("language");
		groupsToCheck.add("group");
		return groupsToCheck;
	}

}
