package se.qxx.jukebox.interfaces;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.protobuf.ByteString;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.watcher.ExtensionFileFilter;
import se.qxx.jukebox.watcher.FileRepresentation;

public interface IUtils {

	/**
	 * Replaces a pattern with empty space. Typically used to ignore certain patterns in filenames
	 * @param fileNameToMatch
	 * @param strIgnorePattern
	 * @return
	 */
	String replaceIgnorePattern(String fileNameToMatch, String strIgnorePattern);

	/****
	 * Replaces all occurences of dot, underline and hyphen with space
	 * @param The string on which replaces should take place
	 * @return 
	 */
	String parseAwaySpace(String inputString);

	/**
	 * Copies the content of a stream to a string
	 * @param is The stream to read from
	 * @returns
	 * @throws IOException
	 */
	String readMessageFromStream(InputStream is) throws IOException;

	/**
	 * Function that recursively searches a directory tree for files with specific extensions
	 * @param directory The top node in the directory to search for files
	 * @param filter The filter of extension to look for
	 * @return
	 */
	List<FileRepresentation> getFileListing(File directory, ExtensionFileFilter filter, boolean recurse);

	/**
	 * The function getFileListing does not work for UNC paths so this is the workaround
	 * using FileSystemView instead
	 * @param directory The top node in the directory to search for files
	 * @param filter The filter of extension to look for
	 * @return
	 */
	List<FileRepresentation> getFileListingWorkAround(File directory, ExtensionFileFilter filter);

	/**
	 * Tries to parse an integer to a string
	 * @param string The string to be parsed
	 * @return
	 */
	boolean tryParseInt(String string);

	/**
	 * Returns the system temporary directory
	 * @return
	 */
	String getTempDirectory();

	/**
	 * Returns the current system timestamp
	 * @return
	 */
	long getCurrentTimestamp();

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
	Object getInstance(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException;

	Object getInstance(String className, Class<?>[] constructorDefinition, Object[] args)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException;

	ByteString getScaledImage(ByteString imagedata) throws IOException;

	ByteString getByteStringFromImage(BufferedImage img) throws IOException;

	boolean isMatroskaFile(Media md);

	String getFullFilePath(Media md);

	String getFullFilePath(String filePath, String fileName);

	String getConvertedFullFilepath(Media md);

	String findIpAddress();
	
	boolean fileExists(String filename);
	boolean mediaFileExists(Media md);

}