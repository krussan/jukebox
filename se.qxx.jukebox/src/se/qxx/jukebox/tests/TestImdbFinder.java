package se.qxx.jukebox.tests;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.IMDBFinder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class TestImdbFinder {
	public static void main(String[] args) throws Exception {
		Settings.initialize();

		if (args.length > 0 && !StringUtils.isEmpty(args[0])) {
			Movie m = Movie.newBuilder()
					.setID(-1)
					.setTitle(args[0])
					.build();
			Movie mm = IMDBFinder.Get(m);
			
			System.out.println(mm);
		}
	}
}
