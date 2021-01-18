package se.qxx.jukebox.factories;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.interfaces.IConvertedFile;

public interface ConvertedFileFactory {
	IConvertedFile create(Media md);
}
