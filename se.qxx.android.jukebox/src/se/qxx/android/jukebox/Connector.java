package se.qxx.android.jukebox;

import com.google.protobuf.RpcCallback;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;

public class Connector {

	public static void connect(Activity a) {
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(), 
				JukeboxSettings.get().getServerPort(),				
				JukeboxConnectionProgressDialog.build(a,
						"Getting list of media ..."));

		try {
			jh.listMovies("", new RpcCallback<JukeboxResponseListMovies>() {

				@Override
				public void run(JukeboxResponseListMovies response) {
					//TODO: if repsonse is null probably the server is down..
					if (response != null) {
						Model.get().clearMovies();
						Model.get().addAllMovies(response.getMoviesList());
						Model.get().addAllSeries(response.getSeriesList());
						Model.get().setInitialized(true);
					}
				}

			});
		} catch (Exception e) {
			showMessage(a, "Connection failed. Check settings ...");

		}

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
