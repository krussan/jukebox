package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;

public class Upgrade_0_18 extends UpgraderBase implements IIncrimentalUpgrade {
	
	public Upgrade_0_18(IDatabase database) {
		super(database);
	}

//	private static String[] DbScripts = {
//			"UPDATE SubtitleQueue\r\n" + 
//			"SET subtitleretreivedat = 0\r\n" + 
//			" , subtitleretreiveresult = 0\r\n" + 
//			"WHERE ID IN (__IDS__) "};
	
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
		// List<String> movieIds = new ArrayList<String>();
//		
//		List<Movie> movies = this.getDatabase().searchMoviesByTitle("");
//		for (Movie m : movies) {
//			for (Media md : m.getMediaList()) {
//				if (md.getFilename().endsWith("mkv")) {
//					System.out.println(String.format("Checking subtitles on %s", md.getFilename()));
//					MediaMetadataHelper helper = new MediaMetadataHelper(loggerFactory);
//					MediaMetadata mm = MediaMetadata.getMediaMetadata(md);
//					if (mm != null && mm.getSubtitles().size() > 0) {
//						System.out.println("Subs found. Clearing subtitlequeue");
//						movieIds.add(String.valueOf(m.getSubtitleQueue().getID()));
//					}
//				}
//			}
//		}
//		
//		List<Series> series = this.getDatabase().searchSeriesByTitle("");
//		for (Series s : series) {
//			for (Season ss : s.getSeasonList()) {
//				for (Episode ep : ss.getEpisodeList()) {
//					for (Media md : ep.getMediaList()) {
//						if (md.getFilename().endsWith("mkv")) {
//							System.out.println(String.format("Checking subtitles on %s", md.getFilename()));					
//							MediaMetadata mm = MediaMetadata.getMediaMetadata(md);
//							if (mm != null && mm.getSubtitles().size() > 0) {
//								System.out.println("Subs found. Clearing subtitlequeue");								
//								movieIds.add(String.valueOf(ep.getSubtitleQueue().getID()));
//							}
//						}
//					}					
//				}
//			}
//		}
//		
//		DbScripts[0] = DbScripts[0].replace("__IDS__", String.join(",", movieIds));
//		
//		runDatabasescripts(DbScripts);		
	}

}
