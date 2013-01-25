package se.qxx.jukebox.builders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Episodes.Pattern.Groups.Group;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.parser.ParserType;

public class ParserBuilder {
	
	public ParserMovie extractMovie(String filepath, String filename) {
		ParserMovie pm = new ParserMovie();
		pm.setFilename(filename);
		String stringToProcess = removeParenthesis(filename);
		String[] tokens = StringUtils.split(stringToProcess, " _.-");
		
		// assume movie name always comes first. The next token after that identifies the end of filename.
		
		
		for (String token : tokens) {
			if (!StringUtils.isEmpty(StringUtils.trim(token))) {
				ParserType pt = Settings.parser().checkToken(token);
				Pair<Integer, Integer> tvEpisode = getTvEpisode(token);

				if (!pt.equals(ParserType.UNKNOWN)) 
					pm.pushTitle();

				if (tvEpisode != null) {
					pm.setSeason(tvEpisode.getLeft());
					pm.setEpisode(tvEpisode.getRight());
					pm.pushTitle();
				}
				else {
					if (isYear(token)) {
						pm.setYear(Integer.parseInt(token));
						pm.pushTitle();
					}
					else {					
						// All other unknown tokens (after first recognized tokens) are ignored
						switch (pt) {
						case FORMAT:
							pm.getFormats().add(token);
							break;
						case LANGUAGE:
							pm.getLanguages().add(token);
							break;
						case TYPE:
							pm.getTypes().add(token);
							break;
						case SOUND:
							pm.getSounds().add(token);
							break;
						case OTHER:
							pm.getOthers().add(token);
							break;
						case UNKNOWN:
							pm.addMovieNameToken(token);
	//						else
	//							Log.Info(String.format("ParserBuilder :: Token %s ignored", token), LogType.FIND);
							break;
						}
					}
				}
			}
		}
		
		pm.pushTitle();
		
		return pm;
	}

	private String removeParenthesis(String string) {
		String[] removeChars = {
			"(", ")", "[", "]"
		};
		
		String ret = string;
		for (String searchString : removeChars)
			ret = StringUtils.replace(ret, searchString, " ");

		return ret;
	}	
	
	private boolean isYear(String token) {
		Pattern p = Pattern.compile("^(19|20)\\d{2}$");
		Matcher m = p.matcher(token);
		return m.find();
	}
		
	private Pair<Integer, Integer> getTvEpisode(String token) {
		for(se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Episodes.Pattern p 
				: Settings.get().getStringSplitters().getEpisodes().getPattern()) {

			Pattern regexPattern = Pattern.compile(StringUtils.trim(p.getRegex()), Pattern.CASE_INSENSITIVE);
			Matcher matcher = regexPattern.matcher(token);
			
			if (matcher.find()) {
				int season = Integer.parseInt(getProperty(p, matcher, "season"));
				int episode = Integer.parseInt(getProperty(p, matcher, "episode"));
				
				return new ImmutablePair<Integer, Integer>(season, episode);
			}
		}
		
		return null;
	}

	private String getProperty(se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Episodes.Pattern p, Matcher matcher, String property) {
		int groupId = getGroupIndex(p, property);

		if (groupId > 0)
			if (matcher.group(groupId) != null)
				return StringUtils.trim(Util.parseAwaySpace(matcher.group(groupId)));

		return StringUtils.EMPTY;
	}

	private int getGroupIndex(se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Episodes.Pattern p, String property) {
		for(Group g : p.getGroups().getGroup()) {
			if (StringUtils.equalsIgnoreCase(g.getProperty(), property))
				return g.getId();
		}

		return -1;
	}

	
}
