package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.domain.DomainUtil;

public class Upgrade_0_17 extends UpgraderBase implements IIncrimentalUpgrade {
	
	public Upgrade_0_17(IDatabase database) {
		super(database);
	}

	@Override
	public Version getThisVersion() {
		return new Version(0,17);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,16);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		try {
	
			// create thumbnails for each movie,series,season,episode
			List<Movie> movies = this.getDatabase().searchMoviesByTitle(StringUtils.EMPTY);
			for (Movie m: movies) {
				if (!m.getImage().isEmpty() && m.getThumbnail().isEmpty()){
					System.out.println(String.format("Creating thumbnail for %s", m.getTitle()));
					Movie m_new = Movie.newBuilder(m).setThumbnail(Util.getScaledImage(m.getImage())).build();
					this.getDatabase().save(m_new);
				}
			}
			
			List<Series> series = this.getDatabase().searchSeriesByTitle(StringUtils.EMPTY);
			for (Series s : series) {
				for (Season sn : s.getSeasonList()) {					
					for (Episode e : sn.getEpisodeList()) {
						if (!e.getImage().isEmpty() && e.getThumbnail().isEmpty()) {
							System.out.println(String.format("Creating thumbnail for %s S%sE%s", s.getTitle(), sn.getSeasonNumber(), e.getEpisodeNumber()));
							Episode e_new = Episode.newBuilder(e).setThumbnail(Util.getScaledImage(e.getImage())).build();
							DomainUtil.updateEpisode(sn, e_new);
						}
					}
					
					Season sn_new = sn;
					if (!sn.getImage().isEmpty() && sn.getThumbnail().isEmpty()) {
						System.out.println(String.format("Creating thumbnail for %s season %s", s.getTitle(), sn.getSeasonNumber()));
						sn_new = Season.newBuilder(sn).setThumbnail(Util.getScaledImage(sn.getImage())).build();
					}
					
					DomainUtil.updateSeason(s, sn_new);
				}
				
				Series s_new = s;
				if (!s.getImage().isEmpty() && s.getThumbnail().isEmpty()) {
					System.out.println(String.format("Creating master thumbnail for %s", s.getTitle()));
					
					s_new = Series.newBuilder(s).setThumbnail(Util.getScaledImage(s.getImage())).build();
				}
				
				this.getDatabase().save(s_new);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new UpgradeFailedException();
		}
	}

}
