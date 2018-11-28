package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Builders.Builder;
import se.qxx.jukebox.tools.Util;

public class MovieBuilderFactory implements IMovieBuilderFactory {

	private ISettings settings;
	private IJukeboxLogger log;
	
	@Inject
	public MovieBuilderFactory(ISettings settings, LoggerFactory loggerFactory) {
		this.setSettings(settings);
		this.setLog(loggerFactory.create(LogType.FIND));
	}
	
	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}
	@Override
	public MovieOrSeries identify(String filepath, String filename) {
		ArrayList<MovieOrSeries> proposals = identifyAndRate(filepath, filename);

		return build(filepath, filename, proposals);
	}

	@Override
	public MovieOrSeries build(String filepath, String filename, List<MovieOrSeries> proposals) {

		MovieOrSeries mos = null;
		if (proposals.size() > 0) {
			mos = proposals.get(0);

			if (mos != null) {
				this.getLog().Debug(
					String.format(
						"MovieBuilder :: Selected proposal (%s) has rating of %s", 
							mos.getIdentifier(), 
							mos.getIdentifierRating()));
			}
		} else {
			this.getLog().Info(String.format("Failed to identify movie with filename %s", filename));
		}
		
		mos = checkSeriesEpisode(mos, proposals);
		return mos;
	}
	
	/***
	 * If MovieOrSeries is a series then check that both episode and season are set
	 * If not search the proposals and update the object with the first that has
	 * both set
	 * @param mos
	 * @param proposals
	 * @return
	 */
	private MovieOrSeries checkSeriesEpisode(MovieOrSeries mos, List<MovieOrSeries> proposals) {
		if (mos.isSeries()) {
			if (!seriesHasSeasonAndEpisode(mos)) {
				for (int i=1;i<proposals.size(); i++) {
					MovieOrSeries otherMos = proposals.get(i);
					if (seriesHasSeasonAndEpisode(otherMos)) {
						mos.setSeasonAndEpisode(
							otherMos.getSeason().getSeasonNumber(), 
							otherMos.getEpisode().getEpisodeNumber());
						break;
					}
				}
			}
		}
			
		return mos;
	}
	
	private boolean seriesHasSeasonAndEpisode(MovieOrSeries mos) {
		return mos.getEpisode().getEpisodeNumber() > 0
			&& mos.getSeason().getSeasonNumber() > 0;
	}

	/**
	 * Execute all builders, perform rating and returns a sorted list where the best
	 * match is the first
	 * 
	 * @param filepath
	 * @param filename
	 * @return A sorted list of proposals where the best match is the first
	 */
	protected ArrayList<MovieOrSeries> identifyAndRate(String filepath, String filename) {
		ArrayList<MovieOrSeries> proposals = new ArrayList<MovieOrSeries>();

		Class<?>[] parTypes = new Class<?>[] {ISettings.class, IJukeboxLogger.class};
		Object[] args = new Object[] {this.getSettings(), this.getLog()};
		
		for (Builder b : this.getSettings().getSettings().getBuilders().getBuilder()) {
			String className = b.getClazz();
			int weight = 1;
			if (b.getWeight() != null)
				weight = b.getWeight();

			try {
				if (b.isEnabled()) {
					Object o = Util.getInstance(className, parTypes, args);
					if (o != null) {
						MovieOrSeries proposal = ((MovieBuilder) o).extract(filepath, filename);
						if (proposal != null && !proposal.isEmpty()) {
							proposal.setIdentifierRating(proposal.getIdentifierRating() * weight);

							// it this is a series and season and episode is not set then ignore the
							// proposal
							if (verifyProposal(proposal)) {
								proposals.add(proposal);
							} else {
								this.getLog().Info("MovieBuilder :: Series ignored since it failed to identify season and episode");

							}
						}
					}
				}
			} catch (Exception e) {
				this.getLog().Error(String.format("Error when loading or executing movie builder %s", className), e);
			}
		}

		Collections.sort(proposals, new MovieBuilderComparer());
		return proposals;
	}

	
	private boolean verifyProposal(MovieOrSeries proposal) {
		return !proposal.isSeries() 
				|| (proposal.isSeries() 
						&& proposal.getSeries().getSeasonCount() > 0
						&& proposal.getSeries().getSeason(0).getEpisodeCount() > 0);
	}
}
