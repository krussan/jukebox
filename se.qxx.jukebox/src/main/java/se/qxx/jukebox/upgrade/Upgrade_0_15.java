package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.tools.Util;

public class Upgrade_0_15 extends UpgraderBase implements IIncrimentalUpgrade {

	private IUtils utils = new Util();
	
	public Upgrade_0_15(IDatabase database) {
		super(database);
	}

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
		runDatabasescripts(DbScripts);

		try {
	
			// create thumbnails for each movie,series,season,episode
			List<Movie> movies = this.getDatabase().searchMoviesByTitle(StringUtils.EMPTY, false, false);
			for (Movie m: movies) {
				if (!m.getImage().isEmpty()){
					Movie m_new = Movie.newBuilder(m).setThumbnail(utils.getScaledImage(m.getImage())).build();
					this.getDatabase().save(m_new);
				}
			}
			
			List<Series> series = this.getDatabase().searchSeriesByTitle(StringUtils.EMPTY, false);
			for (Series s : series) {
				for (Season sn : s.getSeasonList()) {					
					for (Episode e : sn.getEpisodeList()) {
						if (!e.getImage().isEmpty()) {
							Episode e_new = Episode.newBuilder(e).setThumbnail(utils.getScaledImage(e.getImage())).build();
							DomainUtil.updateEpisode(sn, e_new);
						}
					}
					
					Season sn_new = sn;
					if (!sn.getImage().isEmpty())
						sn_new = Season.newBuilder(sn).setThumbnail(utils.getScaledImage(sn.getImage())).build();
					
					DomainUtil.updateSeason(s, sn_new);
				}
				
				Series s_new = s;
				if (!s.getImage().isEmpty())
					s_new = Series.newBuilder(s).setThumbnail(utils.getScaledImage(s.getImage())).build();
				
				this.getDatabase().save(s_new);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
	}

}
