package se.qxx.jukebox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter;
import se.qxx.jukebox.subtitles.SubFile.Rating;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

public class Util {
	public static Movie extractMovie(String filePath, String fileName) {
		int maxGroupMatch = 0;
		ArrayList<String> groupsToCheck = new ArrayList<String>();
		groupsToCheck.add("title");
		groupsToCheck.add("year");
		groupsToCheck.add("type");
		groupsToCheck.add("format");
		groupsToCheck.add("sound");
		groupsToCheck.add("language");
		groupsToCheck.add("group");
		
		String 	title = "", 
				type = "",
				format = "", 
				sound = "", 
				language = "", 
				group = "";
		int year = 0;
		
		for (Splitter splitter : Settings.get().getStringSplitters().getSplitter()) {
			NamedPattern p = NamedPattern.compile(splitter.getRegex().trim());
			NamedMatcher m = p.matcher(fileName);
			
			int matches = 0;
			for (String s : groupsToCheck) {
				if (m.matches() && p.groupNames().contains(s))
					if (m.group(s) != null)
						if (m.group(s).length() > 0) 
							matches++;
			}
			
			if (matches > maxGroupMatch) {
				maxGroupMatch = matches;

				if (m.group("title") != null && p.groupNames().contains("title")) 
					title = m.group("title").replace(".", " ").replace("_", " ").replace("-", " ");
				
				if (m.group("year") != null && p.groupNames().contains("year")) 
					year = Integer.valueOf(m.group("year").replace(".", " ").replace("_", " ").replace("-", " "));
				
				if (m.group("type") != null && p.groupNames().contains("type")) 
					type = m.group("type").replace(".", " ").replace("_", " ").replace("-", " ");
				
				if (m.group("format") != null && p.groupNames().contains("format")) 
					format = m.group("format").replace(".", " ").replace("_", " ").replace("-", " ");
				
				if (m.group("sound") != null && p.groupNames().contains("sound")) 
					sound = m.group("sound").replace(".", " ").replace("_", " ").replace("-", " ");
				
				if (m.group("language") != null && p.groupNames().contains("language")) 
					language = m.group("language").replace(".", " ").replace("_", " ").replace("-", " ");
				
				if (m.group("group") != null && p.groupNames().contains("group")) 
					group = m.group("group").replace(".", " ").replace("_", " ").replace("-", " ");
				
			}
			
		}

		if (maxGroupMatch > 0) {
			Movie movie = Movie.newBuilder()
				.setID(-1)
				.setFilename(fileName)
				.setFilepath(filePath)
				.setTitle(title)
				.setYear(year)
				.setType(type)
				.setFormat(format)
				.setSound(sound)
				.setLanguage(language)
				.setGroup(group)
				.build();
						
			return movie;
		}
		else {
			// movie does not match
			return Movie.newBuilder().setID(-1).build();
		}

	}

	public static Rating rateSub(Movie m, String subFilename) {
		Movie subMovie = Util.extractMovie("", subFilename);
		
		//TODO: Add compare of filenames to get an exact match
		Rating r = Rating.NotMatched;
		if (subMovie.getGroup().equals(m.getGroup())) {
			if (subMovie.getFormat().equals(m.getFormat()))
				r = Rating.PositiveMatch;
			else
				r = Rating.ProbableMatch;
		}
		return r;
	}
	
	public static String getTempSubsName(String filename) {
		String path = createTempSubsPath();
        return String.format("%s/%s_%s", path, Thread.currentThread().getId(), filename);
	}
	
	public static String createTempSubsPath() {
		String tempPath = Settings.get().getSubFinders().getSubsPath() + "/temp";
		File path = new File(tempPath);
		if (!path.exists()) {
			path.mkdir();
		}
		
		return tempPath;
	}
	
	public static String readMessageFromStream(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
        int len;
        byte[] buffer = new byte[4096];
        
        while (-1 != (len = is.read(buffer))) {
        	Log.Debug(String.format("Read %s bytes from inputstream", len));
        	bos.write(buffer, 0, len);
        	
        	Log.Debug(new String(bos.toByteArray(), "ISO-8859-1"));
        }
        
        Log.Debug("end-of-file reached in inputstream");
        bos.flush();
        bos.close();
        
		return new String(bos.toByteArray(), "ISO-8859-1");

	}

}
