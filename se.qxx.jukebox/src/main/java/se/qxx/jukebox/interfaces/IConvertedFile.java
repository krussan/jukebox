package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.domain.JukeboxDomain.Media;

public interface IConvertedFile {

	Media getMedia();

	void setMedia(Media media);

	String getFilename();

	String getFullFilepath();

	String getConvertedFilename();

	String getFilePath();

	String getConvertedFullFilepath();

	boolean sourceFileExist();

	boolean isForcedOrFailed();

	boolean convertedFileExists();

}