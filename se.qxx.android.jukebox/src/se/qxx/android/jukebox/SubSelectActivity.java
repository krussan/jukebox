package se.qxx.android.jukebox;

import se.qxx.android.jukebox.adapters.MediaSubsLayoutAdapter;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SubSelectActivity extends JukeboxActivityBase implements OnItemClickListener, OnDismissListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subtitlepicker);
        
        initializeView();
    }

	private void initializeView() {
	    Media md = Model.get().getCurrentMedia();
	    
	    GUITools.setTextOnTextview(R.id.lblSubpickerFilename, md.getFilename(), this.getRootView());
		MediaSubsLayoutAdapter adapter = new MediaSubsLayoutAdapter(this, md); 
		ListView v = (ListView)findViewById(R.id.listSubtitlePicker);
		v.setAdapter(adapter);
		v.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final Subtitle sub = (Subtitle)arg0.getItemAtPosition(arg2);	
		Logger.Log().d(String.format("Setting subtitle to %s", sub.getDescription()));
		Model.get().setCurrentSubtitle(sub.getDescription());
		
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(this, "Setting subtitle ..."));
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
