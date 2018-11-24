package se.qxx.jukebox.interfaces;

import java.io.FileNotFoundException;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.tools.MediaMetadata;

public interface IMediaMetadataHelper {

	MediaMetadata getMediaMetadata(Media md);

	MediaMetadata getMediaMetadata(String fullFilePath) throws FileNotFoundException;

	Media addMediaMetadata(Media md);

}