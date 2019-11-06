package se.qxx.android.jukebox.comm;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class Connector {

    private ConnectorCallbackEventListener callback;
    private final JukeboxSettings settings;

    public ConnectorCallbackEventListener getCallback() {
        return callback;
    }

    public void setCallback(ConnectorCallbackEventListener callback) {
        this.callback = callback;
    }

    public interface ConnectorCallbackEventListener {
        void handleMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies);
        void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries);
        void handleSeasonsUpdated(List<JukeboxDomain.Season> seasons, int totalSeasons);
        void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes, int totalEpisodes);
    }

    public Connector(ConnectorCallbackEventListener callback, JukeboxSettings settings) {
        this.callback = callback;
        this.settings = settings;
    }
/*
    private static final synchronized void fireMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies){
        Iterator<ConnectorCallbackEventListener> i = _listeners.iterator();

        while(i.hasNext())
            i.next().handleMoviesUpdated(movies, totalMovies);

    }
    private static final synchronized void fireSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries) {
        Iterator<ConnectorCallbackEventListener> i = _listeners.iterator();

        while(i.hasNext())
            i.next().handleSeriesUpdated(series, totalSeries);
    }

    private static final void fireSeasonsUpdated(List<JukeboxDomain.Season> seasons, int totalSeasons) {
        Iterator<ConnectorCallbackEventListener> i = _listeners.iterator();

        while(i.hasNext())
            i.next().handleSeasonsUpdated(seasons, totalSeasons);
    }

    private static final void fireEpisodesUpdated(List<JukeboxDomain.Episode> episodes, int totalEpisodes) {
        Iterator<ConnectorCallbackEventListener> i = _listeners.iterator();

        while(i.hasNext())
            i.next().handleEpisodesUpdated(episodes, totalEpisodes);
    }
*/

	public void connect(final int offset, final int nrOfItems, ViewMode modelType, final int seriesID, int seasonID, boolean excludeImages, boolean excludeTextdata) {
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				settings.getServerIpAddress(),
				settings.getServerPort());

		try {
			if (modelType == ViewMode.Movie) {
				Logger.Log().d("Listing movies");
				jh.listMovies("",
						nrOfItems,
						offset,
						excludeImages,
						excludeTextdata,
						response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
                                this.getCallback().handleMoviesUpdated(response.getMoviesList(), response.getTotalMovies());
                            }
                        });
			}
			else if (modelType == ViewMode.Series) {
			    jh.listSeries("",
                        nrOfItems,
                        offset,
						excludeImages,
						excludeTextdata,
						response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
                                this.getCallback().handleSeriesUpdated(response.getSeriesList(), response.getTotalSeries());
                            }
                        });
			}
            else if (modelType == ViewMode.Season) {
				jh.getItem(seriesID, JukeboxDomain.RequestType.TypeSeries, excludeImages, excludeTextdata,
                        response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
								this.getCallback().handleSeasonsUpdated(
										response.getSerie().getSeasonList(),
										response.getSerie().getSeasonCount());

                            }
                        });
            }
			else if (modelType == ViewMode.Episode) {
			    jh.getItem(seasonID, JukeboxDomain.RequestType.TypeSeason, excludeImages, excludeTextdata,
                        response -> {
			                if (response != null) {
                                this.getCallback().handleEpisodesUpdated(response.getSeason().getEpisodeList(), response.getSeason().getEpisodeCount());
                            }
                        });
			}

		} catch (Exception e) {

		}
	}

	
	public void showMessage(Activity a, final String message) {
		final Context c = (Context)a;
		a.runOnUiThread(() -> Toast.makeText(c, message, Toast.LENGTH_SHORT).show());
	}

	public void onoff(Activity a) {
		// TODO: Check if computer is live.
		final boolean isOnline = settings.isCurrentMediaPlayerOn();
		final String currentMediaPlayer = settings.getCurrentMediaPlayer();
	
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				settings.getServerIpAddress(),
				settings.getServerPort(),
				JukeboxConnectionProgressDialog.build(a,
						isOnline ? "Suspending target media player..."
								: "Waking up..."));
	
		Thread t = new Thread(() -> {
            if (isOnline)
                jh.suspend(currentMediaPlayer);
            else
                jh.wakeup(currentMediaPlayer);
        });
		t.start();
	
        settings.setIsCurrentMediaPlayerOn(!isOnline);
	}
	
	public void setupOnOffButton(View v) {
		if (settings.isCurrentMediaPlayerOn()) {
			GUITools.showView(R.id.btnOff, v);
			GUITools.hideView(R.id.btnOn, v);
		} else {
			GUITools.showView(R.id.btnOn, v);
			GUITools.hideView(R.id.btnOff, v);
		}
	}
	
}
