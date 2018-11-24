package se.qxx.jukebox.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import fr.noop.subtitle.model.SubtitleParsingException;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public interface ISubtitleFileWriter {

	File writeSubtitleToTempFileVTT(Subtitle sub) throws FileNotFoundException, IOException, SubtitleParsingException;

	File writeSubtitleToTempFile(Subtitle sub) throws FileNotFoundException, IOException, SubtitleParsingException;

	File writeSubtitleToFile(Subtitle sub, File destinationFile)
			throws IOException, SubtitleParsingException, FileNotFoundException;

	File writeSubtitleToFileVTT(Subtitle sub, File destinationFile)
			throws IOException, SubtitleParsingException, FileNotFoundException;

}