package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.*;

public class ParserBuilder extends MovieBuilder {

	public ParserBuilder(ISettings settings, IJukeboxLogger log) {
		super(settings, log);
	}

	@Override
	public MovieOrSeries extract(String filepath, String filename) {
		return extractMovieParser(filepath, filename).build();
	}
	
	private ParserTest getParser() {
		return this.getSettings().getParser();
	}
	
	public ParserMovie extractMovieParser(String filepath, String filename) {
		Media md = getMedia(filepath, filename);
		ParserMovie pm = new ParserMovie(md);
		
		String fileNameToMatch = FilenameUtils.getBaseName(md.getFilename());
		
		this.getLog().Info(String.format("Running ParserBuilder on %s", fileNameToMatch));
				
		String stringToProcess =
			removeIgnored(
				removeParenthesis(
					removeInitialParenthesis(fileNameToMatch)));

		String[] tokens = StringUtils.split(stringToProcess, " _.-");
		
		// assume movie name always comes first. The next token after that identifies the end of filename.
		// assume TV episode title is before season and episode specifier
		// assume groupName is the last token in the file (if other tokens exist)
		boolean titleMode = false;
		
		int nrOfTokens = tokens.length;
		
		for (int i=0; i<nrOfTokens;i++) {
			String token = tokens[i];

			// get the tail if we are to use lookahead
			String[] tail = new String[] {}; 
			if (i < nrOfTokens - 1)
				tail = ArrayUtils.subarray(tokens, i + 1, nrOfTokens);
			
			// check if we are on the last or first token
			boolean isFirst = i == 0;
			boolean isLast = i == nrOfTokens - 1;
			
			if (!StringUtils.isEmpty(StringUtils.trim(token))) {
				// check the token
				ParserToken result = checkToken(token, isFirst, isLast, tail);
				
				// parse the token and make recursive calls if needed.
				titleMode = 
					parseToken(
						pm, 
						titleMode, 
						result, 
						result.getRecursiveCount(),
						new ArrayList<>(),
						tail);
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

	private String removeIgnored(String str) {
		for (ParserRegexTest pt : getWordList(ParserType.IGNORED)) {
			Pattern p = Pattern.compile(pt.getRegex(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(str);

			str = m.replaceAll("");

		}

		return str;
	}

	private boolean parseToken(
			ParserMovie pm, 
			boolean titleMode, 
			ParserToken token, 
			int recursiveCount, 
			List<ParserType> ignoreTypes,
			String[] tail) {
			
		ParserType pt = token.getType();
		String resultingToken = token.getResultingToken();
		
		// how do we get a token to be parsed several times if found.
		// I.e. season/episode??.. that is look-ahead.. right?
		// recursive call on the same token, ignoring the first ParserType??
		
		// if we get to an unknown token we switch to title mode
		if (pt.equals(ParserType.UNKNOWN) && !titleMode)
			titleMode = true;
		
		// if we are in title mode and we get to non unknown token
		// we push the title. All other unknown tokens will be ignored
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
			if (pm.getEpisode() == 0)
				pm.setEpisode(Integer.parseInt(resultingToken));
				break;
		case SEASON:
			if (pm.getSeason() == 0)
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
			break;
		}

		if (recursiveCount > 1) {
			recursiveCount--;
			ignoreTypes.add(pt);
			
			ParserToken newToken = 
					checkToken(
						token.getOriginalToken(),
						token.isFirst(),
						token.isLast(),
						ignoreTypes,
						tail);
			
			titleMode = parseToken(pm, titleMode, newToken, recursiveCount, ignoreTypes, tail);
		}
		
		return titleMode;
	}

	private ParserToken checkToken(String token, boolean isFirst, boolean isLast, String[] tail) {
		return checkToken(token, isFirst, isLast, new ArrayList<>(), tail);
	}
	
	private ParserToken checkToken(
			String token, 
			boolean isFirst, 
			boolean isLast, 
			List<ParserType> ignoreTypes,
			String[] tail) {
		
		for (ParserType pt : ParserType.values()) {
			if (!ignoreTypes.contains(pt)) {
				for (ParserRegexTest wt : getWordList(pt)) {
					String result = checkWordType(wt, token, isFirst, isLast);
					if (!StringUtils.isEmpty(result)) {
						return new ParserToken(
								pt, 
								token, 
								result, 
								wt.getRecursiveCountInt(),
								isFirst, 
								isLast);
					}
					else {
						if (wt.getLookaheadInt() > 0 && tail.length > 0 && tail.length >= wt.getLookaheadInt()) {
							// get sub-array of the tail of the number of lookahead elements we need
							// concatenate them into a new token and parse that
							String newToken = token + " " + 
									StringUtils.join(ArrayUtils.subarray(tail, 0, wt.getLookaheadInt()), " ");
							
							String newTokenResult = checkWordType(wt, newToken, isFirst, isLast);
							if (!StringUtils.isEmpty(newTokenResult)) {
								return new ParserToken(
										pt, 
										newToken, 
										newTokenResult, 
										wt.getRecursiveCountInt(),
										isFirst, 
										isLast);
							}
						}
					}
				}
			}
		}
		
		return new ParserToken(ParserType.UNKNOWN, token, token, 1, isFirst, isLast);
	} 
	
	private String checkWordType(ParserRegexTest wt, String token, boolean isFirst, boolean isLast) {
		if (wt != null) {
			if (isTokenConsidered(wt, isFirst, isLast)) {
				if (wt.isRegex()) {
					return checkRegex(token, wt.getRegex(), wt.getGroupInt());
				}
				else {
					return StringUtils.equalsIgnoreCase(wt.getRegex(), token) ? token : StringUtils.EMPTY;
				}
			}
		}
		
		return StringUtils.EMPTY;
	}
	
	private boolean isTokenConsidered(ParserRegexTest wt, boolean isFirst, boolean isLast) {
		return (wt.isFirstToken() && isFirst) || 
				(wt.isLastToken() && isLast) ||
				(!wt.isFirstToken() && !wt.isLastToken());
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

	private List<ParserRegexTest> getWordList(ParserType pt) {
		switch (pt) {
		case IGNORED:
			return this.getParser().getIgnored();
		case EPISODE:
			return this.getParser().getEpisode();
		case FORMAT:
			return this.getParser().getFormatAsParserRegex();
		case LANGUAGE:
			return this.getParser().getLanguageAsParserRegex();
		case OTHER:
			return this.getParser().getOtherAsParserRegex();
		case PART:
			return this.getParser().getParts();
		case SEASON:
			return this.getParser().getSeason();
		case SOUND:
			return this.getParser().getSoundAsParserRegex();
		case TYPE:
			return this.getParser().getTypeAsParserRegex();
		case YEAR:
			return this.getParser().getYear();
		case GROUP:
			return this.getParser().getGroupsAsParserRegex();
		default:
			return new ArrayList<>();
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

	private String removeInitialParenthesis(String string) {
		String ret = handleInitialParenthesis(string, "\\[", "\\]");
		ret = handleInitialParenthesis(ret, "\\(", "\\)");
		return ret;
	}

	private String handleInitialParenthesis(String string, String start, String end) {
		String pattern = String.format("^%s.*?%s", start, end);
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(string);
	
		return m.replaceAll("");
	}	
}
