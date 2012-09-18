package se.qxx.android.jukebox;

import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PlayerPickerActivity extends JukeboxActivityBase {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    ListView view = (ListView)findViewById(R.id.listPlayers);
	    
	    this.sendCommand("Getting list of player", JukeboxRequestType.ListPlayers);
	    
	    ListAdapter la = new ArrayAdapter<String>(this, R.id.listMediaPlayer, R.id.txtPlayerName);
	    view.setAdapter(la);
	}

}
 