package se.qxx.jukebox.upgrade;

import se.qxx.jukebox.Version;

public class Upgrade_0_10 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"ALTER TABLE movie ADD " + 
	    " isTvEpisode bool NOT NULL DEFAULT 0," +
		" episode int NULL, " +
	    " firstAirDate date NULL, " +
		" _season_ID int NULL",

		"CREATE TABLE Season (" +
		" ID int NOT NULL PRIMARY KEY," +
		" rating varchar(5)," +
		" story varchar(1024)," +
		")",
		
		"CREATE TABLE SeasonGenre (" +
		"  _season_ID int NOT NULL CONSTRAINT FK_SeasonGenre_Season REFERENCES Seasion(ID)," +
		"  _genre_ID int NOT NULL CONSTRAINT FK_SeasonGenre_Genre REFERENCES Genre(ID)" +
		")"
				
//		"CREATE TABLE tv_seasons (" +
//		"   ID int NOT NULL PRIMARY KEY," +
//		" , 
		
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,10);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,9);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

	}
}
