package se.qxx.jukebox.converter;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.tools.Util;

public class ConvertedFile {

	private Media media;

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	public ConvertedFile(Media md) {
		this.setMedia(md);
	}
	
	public String getFilename() {
		return this.getMedia().getFilename();
	}
	
	public String getFullFilepath() {
		return new Util().getFullFilePath(this.getMedia());
	}
	
	public String getConvertedFilename() {
		return String.format("%s_[tazmo].mp4", FilenameUtils.getBaseName(this.getMedia().getFilename()));
	}
	
	public String getFilePath() {
		return this.getMedia().getFilepath();
	}
	
	public String getConvertedFullFilepath() {
		return Util.getFullFilePath(this.getFilePath(), this.getConvertedFilename());
	}
	
	public boolean sourceFileExist() {
		File f = new File(this.getFullFilepath());
		return f.exists();
	}
	
	public boolean isForcedOrFailed() {
		return this.getMedia().getConverterState() == MediaConverterState.Forced
				|| this.getMedia().getConverterState() == MediaConverterState.Failed;
	}

	public boolean convertedFileExists() {
		File f = new File(this.getConvertedFullFilepath());
		
		return f.exists();
	}
}
