package se.qxx.jukebox;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class IMDBFinder {
	private static long nextSearch = 0;
	
	public synchronized static Movie Search(Movie m) throws IOException {
        //http://www.imdb.com/find?s=all&q=the+decent
        // search for :
        // Titles (Exact Matches)
        // Popular Titles               <-- This is the one
        // Titles (Partial Matches)
        // Titles (Approx Matches)
        // find first href after that

        // Titles\s\(Exact\sMatches\).*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
        // Popular\sTitles.*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
	
		long currentTimeStamp = Util.getCurrentTimestamp();
		try {
			if (currentTimeStamp < nextSearch)
				Thread.sleep(nextSearch - currentTimeStamp);
			
			String urlParameters = java.net.URLEncoder.encode(m.getTitle(), "ISO-8859-1");
			String urlString = "http://www.imdb.com/find?s=tt&q=" + urlParameters;

			WebResult webResult = WebRetriever.getWebResult(urlString);
			
			// Accomodate for that sometimes IMDB redirects you
			// directly to the correct movie. (i.e. "Cleanskin")
			// This could be detected by that the title of the web page is the
			// title of the movie or NOT "IMDB search"
			IMDBRecord rec;
			if (webResult.isRedirected()) {
				rec = IMDBRecord.getFromWebResult(webResult);
			}
			else {
				rec = findUrlByPopularTitles(m, webResult.getResult());
				if (rec == null)
					rec = findUrlByExactMatches(m, webResult.getResult());
					
				if (rec == null)
					rec = findUrlByApproxMatches(m, webResult.getResult());
				
				
			}
			
			//TODO: Probably add this to user settings
			Random r = new Random();
			int n = r.nextInt(20000) + 10000;
			
			// sleep randomly to avoid detection (from 10 sec to 30 sec)
			nextSearch = Util.getCurrentTimestamp() + n;
			
			if (rec != null) {
				return Movie.newBuilder().mergeFrom(m)
						.setImdbUrl(rec.getUrl())
						.setDirector(rec.getDirector())
						.setDuration(rec.getDurationMinutes())
						.setStory(rec.getStory())
						.setRating(rec.getRating())
						.addAllGenre(rec.getAllGenres())
						.build();
			}
			else
				return m;
		} catch (InterruptedException e) {
			return m;
		}
	}
	
	/*private static boolean isRedirectedToMovie(String webResult) {
		Pattern p = Pattern.compile("<title>\\s*IMDb\\s*Search\\s*</title>"
				, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(webResult);
		
		return !m.find();
	}*/
	
	private static boolean testResult(Movie m, IMDBRecord result) {
		if (m == null || result == null)
			return false;
		
		return (m.getYear() == 0 || m.getYear() == result.getYear());
	}

	private static IMDBRecord findUrlByExactMatches(Movie movie, String text) {
		return findUrl(
			movie,
			text,
			"<p>(.*?)Titles\\s*\\(Exact\\s*Matches\\)(.*?)<table>(.*?)<\\/table>",
			3,
			"<a\\s*href=\"([^\"]*?)\"[^>]*>[^<]+?</a>\\s*\\((\\d{4})[^\\)]*?\\)",
			1,
			2);
	}

	private static IMDBRecord findUrlByPopularTitles(Movie movie, String text) {
		return findUrl(
			movie,
			text,
			"<p>(.*?)Popular\\s*Titles(.*?)<table>(.*?)<\\/table>",
			3,
			"<a\\s*href=\"([^\"]*?)\"[^>]*>[^<]+?</a>\\s*\\((\\d{4})[^\\)]*?\\)",
			1,
			2);
	}
	
	private static IMDBRecord findUrlByApproxMatches(Movie movie, String text) {
		return findUrl(
			movie,
			text,
			"<p>(.*?)Titles\\s*\\(Approx\\s*Matches\\)(.*?)<table>(.*?)<\\/table>",
			3,
			"<a\\s*href=\"([^\"]*?)\"[^>]*>[^<]+?</a>\\s*\\((\\d{4})[^\\)]*?\\)",
			1,
			2);
	}

	private static IMDBRecord findUrl(
			Movie movie, 
			String text, 
			String patternForBlock, 
			int patternGroupForBlock,
			String patternForRecord,
			int urlGroup,
			int yearGroup) {
		
		//TODO: Also match by length of movie
		try {
			Pattern p = Pattern.compile(patternForBlock
					, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			Matcher m = p.matcher(text);
			
			if (m.find()) {
				String blockMatch = m.group(patternGroupForBlock);
				
				Pattern pRec = Pattern.compile(patternForRecord);
				Matcher mRec = pRec.matcher(blockMatch);
				
				while (mRec.find()) {
					String url = mRec.group(urlGroup);
					int year = Integer.parseInt(mRec.group(yearGroup));
					
					IMDBRecord rec = new IMDBRecord(url, year);
					
					if (testResult(movie, rec))
					{
						// if year and title matches then continue to the URL and extract information about the movie.
						rec = IMDBRecord.get(url);

						// If the duration of the movie corresponds with the information retreived from MediaInfo then we're
						// probably right. 

						return rec;
					}
						
					
				}
				
			}
		}
		catch (Exception e) {
			Log.Error(String.format("Error occured when trying to find %s in IMDB", movie.getTitle()), LogType.FIND, e);
		}

		return null;
	}
}

/*
    internal class IMDBFinder {
        //http://www.imdb.com/find?s=all&q=the+decent
        // search for :
        // Titles (Exact Matches)
        // Popular Titles               <-- This is the one
        // Titles (Partial Matches)
        // Titles (Approx Matches)
        // find first href after that

        // Titles\s\(Exact\sMatches\).*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
        // Popular\sTitles.*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
        public static void Search(Movie m) {
            string url = String.Format("http://www.imdb.com/find?s=all&q={0}", System.Web.HttpUtility.UrlEncode(m.Title));
            WebRequest w = HttpWebRequest.Create(url);
            WebResponse resp = w.GetResponse();

            string response = String.Empty;
            using (StreamReader sr = new StreamReader(resp.GetResponseStream())) {
                response = sr.ReadToEnd();
            }

            Match match = Regex.Match(response, @"Titles\s\(Exact\sMatches\).*?\<a\s*href\s*=\s*[""|'](?<url>.*?)[""|']");
            if (match.Length > 0) {
                m.IMDB_URL = String.Format("http://www.imdb.com{0}", match.Groups["url"].Value);
            }
            else {
                match = Regex.Match(response, @"Popular\sTitles.*?\<a\s*href\s*=\s*[""|'](?<url>.*?)[""|']");
                if (match.Length > 0)
                    m.IMDB_URL = String.Format("http://www.imdb.com{0}", match.Groups["url"].Value);
            }

            if (!String.IsNullOrEmpty(m.IMDB_URL)) {
                Match idMatch = Regex.Match(m.IMDB_URL, @"http://www.imdb.com/title/tt(?<id>\d*)/");
                if (idMatch.Length > 0)
                    m.IMDB_ID = Convert.ToInt32(idMatch.Groups["id"].Value);
            }
        }
    }

*/