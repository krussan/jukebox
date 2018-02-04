package se.qxx.jukebox.upgrade;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;

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
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.domain.DomainUtil;

public class Upgrade_0_18 implements IIncrimentalUpgrade {
	
	private static String[] DbScripts = {
			"UPDATE SubtitleQueue\r\n" + 
			"SET subtitleretreivedat = 0\r\n" + 
			" , subtitleretreiveresult = 0\r\n" + 
			"WHERE ID IN (\r\n" + 
			"  SELECT SQ.ID\r\n" + 
			"  FROM Movie M\r\n" + 
			"  INNER JOIN SubtitleQueue SQ\r\n" + 
			"    ON M._subtitlequeue_ID = SQ.ID\r\n" + 
			"  INNER JOIN MovieMedia_Media MDD\r\n" + 
			"    ON MDD._movie_ID = M.ID\r\n" + 
			"  INNER JOIN Media MD\r\n" + 
			"    ON MDD._media_ID = MD.ID\r\n" + 
			"  WHERE filename LIKE '%mkv'\r\n" + 
			")\r\n" 
			,
					
			"UPDATE SubtitleQueue\r\n" + 
			"SET subtitleretreivedat = 0\r\n" + 
			" , subtitleretreiveresult = 0\r\n" + 
			"WHERE ID IN (\r\n" +
			"  SELECT SQ.ID\r\n" + 			
			"FROM Episode EP " + 
			"INNER JOIN SubtitleQueue SQ " + 
			"  ON EP._subtitlequeue_ID = SQ.ID " + 
			"INNER JOIN EpisodeMedia_Media EMD " + 
			"  ON EMD._episode_ID = EP.ID " + 
			"INNER JOIN Media MD " + 
			"  ON EMD._media_ID = MD.ID " + 
			"  WHERE filename LIKE '%mkv'\r\n" + 
			")\r\n"};
	
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
		Upgrader.runDatabasescripts(DbScripts);		
	}

}
