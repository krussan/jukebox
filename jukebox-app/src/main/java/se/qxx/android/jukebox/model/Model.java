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

    private String currentSub = StringUtils.EMPTY;
	private boolean initialized = false;

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean loading) {
		isLoading = loading;
	}

	private boolean isLoading = false;
	private static Model _instance;

	private List<String> _players;
	private List<Subtitle> _subs;
	private List<Series> _series;

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

}
