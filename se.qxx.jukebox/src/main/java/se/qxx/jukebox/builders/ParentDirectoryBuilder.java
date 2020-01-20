package se.qxx.jukebox.builders;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.CatalogsTest;

public class ParentDirectoryBuilder extends ParserBuilder {

	public ParentDirectoryBuilder(ISettings settings, IJukeboxLogger log) {
		super(settings, log);
	}

	@Override
	public MovieOrSeries extract(String filepath, String filename) {
		Media md = getMedia(filepath, filename);
		
		File path = new File(filepath); 
		String parentDirectoryName = FilenameUtils.getName(filepath);
		
		Pattern pattern = Pattern.compile("CD[1-9]+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(parentDirectoryName);
		
		if (matcher.matches()) {
			this.getLog().Info("ParentDirectory appears to be a CD indicator..");
			path = path.getParentFile();
			filepath = path.getAbsolutePath();
		}

		this.getLog().Info(String.format("ParentDirectoryBuilder path :: %s", filepath));

		MovieOrSeries mos = null;
		
		// check if path exist and if that is under one of the base directory
		if (path.exists() && !isBasePath(filepath)) {
			// add .dummy as extension as this is removed by FilenameBuilder
			mos = super.extract(path.getParent(), path.getName() + ".dummy");
			if (mos != null) {
				mos.replaceMedia(md);
				mos.setIdentifier(Identifier.ParentDirectory);
			}
		}

		return mos;
	}
	
	private boolean isBasePath(String path) {
		if (path.length() >= 2 && (path.endsWith("/") || path.endsWith("\\")))
			path = path.substring(0, path.length() - 2);
		
		for (CatalogsTest c : this.getSettings().getSettings().getCatalogs()) {
			if (path.toLowerCase().equals(c.getPath().toLowerCase()))
				return true;
		}
		
		return false;
	}

}
