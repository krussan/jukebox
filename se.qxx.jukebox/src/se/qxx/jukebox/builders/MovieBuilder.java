package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Builders.Builder;
import se.qxx.jukebox.settings.Settings;

public abstract class MovieBuilder {
	
	public MovieBuilder() {
	}
	
	public abstract Movie extractMovie(String filepath, String filename);
	
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
	 * Execute all enabled builders, perform rating and return the one with the best match.
	 * @param filepath
	 * @param filename
	 * @return
	 */
	public static Movie identifyMovie(String filepath, String filename) {
		PartPattern pp = new PartPattern(filename);
				
		ArrayList<Movie> proposals = identifyAndRate(filepath, pp.getResultingFilename());
		
		// check if one of the proposals has identified the movie and added an imdb url
		String imdbUrl = checkImdbUrl(proposals);
		
		//TODO: Add TV episode to this function
		return buildMovie(filepath, filename, proposals, imdbUrl, pp);
	
	}
	
	
	/**
	 * Builds the movie from the first proposal in the list
	 * @param filepath 
	 * @param filename
	 * @param proposals
	 * @param imdbUrl
	 * @param part
	 * @return The movie
	 */
	protected static Movie buildMovie(
			String filepath, 
			String filename, 
			ArrayList<Movie> proposals, 
			String imdbUrl,
			PartPattern pp) {
		
		Movie m = null;
		if (proposals.size() > 0) {
			m = proposals.get(0);
			
			if (m != null)  {
				Log.Debug(String.format("MovieBuilder :: Selected proposal has rating of %s", m.getIdentifierRating()), LogType.FIND);
				
				Media md = Media.newBuilder()
						.setID(-1)
						.setIndex(pp.getPart())
						.setFilename(filename)
						.setFilepath(filepath)
						.build();
				
				Season s = Season.newBuilder()
						.setID(-1)
						.setSeasonNumber(pp.getSeason())
						.build();
				
				Movie.Builder builder = Movie.newBuilder(m)
						.setIsTvEpisode(pp.isTvEpisode())
						.setEpisode(pp.getEpisode())
						.setSeason(s)
						.addMedia(md);

				// If a Imdb Link has been found in one of the builders
				// then merge it into this one
				if (!StringUtils.isEmpty(imdbUrl))
					builder.setImdbUrl(imdbUrl);
				
				m = builder.build();
				
			}
		}
		else {
			Log.Info(String.format("Failed to identify movie with filename %s", filename), LogType.FIND);
		}
		return m;
	}

	/**
	 * Checks the list of proposals and returns if one has identified an IMDB url
	 * @param proposals
	 * @return The first IMDB url found
	 */
	private static String checkImdbUrl(ArrayList<Movie> proposals) {
		String imdbUrl = StringUtils.EMPTY;
		for (Movie m : proposals) {
			if (!StringUtils.isEmpty(m.getImdbUrl()) ) {
				imdbUrl = m.getImdbUrl();				
				Log.Debug(String.format("MovieBuilder :: ImdbUrl found :: %s", imdbUrl), LogType.FIND);								
				return imdbUrl;
			}			
		}
		
		return imdbUrl;
	}

	/**
	 * Execute all builders, perform rating and returns a sorted list where the best match is the first
	 * @param filepath
	 * @param filename
	 * @return A sorted list of proposals where the best match is the first
	 */
	protected static ArrayList<Movie> identifyAndRate(String filepath, String filename) {
		ArrayList<Movie> proposals = new ArrayList<Movie>();		
		
		for (Builder b : Settings.get().getBuilders().getBuilder()) {
			String className = b.getClazz();
			int weight = 1;
			if (b.getWeight() != null)
				weight = b.getWeight();
			
			try {
				if (b.isEnabled()) {
					Object o = Util.getInstance(className);
					if (o != null) {
						Movie proposal = ((MovieBuilder)o).extractMovie(filepath, filename);
						if (proposal != null) {
							proposal = Movie.newBuilder(proposal)
									.setIdentifierRating(proposal.getIdentifierRating() * weight)
									.build();
							
							
							proposals.add(proposal);
						}
					}
				}
			} catch (Exception e) {
				Log.Error(String.format("Error when loading or executing movie builder %s", className), Log.LogType.FIND, e);
			}
		}
		
		Collections.sort(proposals, new MovieBuilderComparer());
		return proposals;
	}
	
	/**
	 * Gets an instance of the builder that identified the movie
	 * @param m
	 * @return A MovieBuilder representing the builder that identified the movie.
	 */
	public static MovieBuilder getIdentifyingBuilder(Movie m) {
		MovieBuilder mb = null;
		switch (m.getIdentifier()) {
		case Filename:
			mb = new FilenameBuilder();
			break;
		case NFO:
			mb = new NfoBuilder();
			break;
		case ParentDirectory:
			mb = new ParentDirectoryBuilder();
			break;
		default:
			mb = new FilenameBuilder();
		}
		
		return mb;
	}

}
