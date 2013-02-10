package se.qxx.android.jukebox;

import java.util.EventObject;
import java.util.List;

import se.qxx.android.jukebox.adapters.PlayerLayoutAdapter;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.comm.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PlayerPickerActivity extends JukeboxActivityBase implements ModelUpdatedEventListener, OnItemClickListener {

	PlayerLayoutAdapter adapter;
	List<String> values;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.playerpicker);
	    Model.get().addEventListener(this);

	    JukeboxConnectionHandler jh = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(this, "Getting list of players..."));
	    jh.listPlayers();
    
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
 