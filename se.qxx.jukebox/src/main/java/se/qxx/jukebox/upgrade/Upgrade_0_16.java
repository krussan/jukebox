package se.qxx.jukebox.upgrade;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;

import com.google.protobuf.ByteString;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.SubtitleDownloader;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.jukebox.domain.DomainUtil;

public class Upgrade_0_16 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"DELETE FROM SubtitleQueue",
		"UPDATE Movie SET _subtitlequeue_ID = NULL",
		"UPDATE Episode SET _subtitlequeue_ID = NULL",
		"ALTER TABLE Subtitle ADD [_textdata_ID] INTEGER NOT NULL REFERENCES BlobData (ID)"
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,16);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,15);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		//throw new UpgradeFailedException();
		Upgrader.runDatabasescripts(DbScripts);
		
		
		try {

			List<Movie> movies = DB.searchMoviesByTitle("");
			List<Series> series = DB.searchSeriesByTitle("");
					
			for (Movie m : movies) {
				DB.addMovieToSubtitleQueue(m);
			}
			
			for (Series s : series) {
				for (Season ss : s.getSeasonList()) {
					for (Episode e : ss.getEpisodeList()) {
						DB.addEpisodeToSubtitleQueue(e);
					}
				}
			}

		}
		catch (Exception ex) {
			System.out.println(ex);
			throw new UpgradeFailedException();
		}
		
		// Empty the subtitle queue
		// re-enlist all movies and episodes with the subtitle queue
		
	}
}
