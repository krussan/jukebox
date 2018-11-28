package se.qxx.jukebox.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import fr.noop.subtitle.model.SubtitleParsingException;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public interface IMkvSubtitleReader {

	void extractSubs(String filename, String outputPath)
			throws FileNotFoundException, IOException, SubtitleParsingException;

	List<Subtitle> extractSubs(String filename);

}