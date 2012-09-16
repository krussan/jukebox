package se.qxx.jukebox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.StringSplitters.Splitter;
import se.qxx.jukebox.subtitles.SubFile.Rating;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

public class Util {
	/**
	 * Replaces a pattern with empty space. Typically used to ignore certain patterns in filenames
	 * @param fileNameToMatch
	 * @param strIgnorePattern
	 * @return
	 */
	public static String replaceIgnorePattern(String fileNameToMatch, String strIgnorePattern) {
		if (strIgnorePattern.trim().length() > 0) {
			Pattern ignorePattern = Pattern.compile(strIgnorePattern, Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
			Matcher ignoreMatcher = ignorePattern.matcher(fileNameToMatch);
			//fileNameToMatch = fileNameToMatch.replaceAll(strIgnorePattern, "");
			fileNameToMatch = ignoreMatcher.replaceAll("");
		}
		return fileNameToMatch;
	}
	
	/****
	 * Replaces all occurences of dot, underline and hyphen with space
	 * @param The string on which replaces should take place
	 * @return 
	 */
	public static String parseAwaySpace(String inputString) {
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
		FilenameBuilder b = new FilenameBuilder();
		Movie subMovie = b.extractMovie("", subFilename);
		Rating r = Rating.NotMatched;
		
		if (subMovie != null) {
			//Check if filenames match exactly
			String filenameWithoutExtension = getFilenameWithoutExtension(m.getFilename());
			if (filenameWithoutExtension.equals(subFilename))
				return Rating.ExactMatch;
			
			
			String group = m.getGroup();
			String subGroup = subMovie.getGroup();
			
			if (subGroup != null) {
				if (subGroup.equals(group) && subGroup != "") {
					if (subMovie.getFormat().equals(m.getFormat()) && subMovie.getFormat() != "")
						r = Rating.PositiveMatch;
					else
						r = Rating.ProbableMatch;
				}
			}
		}
		return r;
	
	}
	
	/**
	 * Returns the filename except extension
	 * 
	 * @param filename
	 * @return
	 */
	public static String getFilenameWithoutExtension(String filename) {
		int index = filename.lastIndexOf('.');
		if (index >= 0)
			return filename.substring(0,  filename.lastIndexOf('.'));
		else
			return filename;
	}
	
	/**
	 * Return a temporary filename to download subtitles to.
	 * @param filename The filename of the movie
	 * @return
	 */
	public static String getTempSubsName(String filename) {
		String path = createTempSubsPath();
        return String.format("%s/%s_%s", path, Thread.currentThread().getId(), filename);
	}
	
	/**
	 * Returns a temporary path to download subtitles to
	 * @return
	 */
	public static String createTempSubsPath() {
		String tempPath = Settings.get().getSubFinders().getSubsPath() + "/temp";
		File path = new File(tempPath);
		if (!path.exists()) {
			path.mkdir();
		}
		
		return tempPath;
	}
	
	/**
	 * Copies the content of a stream to a string
	 * @param is The stream to read from
	 * @returns
	 * @throws IOException
	 */
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
	
	/**
	 * Function that recursively searches a directory tree for files with specific extensions
	 * @param directory The top node in the directory to search for files
	 * @param filter The filter of extension to look for
	 * @return
	 */
	public static List<File> getFileListing(File directory, ExtensionFileFilter filter)
	{
		List<File> result = checkExtension(directory.listFiles(filter), filter);
		
		return result;
	}

	/**
	 * Function that recursively searches a directory tree for files with specific extensions
	 * @param filesAndDirs Array of files and directories to iterate through
	 * @param filter The filter of extension to look for
	 * @return
	 */
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
	
	/**
	 * The function getFileListing does not work for UNC paths so this is the workaround
	 * using FileSystemView instead
	 * @param directory The top node in the directory to search for files
	 * @param filter The filter of extension to look for
	 * @return
	 */
	public static List<File> getFileListingWorkAround(File directory, ExtensionFileFilter filter) {
		// Workaround
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File dirF = fsv.getParentDirectory(new File(directory.getName(), "C$"));

		return checkExtension(dirF.listFiles(), filter);
	}

	/**
	 * Tries to parse an integer to a string
	 * @param string The string to be parsed
	 * @return
	 */
	public static boolean tryParseInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Function that returns if a string contains another string ignoring case
	 * @param text The string to be searched
	 * @param pattern The string to be found
	 * @return
	 */
	public static boolean stringContainsIgnoreCase(String text, String pattern) {
		Pattern p = Pattern.compile(Pattern.quote(pattern), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		return m.find();
	}	
	
	/**
	 * Returns the system temporary directory
	 * @return
	 */
	public static String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
	}
	
	/**
	 * Returns the current system timestamp
	 * @return
	 */
	public static long getCurrentTimestamp() {
		java.util.Date date = new java.util.Date();
		return date.getTime();
	}
	
	/**
	 * Gets a new instance for the specified class. The class has to have a public constructor with no arguments.
	 * @param className
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	public static Object getInstance(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> c = Class.forName(className);
		
		Class<?>[] parTypes = new Class<?>[] {};
		Object[] args = new Object[] {};
		Constructor<?> con = c.getConstructor(parTypes);
		return con.newInstance(args);		
	}
}
