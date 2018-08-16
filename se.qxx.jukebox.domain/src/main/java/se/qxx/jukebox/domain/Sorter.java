package se.qxx.jukebox.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public class Sorter {
	public static List<Subtitle> sortSubtitlesByRating(List<Subtitle> subs) {
		List<Subtitle> sortedSubtitles = new ArrayList<Subtitle>(subs);
		
		Collections.sort(sortedSubtitles, new Comparator<Subtitle>() {
			@Override
			public int compare(Subtitle lhs, Subtitle rhs) {
				return rhs.getRating().getNumber() - lhs.getRating().getNumber();
			}
		});		
		
		return sortedSubtitles;
	}
}
