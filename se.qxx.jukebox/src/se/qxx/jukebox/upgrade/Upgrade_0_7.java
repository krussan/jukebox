package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.util.List;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.IMDBFinder;
import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class Upgrade_0_7 implements IIncrimentalUpgrade {

	static final String[] DbScripts = {
		"CREATE TABLE Media (" +
		"   ID integer PRIMARY KEY NOT NULL" +
		" , _movie_id int NOT NULL" +
		" , idx int NOT NULL" +
		" , filepath varchar(500)" +
		" , filename varchar(500)" +
		" , metaDuration int" +
		" , metaFramerate varchar(15) " +
		")",
		
		"INSERT INTO Media (_movie_id, idx, filename, filepath, metaDuration, metaFramerate) " +
		" SELECT _movie_id, 1, filename, filepath, metaDuration, metaFramerate " +
		"FROM Movie",
		
		"CREATE TABLE [xxMovie] (" +
		"  [ID] integer  PRIMARY KEY NOT NULL" +
		", [title] varchar(255)  NULL" +
		", [year] integer  NULL" +
		", [type] varchar(50)  NULL" +
		", [format] varchar(50)  NULL" +
		", [sound] varchar(50)  NULL" +
		", [language] varchar(50)  NULL" +
		", [groupName] varchar(50)  NULL" +
		", [imdburl] varchar(255)  NULL" +
		", duration int" +
		", rating varchar(5)" +
		", director varchar(100)" +
		", story varchar(1024)" +
		", identifier varchar(20)" +
		", identifierRating int" +
		", watched bit NOT NULL default (0))",
		
		"INSERT INTO xxMovie (ID, title, year, type, format, sound, language, groupName, imdburl, duration, rating, director, story, " +
		"	identifier, identifierRating, watched) " +
		"SELECT ID, title, year, type, format, sound, language, groupName, imdburl, duration, rating, director, story, " +
		"	identifier, identifierRating, watched " +
		"FROM Movie",
		
		"DROP TABLE Movie",
		
		"ALTER TABLE xxMovie RENAME TO Movie",
		
		"CREATE TABLE [xxSubtitles] (" +
		"   _media_ID int NOT NULL REFERENCES Media(ID)" +
		" , filename varchar(255)" +
		" , description varchar(255)" +
		" , rating varchar(30))",
		
		"INSERT INTO xxSubtitles (_media_ID, filename, description, rating)" +
		" SELECT MD.ID, S.filename, S.description, S.rating" +
		" FROM Movie M" +
		" INNER JOIN subtitles S ON S._movie_ID = M.ID" +
		" INNER JOIN Media MD ON MD._movie_ID = M.ID",
		
		"DROP TABLE subtitles",
		
		"ALTER TABLE xxSubtitles RENAME TO subtitles"
		
	};
	
	@Override
	public Version getThisVersion() {
		return new Version(0,7);
	}

	@Override
	public Version getPreviousVersion() {
		return new Version(0,6);
	}

	@Override
	public void performUpgrade() throws UpgradeFailedException {
		Upgrader.runDatabasescripts(DbScripts);

	}
}
