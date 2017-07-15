package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.ChromeCastConfiguration;
import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.MediaSubsLayoutAdapter;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SubSelectActivity extends AppCompatActivity implements OnItemClickListener, OnDismissListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subtitlepicker);
        
        initializeView();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(getMenuInflater(), menu);

		return true;
	}

	@Override
	public void onResume() {
		super.onResume();

		ChromeCastConfiguration.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		ChromeCastConfiguration.onPause();
	}

	private void initializeView() {
	    Media md = Model.get().getCurrentMedia();
	    View rootView = GUITools.getRootView(this);

	    GUITools.setTextOnTextview(R.id.lblSubpickerFilename, md.getFilename(), rootView);
		MediaSubsLayoutAdapter adapter = new MediaSubsLayoutAdapter(this, Model.get().getSubtitles());
		ListView v = (ListView)findViewById(R.id.listSubtitlePicker);
		v.setAdapter(adapter);
		v.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final Subtitle sub = (Subtitle)arg0.getItemAtPosition(arg2);	
		Logger.Log().d(String.format("Setting subtitle to %s", sub.getDescription()));
		Model.get().setCurrentSubtitle(sub.getDescription());
		
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
				JukeboxSettings.get().getServerIpAddress(),
				JukeboxSettings.get().getServerPort(),				
				JukeboxConnectionProgressDialog.build(this, "Setting subtitle ..."));
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				jh.setSubtitle(JukeboxSettings.get().getCurrentMediaPlayer(), Model.get().getCurrentMedia(), sub);				
			}
		});
		t.run();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		this.finish();
	}
	
}
