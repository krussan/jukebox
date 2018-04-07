package se.qxx.android.jukebox.activities;

import java.util.EventObject;
import java.util.List;

import com.google.android.gms.cast.framework.CastContext;
import com.google.protobuf.RpcCallback;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.PlayerLayoutAdapter;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.apache.commons.lang3.StringUtils;

public class PlayerPickerActivity extends AppCompatActivity implements ModelUpdatedEventListener, OnItemClickListener {

	PlayerLayoutAdapter adapter;
	List<String> values;
	private CastContext mCastContext;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
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

		mCastContext = CastContext.getSharedInstance(this);
//		if (StringUtils.equalsIgnoreCase(playerName, "Chromecast"))
//			ChromeCastConfiguration.initialize(this);

		updateList();		
	}

}
 