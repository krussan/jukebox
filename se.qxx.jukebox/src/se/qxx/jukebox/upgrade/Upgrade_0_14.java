package se.qxx.jukebox.upgrade;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class Upgrade_0_14 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"ALTER TABLE movie ADD identifiedTitle TEXT NULL",
		"UPDATE movie SET identifiedTitle = title"
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,14);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,13);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);
		
	}
}
