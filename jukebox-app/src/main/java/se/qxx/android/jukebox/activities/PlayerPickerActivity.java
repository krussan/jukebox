package se.qxx.android.jukebox.activities;

import java.util.List;

import com.google.android.gms.cast.framework.CastContext;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.PlayerLayoutAdapter;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PlayerPickerActivity extends AppCompatActivity implements OnItemClickListener {

	PlayerLayoutAdapter adapter;
	List<String> values;
	private CastContext mCastContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        setContentView(R.layout.playerpicker);

		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(),
				JukeboxSettings.get().getServerPort(),	    		
	    		JukeboxConnectionProgressDialog.build(this, "Getting list of players..."));

        adapter = new PlayerLayoutAdapter(this);
        ListView listView = (ListView)findViewById(R.id.listPlayers);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        Thread t = new Thread(() -> jh.listPlayers(response -> {
        	if (response != null)
            	updateList(response.getHostnameList());
        }));
		t.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
	}

	private void updateList(List<String> players) {
	    adapter.clarPlayers();
	    adapter.addPlayer("LOCAL");
	    adapter.addPlayers(players);
	    adapter.addPlayer("ChromeCast");
		runOnUiThread(() -> adapter.notifyDataSetChanged());
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String playerName = (String)arg0.getItemAtPosition(arg2);
		JukeboxSettings.get().setCurrentMediaPlayer(playerName);

		mCastContext = CastContext.getSharedInstance(this);
		//updateList();
	}

}
 