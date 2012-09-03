package se.qxx.jukebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class IMDBFinder {
	public static Movie Search(Movie m) throws IOException {
        //http://www.imdb.com/find?s=all&q=the+decent
        // search for :
        // Titles (Exact Matches)
        // Popular Titles               <-- This is the one
        // Titles (Partial Matches)
        // Titles (Approx Matches)
        // find first href after that

        // Titles\s\(Exact\sMatches\).*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
        // Popular\sTitles.*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
		
		String urlParameters = java.net.URLEncoder.encode(m.getTitle(), "ISO-8859-1");
		String urlString = "http://www.imdb.com/find?s=tt&q=" + urlParameters;

		String webResult = WebRetriever.getWebResult(urlString);


		IMDBRecord rec = findUrlByPopularTitles(m, webResult);
		if (rec == null) {
			rec = findUrlByExactMatches(m, webResult);
		}

		if (rec != null) {
			String url = "http://www.imdb.com" + rec.getUrl();
			return Movie.newBuilder().mergeFrom(m).setImdbUrl(url).build();
		}
		else
			return m;
	}
	
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
			"<a\\s*href=\"([^\"]*?)\"[^>]*>[^<]+?</a>\\s*\\((\\d{4})\\)",
			1,
			2);
	}

	private static IMDBRecord findUrlByPopularTitles(Movie movie, String text) {
		return findUrl(
			movie,
			text,
			"<p>(.*?)Popular\\s*Titles(.*?)<table>(.*?)<\\/table>",
			3,
			"<a\\s*href=\"([^\"]*?)\"[^>]*>[^<]+?</a>\\s*\\((\\d{4})\\)",
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
			Pattern p = Pattern.compile(patternForBlock);
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