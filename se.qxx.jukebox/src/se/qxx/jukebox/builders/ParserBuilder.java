package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.parser.Parser.Keywords;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.settings.parser.ParserType;
import se.qxx.jukebox.settings.parser.WordType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ParserBuilder extends MovieBuilder {

	@Override
	public MovieOrSeries extract(String filepath, String filename) {
		return extractMovieParser(filepath, filename).build();
	}
	
	public ParserMovie extractMovieParser(String filepath, String filename) {
		Media md = getMedia(filepath, filename);
		ParserMovie pm = new ParserMovie(md);
		
		String fileNameToMatch = FilenameUtils.getBaseName(md.getFilename());
		
		String stringToProcess = removeParenthesis(fileNameToMatch);
		String[] tokens = StringUtils.split(stringToProcess, " _.-");
		
		// assume movie name always comes first. The next token after that identifies the end of filename.
		// assume TV episode title is before season and episode specifier
		// assume groupName is the last token in the file (if other tokens exist)
		boolean titleMode = false;
		
		for (String token : tokens) {
			if (!StringUtils.isEmpty(StringUtils.trim(token))) {
				ParserToken result = checkToken(token);	
				titleMode = 
					parseToken(
						pm, 
						titleMode, 
						result, 
						result.getRecursiveCount(), 
						new ArrayList<ParserType>());
			}
		}
		
		pm.pushTitle();
		
		if (pm.getTitles().size() > 0)
			pm.setMovieName(pm.getTitles().get(0));
		
		// if one of the tokens have been identified as a group name then we don't overwrite
		if (pm.getTitles().size() > 1 && StringUtils.isEmpty(pm.getGroupName()))
			pm.setGroupName(pm.getTitles().get(pm.getTitles().size() - 1));
		
		return pm;
	}

	private boolean parseToken(
			ParserMovie pm, 
			boolean titleMode, 
			ParserToken token, 
			int recursiveCount, 
			List<ParserType> ignoreTypes) {
			
		ParserType pt = token.getType();
		String resultingToken = token.getResultingToken();
		
		// how do we get a token to be parsed several times if found.
		// I.e. season/episode??.. that is look-ahead.. right?
		// recursive call on the same token, ignoring the first ParserType??
		
		// if we get to an unknown token we switch to titlemode
		if (pt.equals(ParserType.UNKNOWN) && !titleMode)
			titleMode = !titleMode;
		
		// if we are in titlemode and we get to non unknown token
		// we push the title. All other unkown tokens will be ignored 
		if (!pt.equals(ParserType.UNKNOWN) && titleMode)
			pm.pushTitle();
		
		
		switch (pt) {
		case FORMAT:
			pm.getFormats().add(resultingToken);
			break;
		case LANGUAGE:
			pm.getLanguages().add(resultingToken);
			break;
		case TYPE:
			pm.getTypes().add(resultingToken);
			break;
		case SOUND:
			pm.getSounds().add(resultingToken);
			break;
		case OTHER:
			pm.getOthers().add(resultingToken);
			break;
		case PART:
			pm.setPart(Integer.parseInt(resultingToken));
			break;
		case EPISODE:
			pm.setEpisode(Integer.parseInt(resultingToken));;
			break;
		case SEASON:
			pm.setSeason(Integer.parseInt(resultingToken));
			break;
		case YEAR:
			pm.setYear(Integer.parseInt(resultingToken));
			break;
		case GROUP:
			pm.setGroupName(resultingToken);
			break;
		case UNKNOWN:
			pm.addMovieNameToken(resultingToken);
			break;
		default:
			throw new NotImplementedException();
		}

		if (recursiveCount > 1) {
			recursiveCount--;
			ignoreTypes.add(pt);
			ParserToken newToken = checkToken(token.getOriginalToken(), ignoreTypes);
			titleMode = parseToken(pm, titleMode, newToken, recursiveCount, ignoreTypes);
		}
		
		return titleMode;
	}

	private ParserToken checkToken(String token) {
		return checkToken(token, new ArrayList<ParserType>());
	}
	
	private ParserToken checkToken(String token, List<ParserType> ignoreTypes) {
		for (ParserType pt : ParserType.values()) {
			if (!ignoreTypes.contains(pt)) {
				for (WordType wt : getWordList(pt)) {
					String result = checkWordType(wt, token);
					if (!StringUtils.isEmpty(result))
						return new ParserToken(pt, token, result, wt.getRecursiveCount());
				}
			}
		}
		
		return new ParserToken(ParserType.UNKNOWN, token, token, 1);
	} 
	
	private String checkWordType(WordType wt, String token) {
		if (wt != null) {
			if (wt.isRegex()) {
				return checkRegex(token, wt.getKey(), wt.getGroup());
			}
			else {
				return StringUtils.equalsIgnoreCase(wt.getKey(), token) ? token : StringUtils.EMPTY;
			}
		}
		
		return StringUtils.EMPTY;
	}
 
	private String checkRegex(String token, String regex, int group) {
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(token);
		if (m.matches()) {
			if (group <= m.groupCount())
				return m.group(group);
		}
		
		return StringUtils.EMPTY;
		
	}

	private List<WordType> getWordList(ParserType pt) {
		switch (pt) {
		case EPISODE:
			return ParserSettings.getInstance().getSettings().getKeywords().getEpisode().getWord();	
		case FORMAT:
			return ParserSettings.getInstance().getSettings().getKeywords().getFormat().getWord();
		case LANGUAGE:
			return ParserSettings.getInstance().getSettings().getKeywords().getLanguage().getWord();
		case OTHER:
			return ParserSettings.getInstance().getSettings().getKeywords().getOther().getWord();
		case PART:
			return ParserSettings.getInstance().getSettings().getKeywords().getParts().getWord();
		case SEASON:
			return ParserSettings.getInstance().getSettings().getKeywords().getSeason().getWord();
		case SOUND:
			return ParserSettings.getInstance().getSettings().getKeywords().getSound().getWord();
		case TYPE:
			return ParserSettings.getInstance().getSettings().getKeywords().getType().getWord();
		case YEAR:
			return ParserSettings.getInstance().getSettings().getKeywords().getYear().getWord();
		case GROUP:
			return ParserSettings.getInstance().getSettings().getKeywords().getGroups().getWord();		
		default:
			return new ArrayList<WordType>();
		}
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
}
