package se.qxx.jukebox.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import fr.noop.subtitle.model.SubtitleParsingException;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleRequestType;

public interface ISubtitleFileWriter {

	File getTempFile(Subtitle sub, String extension);
	
	File writeSubtitleToFile(Subtitle sub, File destinationFile)
			throws IOException, SubtitleParsingException, FileNotFoundException;
	
	File writeSubtitleToFileConvert(Subtitle sub, File destinationFile)
			throws IOException, SubtitleParsingException, FileNotFoundException;
	
}
