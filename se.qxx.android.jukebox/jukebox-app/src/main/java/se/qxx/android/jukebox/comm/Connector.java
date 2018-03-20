package se.qxx.android.jukebox.comm;

import com.google.protobuf.RpcCallback;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;

public class Connector {

	public static void connect(Activity a, int offset, int nrOfItems) {
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(),
				JukeboxSettings.get().getServerPort());

		try {
			Model.ModelType m = Model.get().getModelType();
			Model.get().setLoading(true);

			if (m == Model.ModelType.Movie) {
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
									Model.get().clearSeries();
									Model.get().addAllMovies(response.getMoviesList());
									Model.get().setInitialized(true);
								}

								Model.get().setLoading(false);

							}

						});
			}
			else if (m == Model.ModelType.Series) {
				Logger.Log().d("Listing series");
				jh.listMovies("",
						Model.get().getNrOfItems(),
						Model.get().getOffset(),
						new RpcCallback<JukeboxResponseListMovies>() {

							@Override
							public void run(JukeboxResponseListMovies response) {
								//TODO: if repsonse is null probably the server is down..
								if (response != null) {
									Model.get().clearMovies();
									//Model.get().clearSeries(); //Dont clear series when doing partial load
									Model.get().addAllSeries(response.getSeriesList());
									Model.get().setInitialized(true);
								}
							}

						});
			}
		} catch (Exception e) {
			showMessage(a, "Connection failed. Check settings ...");

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
