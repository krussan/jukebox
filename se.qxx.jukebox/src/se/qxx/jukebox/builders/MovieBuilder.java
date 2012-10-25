package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
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
		ArrayList<Movie> proposals = new ArrayList<Movie>();
		String imdbUrl = StringUtils.EMPTY;
		
		// Execute all enabled builders, perform rating and return the one with the best match.
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
							
							if (!StringUtils.isEmpty(proposal.getImdbUrl()) && StringUtils.isEmpty(imdbUrl)) {
								imdbUrl = proposal.getImdbUrl();
								Log.Debug(String.format("MovieBuilder :: ImdbUrl found :: %s", imdbUrl), LogType.FIND);								
							}
							
							proposals.add(proposal);
						}
					}
				}
			} catch (Exception e) {
				Log.Error(String.format("Error when loading or executing movie builder %s", className), Log.LogType.FIND, e);
			}
		}
		
		Collections.sort(proposals, new MovieBuilderComparer());

		Movie m = null;
		if (proposals.size() > 0) {
			m = proposals.get(0);
			
			if (m != null)  {
				Log.Debug(String.format("MovieBuilder :: Selected proposal has rating of %s", m.getIdentifierRating()), LogType.FIND);
				se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder builder = Movie.newBuilder(m)
						.setFilename(filename)
						.setFilepath(filepath);

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
