package se.qxx.android.jukebox;

import java.util.EventObject;
import java.util.List;

import se.qxx.android.jukebox.Model.ModelUpdatedEventListener;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


public class JukeboxActivity extends Activity implements ModelUpdatedEventListener, OnItemClickListener {
	private EditText text;
	private JukeboxMovieLayoutAdapter _jukeboxMovieLayoutAdapter;
	
    private Runnable modelResultUpdatedRunnable = new Runnable() {

        @Override
        public void run() {
            _jukeboxMovieLayoutAdapter.notifyDataSetChanged();
        }
    };
    
	private class JukeboxMovieLayoutAdapter extends ModelAdapter {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView; 
			
			try {
				
				
	            if (v == null) {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate(R.layout.movielistrow, null);
	            }
	            Movie m = (Movie)this.getItem(position);
	            if (m != null) {
	            	TextView tt = (TextView) v.findViewById(R.id.toptext);
	            	TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	            	
	            	if (tt != null) {
	                    tt.setText(m.getTitle());                            }
	            	if(bt != null){
	                    bt.setText(String.valueOf(m.getYear()));
	              }
	            }
			}
			catch (Exception e) {
				Logger.Log().e("Error occured while populating list", e);
			}
			
            return v;
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        JukeboxSettings.init(this);
        
		ListView v = (ListView)findViewById(R.id.listView1);
		v.setOnItemClickListener(this);
		
		_jukeboxMovieLayoutAdapter = new JukeboxMovieLayoutAdapter(); 
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
    
    public void onConnectClickHandler(View view) throws InterruptedException {
    	Logger.Log().i("onConnectClicked");

		connect();
   		
    }
    
    public void connect() {
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", "Getting list of movies");

       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), JukeboxRequestType.ListMovies);
       	Thread t = new Thread(h);
       	t.start();    	
    }
    
    // This method is called at button click because we assigned the name to the
 	// "On Click property" of the button
 	public void myClickHandler(View view) {
 		switch (view.getId()) {

 		}
 	}
 	

	@Override
	public void handleModelUpdatedEventListener(EventObject e) {
		runOnUiThread(modelResultUpdatedRunnable);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		
		Movie m = (Movie)arg0.getItemAtPosition(pos);
		Model.get().setCurrentMovie(m);
		Intent i = new Intent(this, MovieInfoActivity.class);
		startActivity(i);
	}

}