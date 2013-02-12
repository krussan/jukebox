package se.qxx.android.jukebox;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.adapters.MovieLayoutAdapter;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.comm.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class JukeboxActivity extends JukeboxActivityBase
	implements ModelUpdatedEventListener, OnItemClickListener, OnItemLongClickListener {
	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	
    private Runnable modelResultUpdatedRunnable = new Runnable() {

        @Override
        public void run() {
            _jukeboxMovieLayoutAdapter.notifyDataSetChanged();
        }
    };
    	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        JukeboxSettings.init(this);
        
		ListView v = (ListView)findViewById(R.id.listView1);
		v.setOnItemClickListener(this);
		v.setOnItemLongClickListener(this);
		
		_jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(this); 
		v.setAdapter(_jukeboxMovieLayoutAdapter);
		
		Model.get().addEventListener(this);
		
		setupOnOffButton();

		if (!Model.get().isInitialized())
			connect();
		else
			runOnUiThread(modelResultUpdatedRunnable);		
		
    }

    private void setupOnOffButton() {
        View rootView = this.getRootView();
    	
	    if (JukeboxSettings.get().isCurrentMediaPlayerOn()) {
	    	GUITools.showView(R.id.btnOff, rootView);
	    	GUITools.hideView(R.id.btnOn, rootView);
	    }
	    else {
	    	GUITools.showView(R.id.btnOn, rootView);
	    	GUITools.hideView(R.id.btnOff, rootView);	    	
	    } 
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	//super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.preferences:
    		//Toast.makeText(this, "You selected the preferences option", Toast.LENGTH_LONG).show();
    		Intent i = new Intent(this, JukeboxPreferenceActivity.class);
    		startActivity(i);
			break;
    	}
    	
    	return true;
    };
    
    public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, this);
		
		switch (id) {
		case R.id.btnRefresh:
	    	Logger.Log().i("onConnectClicked");

	    	Model.get().clearMovies();
			connect();
			break;
		case R.id.btnSelectMediaPlayer:
			Logger.Log().i("selectMediaPlayerClicked");
			
			Intent i = new Intent(this, PlayerPickerActivity.class);
			startActivity(i);
			break;
		case R.id.btnOn:
		case R.id.btnOff:
			onoff();
			break;
		default:
			break;
		
		}
    }
    
    private void onoff() {
    	//TODO: Check if computer is live.
    	final Context c = this;
    	final boolean isOnline = JukeboxSettings.get().isCurrentMediaPlayerOn();
    	final String currentMediaPlayer = JukeboxSettings.get().getCurrentMediaPlayer();
    	
    	final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
    			JukeboxConnectionProgressDialog.build(c, 
    					isOnline ? "Suspending target media player..." : "Waking up..."));
    	
    	Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (isOnline)
		    		jh.suspend(currentMediaPlayer);
		    	else 
		    		jh.wakeup(currentMediaPlayer);
			}
    	});
    	t.start();
    	
    	JukeboxSettings.get().setIsCurrentMediaPlayerOn(!isOnline);
    	setupOnOffButton();
	}

	public void connect() {
    	final Context c = this;
		final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(JukeboxConnectionProgressDialog.build(c, "Getting list of media ..."));

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				jh.listMovies("");
			}
			
		});
		t.start();
    }
    

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		ModelUpdatedEvent ev = (ModelUpdatedEvent)e;

		if (ev.getType() == ModelUpdatedType.Movies) {
			runOnUiThread(modelResultUpdatedRunnable);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		Model.get().setCurrentMovie(pos);
		Intent i = new Intent(this, FlipperActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ActionDialog d = new ActionDialog(this, Model.get().getMovie(arg2));
		d.show();
		return false;
	}
	
}