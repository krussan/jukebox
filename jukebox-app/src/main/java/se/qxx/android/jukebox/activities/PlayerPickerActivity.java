package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.cast.framework.CastContext;
import org.apache.commons.lang3.StringUtils;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.PlayerLayoutAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.domain.JukeboxDomain;

import java.util.List;

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
            	updateList(((JukeboxDomain.JukeboxResponseListPlayers)response).getHostnameList());

        	jh.stop();
        }));
		t.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu, settings.getCurrentMediaPlayer());

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

		if (StringUtils.equalsIgnoreCase(playerName, "Chromecast"))
			ChromeCastConfiguration.checkGooglePlayServices(this);

		mCastContext = CastContext.getSharedInstance(this);
		//updateList();
	}

}
 