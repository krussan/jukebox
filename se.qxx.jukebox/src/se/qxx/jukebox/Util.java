package se.qxx.jukebox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter;
import se.qxx.jukebox.subtitles.SubFile.Rating;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.sun.xml.internal.ws.util.StringUtils;

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
			NamedPattern p = NamedPattern.compile(splitter.getRegex().trim(), Pattern.CASE_INSENSITIVE);
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
					title = Util.parseAwaySpace(m.group("title"));
				
				if (m.group("year") != null && p.groupNames().contains("year")) {
					String yearString = m.group("year");
					if (Util.tryParseInt(yearString))
						year = Integer.parseInt(yearString);
				}
				
				if (m.group("type") != null && p.groupNames().contains("type")) 
					type = Util.parseAwaySpace(m.group("type"));
				
				if (m.group("format") != null && p.groupNames().contains("format")) 
					format = Util.parseAwaySpace(m.group("format"));
				
				if (m.group("sound") != null && p.groupNames().contains("sound")) 
					sound = Util.parseAwaySpace(m.group("sound"));
				
				if (m.group("language") != null && p.groupNames().contains("language")) 
					language = Util.parseAwaySpace(m.group("language"));
				
				if (m.group("group") != null && p.groupNames().contains("group")) 
					group = Util.parseAwaySpace(m.group("group"));
				
			}
			
		}

		//if movie ends with extension then something is wrong
		if (title.endsWith(fileName.substring(fileName.length() - 3)))
			return null;
		
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
	
	private static String parseAwaySpace(String inputString) {
		return inputString.replace(".", " ").replace("_", " ").replace("-", " ");
	}

	/**
	 * Rates a sub file (or a string) depending on the all categories in the Movie
	 * class
	 * 
	 * @param m 				- The movie to compare against
	 * @param subFilename		- the filename or string of the subfile
	 * @return Rating			- A rating based on the Rating enumeration				
	 */
	public static Rating rateSub(Movie m, String subFilename) {
		Movie subMovie = Util.extractMovie("", subFilename);
		
		//Check if filenames match exactly
		String filenameWithoutExtension = getFilenameWithoutExtension(m.getFilename());
		if (filenameWithoutExtension.equals(subFilename))
			return Rating.ExactMatch;
		
		Rating r = Rating.NotMatched;
		if (subMovie.getGroup().equals(m.getGroup()) && subMovie.getGroup() != "") {
			if (subMovie.getFormat().equals(m.getFormat()) && subMovie.getFormat() != "")
				r = Rating.PositiveMatch;
			else
				r = Rating.ProbableMatch;
		}
		return r;
	}
	
	public static String getFilenameWithoutExtension(String filename) {
		return filename.substring(0,  filename.lastIndexOf('.'));
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
        	//Log.Debug(String.format("Read %s bytes from inputstream", len));
        	bos.write(buffer, 0, len);
        	
        	//Log.Debug(new String(bos.toByteArray(), "ISO-8859-1"));
        }
        
        //Log.Debug("end-of-file reached in inputstream");
        bos.flush();
        bos.close();
        
		return new String(bos.toByteArray(), "ISO-8859-1");

	}
	
	public static List<File> getFileListing(File directory, ExtensionFileFilter filter)
	{
		List<File> result = checkExtension(directory.listFiles(filter), filter);
		
		return result;
	}

	  //TODO: getFileListing does not work for UNC paths
	private static List<File> checkExtension(File[] filesAndDirs, ExtensionFileFilter filter) {
		List<File> result = new ArrayList<File>();

		for(File file : filesAndDirs) {
			
			if (file.isFile() ) {
				result.add(file);
			} else 
			{
				result.addAll(Util.getFileListing(file, filter));
			}
		}
		
		return result;
	}	
	
	public static List<File> getFileListingWorkAround(File directory, ExtensionFileFilter filter) {
		// Workaround
		FileSystemView fsv = FileSystemView.getFileSystemView();
//		System.out.println("Listing shares using UNC path via File.listFiles() with File object returned from\n" +
//			"FileSystemView.getParentDirectory(new File(dir,knownSubdir))\n" +
//			"ultimately still uses File.listFiles(), but this time it works.");
		File dirF = fsv.getParentDirectory(new File(directory.getName(), "C$"));
//		System.out.println("List.listFiles() of "+dirF+" (note missing \\\\ that normally begins a UNC path).");
//		System.out.println("List.toURI() = "+dirF.toURI());
		return checkExtension(dirF.listFiles(), filter);
	}

	public static boolean tryParseInt(String string) {
		int ret;
		try {
			ret = Integer.parseInt(string);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
