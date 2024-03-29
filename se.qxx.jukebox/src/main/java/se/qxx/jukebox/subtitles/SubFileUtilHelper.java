package se.qxx.jukebox.subtitles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileUtilHelper ;

@Singleton
public class SubFileUtilHelper implements ISubFileUtilHelper {
	
	private ISettings settings;
	@Inject
	public SubFileUtilHelper(ISettings settings) {
		this.setSettings(settings);
		
	}
	public ISettings getSettings() {
		return settings;
	}
	public void setSettings(ISettings settings) {
		this.settings = settings;
	}
	
	/**
	 * Returns a temporary path to download subtitles to
	 * @return
	 */
	@Override
	public String createTempSubsPath(MovieOrSeries mos) {
		String tempPath = 
			FilenameUtils.normalize(
				String.format("%s/temp/%s"
					, this.getSettings().getSettings().getSubfinders().getPath()
					, mos.getID()));

		File path = new File(tempPath);
		if (!path.exists())
			path.mkdirs();
		
		return tempPath;
	}

	@Override
	public List<String> findSubsInDirectory(File dir) {
		if (dir != null && dir.exists())
			return List.of(dir.list((f, name) -> name.endsWith("srt") || name.endsWith("idx") || name.endsWith("sub")));
		else
			return new ArrayList<>();
	}

}
