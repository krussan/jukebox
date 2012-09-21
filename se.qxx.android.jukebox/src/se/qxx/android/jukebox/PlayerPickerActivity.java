package se.qxx.android.jukebox;

import java.util.EventObject;
import java.util.List;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PlayerPickerActivity extends JukeboxActivityBase implements ModelUpdatedEventListener, OnItemClickListener {

	ArrayAdapter<String> adapter;
	List<String> values;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.playerpicker);
	    Model.get().addEventListener(this);
	    
	    ListView view = (ListView)findViewById(R.id.listPlayers);
	    
	    this.sendCommand("Getting list of player", JukeboxRequestType.ListPlayers);
	    
	    adapter = new PlayerLayoutAdapter(this, R.layout.playerpickerrow, R.id.txtPlayerName, Model.get().getPlayers());
	    view.setAdapter(adapter);
	    
		ListView v = (ListView)findViewById(R.id.listPlayers);
		v.setOnItemClickListener(this);
	    
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		String playerName = (String)arg0.getItemAtPosition(pos);
		JukeboxSettings.get().setCurrentMediaPlayer(playerName);
		updateList();
	}

	@Override
	protected View getRootView() {
		return findViewById(R.id.rootPlayerPicker);
	}

}
 