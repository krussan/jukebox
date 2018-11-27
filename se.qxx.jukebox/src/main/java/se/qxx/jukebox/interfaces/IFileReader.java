package se.qxx.jukebox.interfaces;

import java.io.File;
import java.io.IOException;

public interface IFileReader {
	public byte[] readFile(File f) throws IOException;
}
