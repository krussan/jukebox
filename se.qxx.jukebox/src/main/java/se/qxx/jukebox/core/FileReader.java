package se.qxx.jukebox.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.inject.Inject;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.interfaces.IFileReader;

public class FileReader implements IFileReader {

	@Inject
	public FileReader() {
		
	}
	
	public byte[] readFile(File f) {
		try {
			FileInputStream fs = new FileInputStream(f);
			long length = f.length();
			if (length > Integer.MAX_VALUE) {
				fs.close();
				throw new ArrayIndexOutOfBoundsException();
			}
			byte[] data = new byte[(int) length];
			int offset = 0;
			int numRead = 0;
			while (offset < data.length && (numRead = fs.read(data, offset, data.length - offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < data.length) {
				fs.close();
				throw new IOException("Could not completely read file " + f.getName());
			}

			// Close the input stream and return bytes
			fs.close();
			return data;
		} catch (Exception e) {
			Log.Error("Error when reading file", LogType.FIND, e);
			return null;
		}

	}
}
