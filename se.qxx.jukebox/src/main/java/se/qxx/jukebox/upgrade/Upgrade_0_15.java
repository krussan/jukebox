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
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.domain.DomainUtil;

public class Upgrade_0_15 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"ALTER TABLE movie ADD [_thumbnail_ID] INTEGER NULL REFERENCES BlobData (ID)",
		"ALTER TABLE series ADD [_thumbnail_ID] INTEGER NULL REFERENCES BlobData (ID)",
		"ALTER TABLE season ADD [_thumbnail_ID] INTEGER NULL REFERENCES BlobData (ID)",
		"ALTER TABLE episode ADD [_thumbnail_ID] INTEGER NULL REFERENCES BlobData (ID)"
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,15);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,14);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

		try {
	
			// create thumbnails for each movie,series,season,episode
			List<Movie> movies = DB.searchMoviesByTitle(StringUtils.EMPTY);
			for (Movie m: movies) {
				if (!m.getImage().isEmpty()){
					Movie m_new = Movie.newBuilder(m).setThumbnail(Util.getScaledImage(m.getImage())).build();
					DB.save(m_new);
				}
			}
			
			List<Series> series = DB.searchSeriesByTitle(StringUtils.EMPTY);
			for (Series s : series) {
				for (Season sn : s.getSeasonList()) {					
					for (Episode e : sn.getEpisodeList()) {
						if (!e.getImage().isEmpty()) {
							Episode e_new = Episode.newBuilder(e).setThumbnail(Util.getScaledImage(e.getImage())).build();
							DomainUtil.updateEpisode(sn, e_new);
						}
					}
					
					Season sn_new = sn;
					if (!sn.getImage().isEmpty())
						sn_new = Season.newBuilder(sn).setThumbnail(Util.getScaledImage(sn.getImage())).build();
					
					DomainUtil.updateSeason(s, sn_new);
				}
				
				Series s_new = s;
				if (!s.getImage().isEmpty())
					s_new = Series.newBuilder(s).setThumbnail(Util.getScaledImage(s.getImage())).build();
				
				DB.save(s_new);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
	}

}
