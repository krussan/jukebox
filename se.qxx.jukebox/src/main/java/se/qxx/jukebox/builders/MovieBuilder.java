package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.exceptions.DeprecatedBuilderException;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.MovieOrSeries;
//import se.qxx.jukebox.domain.Slice;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Builders.Builder;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.settings.Settings;

public abstract class MovieBuilder {

	public MovieBuilder() {
	}

	public abstract MovieOrSeries extract(String filepath, String filename) throws DeprecatedBuilderException;

	protected ArrayList<String> getGroupsToCheck() {
		ArrayList<String> groupsToCheck = new ArrayList<String>();
		groupsToCheck.add("title");
		groupsToCheck.add("year");
		groupsToCheck.add("type");
		groupsToCheck.add("format");
		groupsToCheck.add("sound");
		groupsToCheck.add("language");
		groupsToCheck.add("group");
		return groupsToCheck;
	}

	/**
	 * Execute all enabled builders, perform rating and return the one with the best
	 * match.
	 * 
	 * @param filepath
	 * @param filename
	 * @return
	 */
	public static MovieOrSeries identify(String filepath, String filename) {
		ArrayList<MovieOrSeries> proposals = identifyAndRate(filepath, filename);

		return MovieBuilder.build(filepath, filename, proposals);
	}

	/**
	 * Builds the movie from the first proposal in the list
	 * 
	 * @param filepath
	 * @param filename
	 * @param proposals
	 * @param imdbUrl
	 * @param part
	 * @return The movie
	 */
	protected static MovieOrSeries build(String filepath, String filename, ArrayList<MovieOrSeries> proposals) {

		MovieOrSeries mos = null;
		if (proposals.size() > 0) {
			mos = proposals.get(0);

			if (mos != null) {
				Log.Debug(
						String.format("MovieBuilder :: Selected proposal (%s) has rating of %s", mos.getIdentifier(), mos.getIdentifierRating()),
						LogType.FIND);
			}
		} else {
			Log.Info(String.format("Failed to identify movie with filename %s", filename), LogType.FIND);
		}
		return mos;
	}

	/**
	 * Execute all builders, perform rating and returns a sorted list where the best
	 * match is the first
	 * 
	 * @param filepath
	 * @param filename
	 * @return A sorted list of proposals where the best match is the first
	 */
	protected static ArrayList<MovieOrSeries> identifyAndRate(String filepath, String filename) {
		ArrayList<MovieOrSeries> proposals = new ArrayList<MovieOrSeries>();

		for (Builder b : Settings.get().getBuilders().getBuilder()) {
			String className = b.getClazz();
			int weight = 1;
			if (b.getWeight() != null)
				weight = b.getWeight();

			try {
				if (b.isEnabled()) {
					Object o = Util.getInstance(className);
					if (o != null) {
						MovieOrSeries proposal = ((MovieBuilder) o).extract(filepath, filename);
						if (proposal != null && !proposal.isEmpty()) {
							proposal.setIdentifierRating(proposal.getIdentifierRating() * weight);

							// it this is a series and season and episode is not set then ignore the
							// proposal
							if (verifyProposal(proposal)) {
								proposals.add(proposal);
							} else {
								Log.Info(
										"MovieBuilder :: Series ignored since it failed to identify season and episode",
										LogType.FIND);

							}
						}
					}
				}
			} catch (Exception e) {
				Log.Error(String.format("Error when loading or executing movie builder %s", className),
						Log.LogType.FIND, e);
			}
		}

		Collections.sort(proposals, new MovieBuilderComparer());
		return proposals;
	}

	private static boolean verifyProposal(MovieOrSeries proposal) {
		return !proposal.isSeries() 
				|| (proposal.isSeries() 
						&& proposal.getSeries().getSeasonCount() > 0
						&& proposal.getSeries().getSeason(0).getEpisodeCount() > 0);
	}

	protected Media getMedia(String filepath, String filename) {
		return Media.newBuilder().setID(-1).setFilename(filename).setFilepath(filepath).setIndex(1)
				.setDownloadComplete(false).build();
	}

	/**
	 * Gets an instance of the builder that identified the movie
	 * 
	 * @param m
	 * @return A MovieBuilder representing the builder that identified the movie.
	 */
	// public static MovieBuilder getIdentifyingBuilder(Movie m) {
	// MovieBuilder mb = null;
	// switch (m.getIdentifier()) {
	// case Filename:
	// mb = new FilenameBuilder();
	// break;
	// case NFO:
	// mb = new NfoBuilder();
	// break;
	// case ParentDirectory:
	// mb = new ParentDirectoryBuilder();
	// break;
	// default:
	// mb = new FilenameBuilder();
	// }
	//
	// return mb;
	// }

}
