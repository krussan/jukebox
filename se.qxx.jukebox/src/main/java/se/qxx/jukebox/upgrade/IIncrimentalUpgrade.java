package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.core.Version;

public interface IIncrimentalUpgrade {
	public Version getThisVersion();
	public Version getPreviousVersion();
	
	public void performUpgrade() throws UpgradeFailedException;
}
