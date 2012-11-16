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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;

public class Util {
	/**
	 * Replaces a pattern with empty space. Typically used to ignore certain patterns in filenames
	 * @param fileNameToMatch
	 * @param strIgnorePattern
	 * @return
	 */
	public static String replaceIgnorePattern(String fileNameToMatch, String strIgnorePattern) {
		if (strIgnorePattern.trim().length() > 0) {
			Pattern ignorePattern = Pattern.compile(strIgnorePattern, Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.DOTALL);
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
	 * Returns the system temporary directory
	 * @return
	 */
	public static String getTempDirectory() {
        return SystemUtils.getJavaIoTmpDir().getAbsolutePath();
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
	
	public static Object getInstance(String className, Class<?>[] constructorDefinition, Object[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> c = Class.forName(className);
				
		Constructor<?> con = c.getConstructor(constructorDefinition);
		return con.newInstance(args);		
	}

	public static String getImdbIdFromUrl(String imdbUrl) {
		// http://www.imdb.com/title/tt1541874/
		Pattern p = Pattern.compile("http://www.imdb.com/.*?/tt(\\d*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(imdbUrl);
		if (m.find())
			return m.group(1);
		else
			return StringUtils.EMPTY;
	}

	public static void waitForSettings() {
		while (Settings.get() == null) {
			Log.Info("Settings has not been initialized. Sleeping for 10 seconds", Log.LogType.MAIN);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}		
	}

	public static List<String> getExtensions() {
		List<String> list = new ArrayList<String>();
		
		for (JukeboxListenerSettings.Catalogs.Catalog c : Settings.get().getCatalogs().getCatalog()) {
			for (JukeboxListenerSettings.Catalogs.Catalog.Extensions.Extension e : c.getExtensions().getExtension()) {
				if (!list.contains(e.getValue()))
					list.add(e.getValue());
			}
		}
		
		return list;
	}
}
