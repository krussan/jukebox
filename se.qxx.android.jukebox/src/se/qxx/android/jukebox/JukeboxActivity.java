package se.qxx.android.jukebox;

import java.util.EventObject;

import se.qxx.android.jukebox.adapters.MovieLayoutAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class JukeboxActivity extends JukeboxActivityBase implements ModelUpdatedEventListener, OnItemClickListener {
	private MovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	
    private Runnable modelResultUpdatedRunnable = new Runnable() {

        @Override
        public void run() {
            _jukeboxMovieLayoutAdapter.notifyDataSetChanged();
        }
    };
    
	@Override
	protected View getRootView() {
		return findViewById(R.id.rootMain);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        JukeboxSettings.init(this);
        
		ListView v = (ListView)findViewById(R.id.listView1);
		v.setOnItemClickListener(this);
		
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
    	boolean isOnline = JukeboxSettings.get().isCurrentMediaPlayerOn();
    	if (isOnline) 
    		sendCommand("Suspending target media player...", JukeboxRequestType.Suspend);
    	else    		
    		sendCommand("Waking up...", JukeboxRequestType.Wakeup);
    	
    	JukeboxSettings.get().setIsCurrentMediaPlayerOn(!isOnline);
    	setupOnOffButton();
	}

	public void connect() {
		this.sendCommand("Getting list of movies", JukeboxRequestType.ListMovies);	
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


}