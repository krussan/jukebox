package se.qxx.jukebox.interfaces;

import java.io.File;
import java.util.List;

public interface IUnpacker {

	List<File> unpackFiles(File f, String path);

	List<File> moveFile(File f, String path);

}