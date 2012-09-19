package se.qxx.android.jukebox;

import java.util.EventObject;
import java.util.List;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlayerPickerActivity extends JukeboxActivityBase implements ModelUpdatedEventListener {

	ArrayAdapter<String> adapter;
	List<String> values;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.playerpicker);
	    Model.get().addEventListener(this);
	    
	    ListView view = (ListView)findViewById(R.id.listPlayers);
	    
	    this.sendCommand("Getting list of player", JukeboxRequestType.ListPlayers);
	    
	    adapter = new ArrayAdapter<String>(this, R.layout.playerpickerrow, R.id.txtPlayerName, Model.get().getPlayers());
	    view.setAdapter(adapter);
	    
	}

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent)e;

		if (ev.getType() == ModelUpdatedType.Players) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}		
	}

}
 