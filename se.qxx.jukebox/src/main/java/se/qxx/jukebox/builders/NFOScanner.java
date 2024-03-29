package se.qxx.jukebox.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.builders.exceptions.SeriesNotSupportedException;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.INFOScanner;

public class NFOScanner implements INFOScanner {
	private final String[] presentsKeywords = {"presents"};
	private final String[] releaseKeywords = {"release", "release name"};
	private final String[] titleKeywords = {"title", "presents", "original title"};
	private final String[] audioKeywords = {"audio"};
	private final String[] languageKeywords = {"language"};
	private final String[] videoKeywords = {"video"};
	private final String[] resolutionKeywords = {"resolution"};
	private final String[] aspectRatioKeywords = {"aspect ratio"};
	private final String[] subtitlesKeywords = {"subs", "subtitles"};
	private final String[] durationKeywords = {"runtime", "duration", "run time"};
	private final String[] genreKeywords = {"genre"};
	private final String[] formatKeywords = {"format"};
	private final String[] framerateKeywords = {"framerate", "frame rate"};
	
	private final char[] separators = {':', '['};
	
	private final String acceptedCharacters = "abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZåäö1234567890./\\-_\"\'()[]%$+-*/: ";
	private File nfoFile;
	private IJukeboxLogger log;

	public NFOScanner(IJukeboxLogger log, File file) { 
		this.setLog(log);
		this.setNfoFile(file);
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.builders.INFOScanner#getNfoFile()
	 */
	@Override
	public File getNfoFile() {
		return nfoFile;
	}

	private void setNfoFile(File nfoFile) {
		this.nfoFile = nfoFile;
	}

	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.builders.INFOScanner#scan()
	 */
	@Override
	public List<NFOLine> scan() throws SeriesNotSupportedException {
		ArrayList<NFOLine> list = new ArrayList<NFOLine>();
		
		InputStreamReader r = null;
		BufferedReader bf = null;
		
		boolean presentingMode = false;
		
		try {
			
			r = new InputStreamReader(new FileInputStream(this.nfoFile), "ISO-8859-1");
			bf = new BufferedReader(r);
			
			
			StringBuilder sb  = new StringBuilder();
			int d;
			char c;
			while ((d = bf.read()) != -1) {
				c = (char) d;
				
				if (isAcceptedCharacter(c))
					sb.append(c);
				
				if (c == '\n') {
					String line = sb.toString();
					
					if (presentingMode) {
						if (!StringUtils.isEmpty(StringUtils.trim(line))) {
							this.getLog().Debug(String.format("NfoScanner :: Presenting mode :: %s", line));
							list.add(new NFOLine(NFOClass.Title, StringUtils.trim(line), line));
							presentingMode = false;
						}
						else {
							list.add(new NFOLine(NFOClass.Ignore, line, line));
						}
					}
					else {
						NFOLine nfoLine = parseLine(sb.toString());
						
						if (nfoLine.getType() == NFOClass.SeriesInfo)
							throw new SeriesNotSupportedException();
						
						if (nfoLine != null) {
							list.add(nfoLine);
							if (nfoLine.getType() == NFOClass.Presents)
								presentingMode = true;
						}
					}
					sb = new StringBuilder();
				}
			}
		} catch (Exception e) {
			this.getLog().Error(String.format("NFOScanner error while parsing %s:: ", this.nfoFile.getName()), e);
		}
		finally {
			try {
				if (bf!=null)
					bf.close();
				
				if (r!=null)
					r.close();
			} catch (IOException e) {
			}
		}
		
		return list;
	}

	private NFOLine parseLine(String line) {
		NFOLine nfo = null;
		
		// line has been parsed and only accepted characters are present
		nfo = checkImdb(line);
		
		if (nfo == null)
			nfo = checkLine(line, this.presentsKeywords, NFOClass.Presents, false);
		
		if (nfo == null)
			nfo = checkLine(line, this.titleKeywords, NFOClass.Title);

		if (nfo == null)
			nfo = checkLine(line, this.releaseKeywords, NFOClass.Release);

		if (nfo == null)
			nfo = checkLine(line, this.audioKeywords, NFOClass.Audio);
		
		if (nfo == null)
			nfo = checkLine(line, this.languageKeywords, NFOClass.Language);
		
		if (nfo == null)
			nfo = checkLine(line, this.videoKeywords, NFOClass.Video);

		if (nfo == null)
			nfo = checkLine(line, this.resolutionKeywords, NFOClass.Resolution);

		if (nfo == null)
			nfo = checkLine(line, this.aspectRatioKeywords, NFOClass.AspectRatio);

		if (nfo == null)
			nfo = checkLine(line, this.subtitlesKeywords, NFOClass.Subtitles);

		if (nfo == null)
			nfo = checkLine(line, this.durationKeywords, NFOClass.Duration);

		if (nfo == null)
			nfo = checkLine(line, this.genreKeywords, NFOClass.Genre);

		if (nfo == null)
			nfo = checkLine(line, this.formatKeywords, NFOClass.Format);

		if (nfo == null)
			nfo = checkLine(line, this.framerateKeywords, NFOClass.FrameRate);

		if (nfo == null)
			nfo = new NFOLine(NFOClass.Ignore, "", line);
		

		return nfo;
	}

	private NFOLine checkImdb(String line) {
		Pattern p = Pattern.compile("http://www.imdb.com/title/[^/]+/");
		Matcher m = p.matcher(line);
		if (m.find())
			return new NFOLine(NFOClass.IMDBLink, m.group(), line);
		else
			return null;
		
	}

	private NFOLine checkLine(String line, String[] keywords, NFOClass type) {
		return checkLine(line, keywords, type, true);
	}
	
	private NFOLine checkLine(String line, String[] keywords, NFOClass type, boolean splitBySeparator) {
		line = StringUtils.trim(line);
		
		NFOLine seriesLine = checkSeries(line);
		if (line != null)
			return seriesLine;
		
		for(String s : keywords) {
			if (StringUtils.startsWithIgnoreCase(line, s)) {
				if (splitBySeparator) {
					for (int i = 0; i < separators.length; i++) {
						if (StringUtils.containsAny(line, separators[i])) {
								String[] pieces = StringUtils.split(line, separators[i]);
								if (pieces.length > 1)
									return new NFOLine(type, StringUtils.trim(pieces[1]), line);
						}
					}
				}				
				else {
					return new NFOLine(type, line, line);
				}				
			}
		}
		
		return null;
	}

	/***
	 * Help function for identifying lines that include a season/episode pattern
	 * @param line
	 * @return
	 */
	private NFOLine checkSeries(String line) {
		Pattern p = Pattern.compile("s\\d{1,2}e\\s{1,2}");
		Matcher m = p.matcher(line);
		if (m.matches())
			return new NFOLine(NFOClass.SeriesInfo, m.group(), line);
		else
			return null;
	}

	private boolean isAcceptedCharacter(char c) {
		for (int i = 0; i < acceptedCharacters.length(); i++) {
			if (acceptedCharacters.charAt(i) == c)
				return true;
		}
		return false;
	}

}
