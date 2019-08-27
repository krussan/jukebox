package se.qxx.jukebox.converter;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.interfaces.IConvertedFile;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.tools.Util;

public class ConvertedFile implements IConvertedFile {

	private Media media;
	private IUtils utils;

	@Inject
	public ConvertedFile(@Assisted Media md, IUtils utils) {
		this.setUtils(utils);
		this.setMedia(md);
	}

	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#getMedia()
	 */
	@Override
	public Media getMedia() {
		return media;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#setMedia(se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public void setMedia(Media media) {
		this.media = media;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#getFilename()
	 */
	@Override
	public String getFilename() {
		return this.getMedia().getFilename();
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#getFullFilepath()
	 */
	@Override
	public String getFullFilepath() {
		return new Util().getFullFilePath(this.getMedia());
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#getConvertedFilename()
	 */
	@Override
	public String getConvertedFilename() {
		return String.format("%s_[tazmo].mp4", FilenameUtils.getBaseName(this.getMedia().getFilename()));
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#getFilePath()
	 */
	@Override
	public String getFilePath() {
		return this.getMedia().getFilepath();
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#getConvertedFullFilepath()
	 */
	@Override
	public String getConvertedFullFilepath() {
		return this.getUtils().getFullFilePath(this.getFilePath(), this.getConvertedFilename());
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#sourceFileExist()
	 */
	@Override
	public boolean sourceFileExist() {
		return this.getUtils().fileExists(this.getFullFilepath())
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#isForcedOrFailed()
	 */
	@Override
	public boolean isForcedOrFailed() {
		return this.getMedia().getConverterState() == MediaConverterState.Forced
				|| this.getMedia().getConverterState() == MediaConverterState.Failed;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.converter.IConvertedFile#convertedFileExists()
	 */
	@Override
	public boolean convertedFileExists() {
		File f = new File(this.getConvertedFullFilepath());
		
		return f.exists();
	}
}
