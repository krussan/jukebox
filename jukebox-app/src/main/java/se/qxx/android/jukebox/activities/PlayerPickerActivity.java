package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.gms.cast.framework.CastContext;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.PlayerLayoutAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;

public class PlayerPickerActivity extends AppCompatActivity implements OnItemClickListener {

	PlayerLayoutAdapter adapter;
	List<String> values;
	private CastContext mCastContext;
	private JukeboxSettings settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        setContentView(R.layout.playerpicker);
        settings = new JukeboxSettings(this);

		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				settings.getServerIpAddress(),
				settings.getServerPort(),
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
		settings.setCurrentMediaPlayer(playerName);

		mCastContext = CastContext.getSharedInstance(this);
		//updateList();
	}

}
 