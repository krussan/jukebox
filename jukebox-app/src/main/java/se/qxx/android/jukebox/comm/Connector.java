package se.qxx.android.jukebox.comm;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

public class Connector {
    public interface ConnectorCallbackEventListener {
        void handleMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies);
        void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries);
        void handleSeasonsUpdated(List<JukeboxDomain.Season> seasons, int totalSeasons);
        void handleEpisodesUpdated(List<JukeboxDomain.Episode> episodes, int totalEpisodes);
    }

    public static synchronized void addEventListener(ConnectorCallbackEventListener listener) {
        // only add once
        for (ConnectorCallbackEventListener l : _listeners) {
            if (l.equals(listener))
                return;
        }

        _listeners.add(listener);
    }

    private static List<ConnectorCallbackEventListener> _listeners = new ArrayList<ConnectorCallbackEventListener>();

    public static synchronized void removeEventListener(ConnectorCallbackEventListener listener) {
        _listeners.remove(listener);
    }

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

	public static void connect(final int offset, final int nrOfItems, Model.ModelType modelType, final int seriesID, int seasonID) {
		Model.get().setModelType(modelType);

		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(),
				JukeboxSettings.get().getServerPort());

		try {
			Model.get().setLoading(true);

			if (modelType == Model.ModelType.Movie) {
				Logger.Log().d("Listing movies");
				jh.listMovies("",
						nrOfItems,
						offset,
						new RpcCallback<JukeboxResponseListMovies>() {

							@Override
							public void run(JukeboxResponseListMovies response) {
								//TODO: if repsonse is null probably the server is down..
								if (response != null) {
									//Model.get().clearMovies(); //Dont clear movies when doing partial load
								fireMoviesUpdated(response.getMoviesList(), response.getTotalMovies());
									Model.get().setInitialized(true);
								}

								Model.get().setLoading(false);

							}

						});
			}
			else if (modelType == Model.ModelType.Series) {
			    jh.listSeries("",
                        nrOfItems,
                        offset,
						new RpcCallback<JukeboxResponseListMovies>() {

							@Override
							public void run(JukeboxResponseListMovies response) {
								//TODO: if repsonse is null probably the server is down..
								if (response != null) {
								    fireSeriesUpdated(response.getSeriesList(), response.getTotalSeries());
									Model.get().setInitialized(true);
								}

								Model.get().setLoading(false);
							}

						});
			}
            else if (modelType == Model.ModelType.Season) {
                jh.listSeasons("",
                        seriesID,
                        nrOfItems,
                        offset,
                        response -> {
                            //TODO: if repsonse is null probably the server is down..
                            if (response != null) {
                                if (response.getSeriesCount() > 0)
                                    fireSeasonsUpdated(response.getSeries(0).getSeasonList(), response.getTotalSeasons());

                                Model.get().setInitialized(true);
                            }

                            Model.get().setLoading(false);
                        });
            }
			else if (modelType == Model.ModelType.Episode) {
			    jh.listEpisodes("",
                        seriesID,
                        seasonID,
                        nrOfItems,
                        offset,
                        response -> {
			                if (response != null) {
                                if (response.getSeasonCount() > 0)
                                    fireEpisodesUpdated(response.getSeason(0).getEpisodeList(), response.getTotalEpisodes());

                                Model.get().setInitialized(true);
                            }
                        });
			}

		} catch (Exception e) {

		}

		Model.get().setLoading(false);
	}

	
	public static void showMessage(Activity a, final String message) {
		final Context c = (Context)a;
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static void onoff(Activity a) {
		// TODO: Check if computer is live.
		final boolean isOnline = JukeboxSettings.get().isCurrentMediaPlayerOn();
		final String currentMediaPlayer = JukeboxSettings.get()
				.getCurrentMediaPlayer();
	
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(), 
				JukeboxSettings.get().getServerPort(),				
				JukeboxConnectionProgressDialog.build(a,
						isOnline ? "Suspending target media player..."
								: "Waking up..."));
	
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (isOnline)
					jh.suspend(currentMediaPlayer);
				else
					jh.wakeup(currentMediaPlayer);
			}
		});
		t.start();
	
		JukeboxSettings.get().setIsCurrentMediaPlayerOn(!isOnline);
	}
	
	public static void setupOnOffButton(View v) {
		if (JukeboxSettings.get().isCurrentMediaPlayerOn()) {
			GUITools.showView(R.id.btnOff, v);
			GUITools.hideView(R.id.btnOn, v);
		} else {
			GUITools.showView(R.id.btnOn, v);
			GUITools.hideView(R.id.btnOff, v);
		}
	}
	
}
