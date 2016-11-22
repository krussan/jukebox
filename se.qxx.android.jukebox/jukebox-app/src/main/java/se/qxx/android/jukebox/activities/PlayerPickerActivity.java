package se.qxx.android.jukebox.activities;

import java.util.EventObject;
import java.util.List;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.protobuf.RpcCallback;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.PlayerLayoutAdapter;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.android.jukebox.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.media.MediaRouter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.support.v7.media.MediaRouteSelector;

public class PlayerPickerActivity extends Activity implements ModelUpdatedEventListener, OnItemClickListener {

	PlayerLayoutAdapter adapter;
	List<String> values;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        setContentView(R.layout.playerpicker);
	    Model.get().addEventListener(this);

	    final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(),
				JukeboxSettings.get().getServerPort(),	    		
	    		JukeboxConnectionProgressDialog.build(this, "Getting list of players..."));

		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
			    jh.listPlayers(new RpcCallback<JukeboxResponseListPlayers>() {
					@Override
					public void run(JukeboxResponseListPlayers response) {
						// Add Jukebox central players
						Model.get().clearPlayers();
						Model.get().addAllPlayers(response.getHostnameList());

						// Add chromecast players
                        Model.get().getPlayers().add("Chromecast");
					}
				});
			}
		});
		t.start();



		adapter = new PlayerLayoutAdapter(this);
	    ListView listView = (ListView)findViewById(R.id.listPlayers);	    

	    listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent)e;

		if (ev.getType() == ModelUpdatedType.Players) {
			updateList();
		}		
	}
	
	private void updateList() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String playerName = (String)arg0.getItemAtPosition(arg2);
		JukeboxSettings.get().setCurrentMediaPlayer(playerName);
		updateList();		
	}

}
 