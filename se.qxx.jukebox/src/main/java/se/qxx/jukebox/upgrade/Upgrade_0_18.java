package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.MediaMetadata;

public class Upgrade_0_18 extends UpgraderBase implements IIncrimentalUpgrade {
	
	public Upgrade_0_18(IDatabase database) {
		super(database);
	}

	private static String[] DbScripts = {
			"UPDATE SubtitleQueue\r\n" + 
			"SET subtitleretreivedat = 0\r\n" + 
			" , subtitleretreiveresult = 0\r\n" + 
			"WHERE ID IN (__IDS__) "};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,18);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,17);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		// get all media
		// check with mediainfo if subs exist
		// update those subtitlequeues
		try {
			Settings.initialize();
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
		
		List<String> movieIds = new ArrayList<String>();
		
		List<Movie> movies = this.getDatabase().searchMoviesByTitle("");
		for (Movie m : movies) {
			for (Media md : m.getMediaList()) {
				if (md.getFilename().endsWith("mkv")) {
					System.out.println(String.format("Checking subtitles on %s", md.getFilename()));					
					MediaMetadata mm = MediaMetadata.getMediaMetadata(md);
					if (mm != null && mm.getSubtitles().size() > 0) {
						System.out.println("Subs found. Clearing subtitlequeue");
						movieIds.add(String.valueOf(m.getSubtitleQueue().getID()));
					}
				}
			}
		}
		
		List<Series> series = this.getDatabase().searchSeriesByTitle("");
		for (Series s : series) {
			for (Season ss : s.getSeasonList()) {
				for (Episode ep : ss.getEpisodeList()) {
					for (Media md : ep.getMediaList()) {
						if (md.getFilename().endsWith("mkv")) {
							System.out.println(String.format("Checking subtitles on %s", md.getFilename()));					
							MediaMetadata mm = MediaMetadata.getMediaMetadata(md);
							if (mm != null && mm.getSubtitles().size() > 0) {
								System.out.println("Subs found. Clearing subtitlequeue");								
								movieIds.add(String.valueOf(ep.getSubtitleQueue().getID()));
							}
						}
					}					
				}
			}
		}
		
		DbScripts[0] = DbScripts[0].replace("__IDS__", String.join(",", movieIds));
		
		runDatabasescripts(DbScripts);		
	}

}
