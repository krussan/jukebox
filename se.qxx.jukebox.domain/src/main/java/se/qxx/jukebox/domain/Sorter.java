package se.qxx.jukebox.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleUri;

public class Sorter {
	public static List<Subtitle> sortSubtitlesByRating(List<Subtitle> subs) {
		List<Subtitle> sortedSubtitles = new ArrayList<>(subs);
		
		Collections.sort(sortedSubtitles, (lhs, rhs) -> rhs.getRating().getNumber() - lhs.getRating().getNumber());
		
		return sortedSubtitles;
	}

	public static List<SubtitleUri> sortSubtitlesUrisByRating(List<SubtitleUri> subs) {
		List<SubtitleUri> sortedSubtitles = new ArrayList<>(subs);

		Collections.sort(sortedSubtitles, (lhs, rhs) -> rhs.getSubtitle().getRating().getNumber() - lhs.getSubtitle().getRating().getNumber());

		return sortedSubtitles;
	}
}
