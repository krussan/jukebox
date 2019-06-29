package se.qxx.jukebox.upgrade;

import java.util.List;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_16 extends UpgraderBase implements IIncrimentalUpgrade {

	public Upgrade_0_16(IDatabase database) {
		super(database);
	}

	static final String[] DbScripts = {
		"UPDATE Movie SET _subtitlequeue_ID = NULL",
		"UPDATE Episode SET _subtitlequeue_ID = NULL",
		"DELETE FROM SubtitleQueue",
		"ALTER TABLE Subtitle ADD [_textdata_ID] INTEGER NULL REFERENCES BlobData (ID) "
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
		runDatabasescripts(DbScripts);
		
		
		try {

			System.out.println("Re-adding all media to subtitle queue");
			
			List<Movie> movies = this.getDatabase().searchMoviesByTitle("",true, true);
			List<Series> series = this.getDatabase().searchSeriesByTitle("", true);
			
			int mSize = movies.size();

			for (int i=0;i<mSize; i++) {
				System.out.println(String.format("Adding movie [%s/%s]", i, mSize));
				this.getDatabase().addMovieToSubtitleQueue(movies.get(i));
			}
			
			int sSize = series.size(); 
			
			for (int i=0;i<sSize;i++) {
				Series s = series.get(i);
				int ssSize = s.getSeasonCount();
				
				for (int j=0;j<ssSize;j++) {
					Season ss = s.getSeason(j);
					int eSize = ss.getEpisodeCount();
					
					for (int k=0;k<eSize;k++) {
						Episode e = ss.getEpisode(k);
						
						System.out.println(String.format("Adding episode [%s/%s] [%s/%s] [%s/%s]", i, sSize, j, ssSize, k, eSize));
						this.getDatabase().addEpisodeToSubtitleQueue(e);
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
