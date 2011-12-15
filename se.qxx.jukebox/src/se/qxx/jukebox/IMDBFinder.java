package se.qxx.jukebox;

import java.io.IOException;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

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
		String urlString = "http://www.imdb.com/find?s=all&q=" + urlParameters;

		String webResult = WebRetriever.getWebResult(urlString);
		
		String result;
		result = findUrl("Titles\\s*\\(Exact\\s*Matches\\).*?\\<a\\s*href\\s*=\\s*[\"|'](?<url>.*?)[\"|']"
				, webResult
				, "url");
		
		if (result == null)
			result = findUrl("Popular\\s*Titles.*?\\<a\\s*href\\s*=\\s*[\"|'](?<url>.*?)[\"|']"
					, webResult
					, "url");
				
		if (result != null) {
			result = "http://www.imdb.com" + result;
			return Movie.newBuilder().mergeFrom(m).setImdbUrl(result).build();
		}
			
		else
			return m;
	}
	
	private static String findUrl(String pattern, String input, String groupName) {
		NamedPattern p = NamedPattern.compile(pattern);
		NamedMatcher matcher = p.matcher(input);
		
		if (matcher.find())
			return matcher.group(groupName);
		else
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