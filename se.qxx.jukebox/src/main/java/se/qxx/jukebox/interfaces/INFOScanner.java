package se.qxx.jukebox.interfaces;

import java.io.File;
import java.util.List;

import se.qxx.jukebox.builders.NFOLine;
import se.qxx.jukebox.builders.exceptions.SeriesNotSupportedException;

public interface INFOScanner {

	File getNfoFile();

	List<NFOLine> scan() throws SeriesNotSupportedException;

}