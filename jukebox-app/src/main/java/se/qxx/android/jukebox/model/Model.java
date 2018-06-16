package se.qxx.android.jukebox.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.Typed;

import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.MovieOrSeries;

public class Model {

    public enum ModelType {
        Movie,
		Series,
		Season,
		Episode
    }

	private ModelType modelType = ModelType.Movie;
	private int currentMovieId = -1;
	private int currentSeriesId = -1;
	private int currentSeasonId = -1;
    private int currentEpisodeId = -1;

	private String currentSub = StringUtils.EMPTY;
	private int currentMediaId = -1;
	private boolean initialized = false;

	private int offset = 0;
	private int nrOfItems = 15;

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean loading) {
		isLoading = loading;
	}

	private boolean isLoading = false;

	public interface ModelUpdatedEventListener {
		public void handleModelUpdatedEventListener(java.util.EventObject e, List<Object> objects);
	}

	private List<ModelUpdatedEventListener> _listeners = new ArrayList<Model.ModelUpdatedEventListener>();
	
	public synchronized void addEventListener(ModelUpdatedEventListener listener) {
		_listeners.add(listener);
	}
	
	public synchronized void removeEventListener(ModelUpdatedEventListener listener) {
		_listeners.remove(listener);
	}
	
	private synchronized void fireModelUpdatedEvent(ModelUpdatedType type) {
		ModelUpdatedEvent event = new ModelUpdatedEvent(this, type);
		Iterator<ModelUpdatedEventListener> i = _listeners.iterator();

		while(i.hasNext())  {
			i.next().handleModelUpdatedEventListener(event);
		}
	}

	private static Model _instance;

	private List<String> _players;
	private List<Subtitle> _subs;
	private List<Series> _series;

    public Season getSeason() {
        return _season;
    }

    public void setSeason(Season _season) {
        this._season = _season;
        fireModelUpdatedEvent(ModelUpdatedType.Season);
    }

    private Season _season;
	
	private Model() {
		_players = new ArrayList<String>();
		_subs = new ArrayList<Subtitle>();
		_series = new ArrayList<Series>();
	}
	
	public static Model get() {
		if (_instance == null)
			_instance = new Model();
		
		return _instance;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}


	public Series getCurrentSeries() {
		if (this.currentSeriesId >= 0 && this.currentSeriesId < this._series.size())
			return this._series.get(this.currentSeriesId);
		else
			return null;
	}

	public int getCurrentSeriesIndex() {
		return this.currentSeriesId;
	}

    public Season getCurrentSeason() {
        Series series = this.getCurrentSeries();

        if (series != null) {
            if (this.currentSeasonId >= 0 && this.currentSeasonId < series.getSeasonCount())
                return series.getSeason(this.currentSeasonId);
            else
                return null;
        }

        return null;
    }

    public int getCurrentEpsiodeIndex() {
        return this.currentEpisodeId;
    }

    public Episode getCurrentEpisode() {
        Season season = this.getCurrentSeason();

        if (season != null) {
            if (this.currentEpisodeId >= 0 && this.currentEpisodeId < season.getEpisodeCount())
                return season.getEpisode(this.currentEpisodeId);
            else
                return null;
        }

        return null;
    }

    public int getCurrentSeasonIndex() {
        return this.currentSeasonId;
    }



	public Series getPreviousSeries() {
		if (this.currentSeriesId == 0)
			return this._series.get(this._series.size() == 0 ? 0 : this._series.size() - 1);
		else
			return this._series.get(this.currentSeriesId - 1);
	}

	public Series getNextSeries() {
		if (this.currentSeriesId == this._series.size() - 1)
			return this._series.get(0);
		else
			return this._series.get(this.currentSeriesId + 1);
	}

    public Season getPreviousSeason() {
        Series series = this.getCurrentSeries();

        if (series != null) {
            if (this.currentSeasonId == 0)
                return series.getSeason(series.getSeasonCount() == 0 ? 0 : series.getSeasonCount() - 1);
            else
                return series.getSeason(this.currentSeasonId - 1);
        }

        return null;
    }

    public Season getNextSeason() {
        Series series = this.getCurrentSeries();

        if (series != null) {
            if (this.currentSeasonId == series.getSeasonCount() - 1)
                return series.getSeason(0);
            else
                return series.getSeason(this.currentSeasonId + 1);
        }

        return null;
    }


	public void currentSeriesSetNext() {
		this.currentSeriesId++;
		this.currentMediaId = 0;
		if (this.currentSeriesId == this._series.size())
			this.currentSeriesId = 0;
	}

	public void currentSeriesSetPrevious() {
		this.currentSeriesId--;
		this.currentMediaId = 0;
		if (this.currentSeriesId < 0)
			this.currentSeriesId = this._series.size() == 0 ? 0 : this._series.size() - 1;
	}

    public void currentSeasonSetNext() {
        Series series = this.getCurrentSeries();

        if (series != null) {
            this.currentSeasonId++;
            this.currentMediaId = 0;
            if (this.currentSeasonId == series.getSeasonCount())
                this.currentSeasonId = 0;
        }
    }

    public void currentSeasonSetPrevious() {
        Series series = this.getCurrentSeries();

        if (series != null) {
            this.currentSeasonId--;
            this.currentMediaId = 0;
            if (this.currentSeasonId < 0)
                this.currentSeasonId = series.getSeasonCount() == 0 ? 0 : series.getSeasonCount() - 1;
            }
    }

	public void setCurrentMovie(int index) {
        this.setModelType(ModelType.Movie);
		this.currentMovieId = index;
		this.currentMediaId = 0;		
	}

	public void setCurrentSeries(int index) {
        this.setModelType(ModelType.Series);
		this.currentSeriesId = index;
		this.currentMediaId = 0;
	}

    public void setCurrentSeason(int index) {
        this.setModelType(ModelType.Series);
        this.currentSeasonId = index;
        this.currentMediaId = 0;
    }

    public void setCurrentEpisode(int index) {
        this.setModelType(ModelType.Series);
        this.currentEpisodeId = index;
        this.currentMediaId = 0;
    }

	//---------------------------------------------------------------------------------------
	// SERIES
	//---------------------------------------------------------------------------------------

	public int countSeries() {
		return this._series.size();
	}

	public Series getSeries(int position) {
		return _series.get(position);
	}
	
	//---------------------------------------------------------------------------------------
	// MEDIA
	//---------------------------------------------------------------------------------------



	//---------------------------------------------------------------------------------------
	// PLAYERS
	//---------------------------------------------------------------------------------------
	
	public List<String> getPlayers() {
		return _players;
	}
	
	public void addAllPlayers(List<String> players) {
		_players.addAll(players);
		Collections.sort(_players);
		
		fireModelUpdatedEvent(ModelUpdatedType.Players);
	}
	
	public void clearPlayers() {
		_players.clear();
		fireModelUpdatedEvent(ModelUpdatedType.Players);
	}
	
	public int countPlayers() {
		return _players.size();
	}
	
	public String getPlayer(int index) {
		return _players.get(index);
	}
	
	//---------------------------------------------------------------------------------------
	// SUBTITLES
	//---------------------------------------------------------------------------------------
	
	public List<Subtitle> getSubtitles() {
		return _subs;
	}
	
	public String[] getSubtitleDescriptions() {
		List<String> strings = new ArrayList<String>();
		for (Subtitle s : this._subs) {
			String desc = s.getDescription();
			
			if (StringUtils.isEmpty(desc))
				strings.add(s.getFilename());
			else
				strings.add(desc);
		}

		return  (String[])strings.toArray(new String[0]);
	}
	
	public void addAllSubtitles(List<Subtitle> subs) {
		_subs.addAll(subs);
		
		fireModelUpdatedEvent(ModelUpdatedType.Subs);
	}
	
	public void clearSubtitles() {
		_subs.clear();
		fireModelUpdatedEvent(ModelUpdatedType.Subs);
	}
	
	public int countSubtitles() {
		return _subs.size();
	}
	
	public Subtitle getSubtitle(int index) {
		return _subs.get(index);
	}

//	public void setCurrentSubtitle(int id) {
//		currentSubId = id;
//		fireModelUpdatedEvent(ModelUpdatedType.CurrentSub);		
//	}

	public void setCurrentSubtitle(String description) {
		this.currentSub = description;
	}
	
	public String getCurrentSubtitle() {
		return this.currentSub;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getNrOfItems() {
		return nrOfItems;
	}

	public void setNrOfItems(int nrOfItems) {
		this.nrOfItems = nrOfItems;
	}

}
