package se.qxx.jukebox.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.imgscalr.Scalr;

import com.google.protobuf.ByteString;

import fr.noop.subtitle.model.SubtitleObject;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.model.SubtitleWriter;
import fr.noop.subtitle.srt.SrtParser;
import fr.noop.subtitle.vtt.VttWriter;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.watcher.ExtensionFileFilter;
import se.qxx.jukebox.watcher.FileRepresentation;

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
	public static List<File> getFileListing(File directory, ExtensionFileFilter filter, boolean recurse)
	{
		return getFileListing(directory.listFiles(filter), filter, recurse);
	}

	/**
	 * Function that recursively searches a directory tree for files with specific extensions
	 * @param filesAndDirs Array of files and directories to iterate through
	 * @param filter The filter of extension to look for
	 * @return
	 */
	private static List<File> getFileListing(File[] filesAndDirs, ExtensionFileFilter filter, boolean recurse) {
		List<File> result = new ArrayList<File>();

		for(File file : filesAndDirs) {
			
			if (file.isFile() ) {
				result.add(file);
			} else if (recurse) {
				result.addAll(
					getFileListing(
						file.listFiles(filter), filter, recurse));
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

		return getFileListing(dirF.listFiles(), filter, true);
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


	
	public static ByteString getScaledImage(ByteString imagedata) throws IOException {
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(imagedata.toByteArray()));
		BufferedImage scaled = Scalr.resize(img, 150);
		return getByteStringFromImage(scaled);
	}
	
	public static ByteString getByteStringFromImage(BufferedImage img) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		return ByteString.copyFrom(baos.toByteArray());
		
	}

	public static boolean isMatroskaFile(Media md) {
		return StringUtils.endsWithIgnoreCase(md.getFilename(), "mkv");
	}
	
	public static String getFullFilePath(Media md) {
		return getFilePath(
			FilenameUtils.normalizeNoEndSeparator(md.getFilepath()), 
			md.getFilename());
	}
	
	public static String getFullFilePath(String filePath, String fileName) {
		return getFilePath(
			FilenameUtils.normalizeNoEndSeparator(filePath), 
			fileName);
	}
	
	
	public static String getConvertedFullFilepath(Media md) {
		return getFilePath(
				FilenameUtils.normalizeNoEndSeparator(md.getFilepath()), 
				md.getConvertedFileName());
	}
	
	private static String getFilePath(String filepath, String filename) {
		return String.format("%s/%s", filepath, filename);
	}

	public static boolean isExcludedFile(FileRepresentation f, IJukeboxLogger log) {
		if (StringUtils.containsIgnoreCase(f.getName(), "sample")) {
			log.Info(String.format("Ignoring %s as this appears to be a sample", f.getName()));
			return true;
		}
		else if (FilenameUtils.removeExtension(f.getName()).endsWith("[tazmo]")) {
			//Log.Info(String.format("Ignoring %s as this is a converted file", f.getName()), logType);
			return true;
		}		
		else if (f.getFileSize() < 104857600) {
			log.Info(String.format("Ignoring %s as this has a file size of less than 100MB", f.getName()));
			return true;
		}

		
		return false;
	}

	public static String findIpAddress() {

		try {
			List<NetworkInterface> nets = Collections.list(NetworkInterface.getNetworkInterfaces());
		
			Collections.sort(nets, new Comparator<NetworkInterface>() {	
				@SuppressWarnings("serial")
				Map<String, Integer> map = new HashMap<String, Integer>() {
				{
					put("eth0", 1);
					put("eth1", 2);
					put("eth2", 3);
					put("eth3", 4);
					put("eth4", 5);
					put("eth5", 6);
					put("wlan0", 7);
					put("wlan1", 8);
					put("wlan2", 9);
					put("wlan3", 10);
					put("wlan4", 11);
					put("wlan5", 12);
					put("lo", 99);
				}};
				
				@Override
				public int compare(NetworkInterface o1, NetworkInterface o2) {
					int x = 99;
					int y = 99;
					if (map.containsKey(o1.getName()))
						x = map.get(o1.getName());
					
					if (map.containsKey(o2.getName()))
						y = map.get(o2.getName());
					
					return Integer.compare(x, y);
				}
				
			});
			
			for (NetworkInterface intf : nets) {
				Enumeration<InetAddress> addr = intf.getInetAddresses();
				while (addr.hasMoreElements()) {
					InetAddress a = addr.nextElement();
					if (a instanceof Inet4Address && !a.getHostAddress().equals("127.0.0.1")) {
						return a.getHostAddress();	
					}
				}
			}
			
		} catch (SocketException e) {
			
		}
		return "127.0.0.1";
	}

}
