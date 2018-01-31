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
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.domain.DomainUtil;

public class Upgrade_0_18 implements IIncrimentalUpgrade {
	
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
		// Find all media where the filename ends with mkv
		// re-enlist those media to the subtitle downloader (triggering the extraction of embedded mkv subs)
		
		List<Movie> movies = DB.searchMoviesByTitle("%", true, false);
		for (Movie m : movies) {
			for (Media md : m.getMediaList()) {
				System.out.println(String.format("Running matroska discovery on :: %s", md.getFilename()));
				if (Util.isMatroskaFile(md)) {
					System.out.println("---- MKV FOUND - reenlisting ---");
					SubtitleDownloader.get().reenlistMovie(m);
					break;
				}
			}
		}
		
		List<Series> series = DB.searchSeriesByTitle("%", true, false);
		for (Series s : series) {
			for (Season ss : s.getSeasonList()) {
				for (Episode ep : ss.getEpisodeList()) {
					for (Media md : ep.getMediaList()) {
						System.out.println(String.format("Running matroska discovery on :: %s", md.getFilename()));
						if (Util.isMatroskaFile(md)) {
							System.out.println("---- MKV FOUND - reenlisting ---");
							SubtitleDownloader.get().reenlistEpisode(ep);
							break;
						}
					}					
				}
			}
		}
		
	}

}
