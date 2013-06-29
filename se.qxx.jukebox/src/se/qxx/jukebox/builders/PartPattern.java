package se.qxx.jukebox.builders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Episodes.Pattern.Groups.Group;

/**
 * Test if the filename is part of a set of media. i.e. CD1, CD2
 * The regex test is read from settings. 
 * 
 * @author Chris
 *
 */
public class PartPattern {
	private String resultingFilename;
	private int part = 1;
	private int partIndexInFilename = -1;
	private String prefixFilename;
	private int season;
	private int episode;
	private boolean tvEpisode = false;

	public String getResultingFilename() {
		return resultingFilename;
	}
	private void setResultingFilename(String resultingFilename) {
		this.resultingFilename = resultingFilename;
	}
	public int getPart() {
		return part;
	}
	private void setPart(int part) {
		this.part = part;
	}
	public int getPartIndexInFilename() {
		return partIndexInFilename;
	}
	private void setPartIndexInFilename(int partIndexInFilename) {
		this.partIndexInFilename = partIndexInFilename;
	}
	public String getPrefixFilename() {
		return prefixFilename;
	}
	private void setPrefixFilename(String prefixFilename) {
		this.prefixFilename = prefixFilename;
	}
	
	public int getSeason() {
		return season;
	}
	private void setSeason(int season) {
		this.season = season;
	}
	public int getEpisode() {
		return episode;
	}
	private void setEpisode(int episode) {
		this.episode = episode;
	}
	
	public boolean isTvEpisode() {
		return tvEpisode;
	}
	private void setIsTvEpisode(boolean tvEpisode) {
		this.tvEpisode = tvEpisode;
	}	
	
	public PartPattern(String filename) {
		this.setResultingFilename(filename);
		parseParts(this.getResultingFilename());
		parseTvEpisode(this.getResultingFilename());		
	}
	
	private void parseParts(String filename) {
		String partPattern = StringUtils.trim(Settings.get().getStringSplitters().getParts().getPartPattern().getRegex());
		int partGroup = Settings.get().getStringSplitters().getParts().getPartPattern().getGroupPart();
		
		Pattern p = Pattern.compile(partPattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(filename);

		Log.Debug(String.format("PartPattern :: Matching filename :: %s :: Regex :: %s", filename, partPattern), LogType.FIND);

		String partIdentifier = StringUtils.EMPTY;
		int partIndex = -1;
		String resultingFilename = filename;
		
		if (m.find()) {
			partIndex = m.start(partGroup);
			partIdentifier = m.group(partGroup);
			Log.Debug(String.format("MovieBuilder :: This file appears to be a part %s of a movie :: %s", partIdentifier, filename), LogType.FIND);
			
			resultingFilename = m.replaceFirst("");			
		}
		
		this.parsePartPattern(filename, resultingFilename, partIdentifier, partIndex);		
	}
	
	private void parseTvEpisode(String filename) {
		for(se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Episodes.Pattern p 
				: Settings.get().getStringSplitters().getEpisodes().getPattern()) {

			Pattern regexPattern = Pattern.compile(StringUtils.trim(p.getRegex()), Pattern.CASE_INSENSITIVE);
			Matcher matcher = regexPattern.matcher(filename);
			
			
			if (matcher.matches()) {
				for(Group g : p.getGroups().getGroup()) {
					if (StringUtils.equalsIgnoreCase("season", g.getProperty()))
						this.setSeason(Integer.parseInt(matcher.group(g.getId())));
					if (StringUtils.equalsIgnoreCase("episode", g.getProperty()))
						this.setEpisode(Integer.parseInt(matcher.group(g.getId())));
				}
				
				this.setIsTvEpisode(true);
				
				this.setResultingFilename(matcher.replaceFirst(StringUtils.EMPTY));
				break;
			}
		}		
	}
	
	private void parsePartPattern(String originalFilename, String resultingFilename, String partIdentifier, int partIndex) {
		this.setResultingFilename(resultingFilename);		
		this.setPrefixFilename(originalFilename);
		if (!StringUtils.isEmpty(partIdentifier)) {
			try {
				int intPart = Integer.parseInt(partIdentifier);
				this.setPart(intPart);
				this.setPartIndexInFilename(partIndex);
				if (partIndex > 0)
					this.setPrefixFilename(originalFilename.substring(0, partIndex - 2));
			}
			catch (NumberFormatException e) {
			}
		}
	}
}
