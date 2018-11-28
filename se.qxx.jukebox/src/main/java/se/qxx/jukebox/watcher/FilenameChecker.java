package se.qxx.jukebox.watcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IJukeboxLogger;

public class FilenameChecker implements IFilenameChecker {

	private IJukeboxLogger log;
	
	@Inject
	public FilenameChecker(LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.FIND));
	}
	
	public IJukeboxLogger getLog() {
		return log;
	}
	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}
	@Override
	public boolean isConvertedFile(String filename) {
		return FilenameUtils.removeExtension(filename).endsWith("[tazmo]");
	}

	@Override
	public boolean isExcludedFile(FileRepresentation file) {
		if (isSampleFile(file.getName())) {
			getLog().Info(String.format("Ignoring %s as this appears to be a sample", file.getName()));
			return true;
		}
		else if (isConvertedFile(file.getName())) {
			//Log.Info(String.format("Ignoring %s as this is a converted file", f.getName()), logType);
			return true;
		}		
		else if (isSmallFile(file.getFileSize())) {
			getLog().Info(String.format("Ignoring %s as this has a file size of less than 100MB", file.getName()));
			return true;
		}

		
		return false;
	}

	@Override
	public boolean isSampleFile(String filename) {
		return StringUtils.containsIgnoreCase(filename, "sample");
	}

	@Override
	public boolean isSmallFile(long size) {
		return size < 104857600;
	}
	

}
