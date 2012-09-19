package se.qxx.android.jukebox;

import java.util.EventObject;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.model.ModelUpdatedEvent;
import se.qxx.android.jukebox.model.ModelUpdatedType;
import se.qxx.android.jukebox.model.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
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
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        JukeboxSettings.init(this);
        
        // clear movies to avoid duplicates when re-initiating app
        Model.get().clearMovies();
        
		ListView v = (ListView)findViewById(R.id.listView1);
		v.setOnItemClickListener(this);
		
		_jukeboxMovieLayoutAdapter = new MovieLayoutAdapter(this); 
		v.setAdapter(_jukeboxMovieLayoutAdapter);
		
		Model.get().addEventListener(this);
		
		connect();
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
		
		switch (id) {
		case R.id.btnRefresh:
		case R.id.btnConnect:
	    	Logger.Log().i("onConnectClicked");

	    	Model.get().clearMovies();
			connect();
			break;
		case R.id.btnSelectMediaPlayer:
			Logger.Log().i("selectMediaPlayerClicked");
			
			Intent i = new Intent(this, PlayerPickerActivity.class);
			startActivity(i);
			break;
		default:
			break;
		
		}
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
		Movie m = (Movie)arg0.getItemAtPosition(pos);
		Model.get().setCurrentMovie(m);
		Intent i = new Intent(this, MovieInfoActivity.class);
		startActivity(i);
	}

}