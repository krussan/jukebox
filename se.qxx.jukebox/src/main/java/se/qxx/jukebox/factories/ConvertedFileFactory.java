package se.qxx.jukebox.factories;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.interfaces.IConvertedFile;
import se.qxx.jukebox.interfaces.IVLCConnection;

public interface ConvertedFileFactory {
	public IConvertedFile create(Media md);
}
