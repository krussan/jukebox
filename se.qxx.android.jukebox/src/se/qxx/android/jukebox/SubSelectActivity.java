package se.qxx.android.jukebox;

import se.qxx.android.jukebox.adapters.MediaSubsLayoutAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SubSelectActivity extends JukeboxActivityBase implements OnItemClickListener {

	@Override
	protected View getRootView() {
		return findViewById(R.id.rootSubtitlePicker);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subtitlepicker);
        
        initializeView();
    }

	private void initializeView() {
	    Movie m = Model.get().getCurrentMovie();
	    Media md = Model.get().getCurrentMedia();
	    
		MediaSubsLayoutAdapter adapter = new MediaSubsLayoutAdapter(this, md); 
		ListView v = (ListView)this.getRootView();
		v.setAdapter(adapter);
		v.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Subtitle sub = (Subtitle)arg0.getItemAtPosition(arg2);	
		Model.get().setCurrentSubtitle(sub.getDescription());
		this.finish();
	}
	
//
//	    View rootView = this.getRootView();
//	    
//	    if (!m.getImage().isEmpty()) 
//	    	GUITools.setImageOnImageView(R.id.imgSubPoster, m.getImage().toByteArray(), rootView);
//	    
//	    GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), rootView);
//	    GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()) , rootView);
//	    
//	    SeekBar sb = (SeekBar)findViewById(R.id.seekBarDuration);
//	    if (sb != null) {
//	    	sb.setMax(m.getMetaDuration());
//	    	sb.setOnSeekBarChangeListener(this);
//	    }
//	    
//	    //TODO: setProgress if movie is actually playing
//	    sendCommand("Starting movie...", JukeboxRequestType.StartMovie);
//	}
//
//	@Override
//	public void onProgressChanged(SeekBar seekBar, int progress,
//			boolean fromUser) {
//		TextView tv = (TextView)findViewById(R.id.txtSeekIndicator);
//		runOnUiThread(new UpdateSeekIndicator(progress, tv, seekBar));		
//	}
//
//	@Override
//	public void onStartTrackingTouch(SeekBar seekBar) {
//	}
//
//	@Override
//	public void onStopTrackingTouch(SeekBar seekBar) {
//		//TODO: send seek command to media player
//		int seconds = seekBar.getProgress();
//		sendCommand("Seeking...", JukeboxRequestType.Seek, seconds);
//	}
//		
//    public void onButtonClicked(View v) {
//    	int id = v.getId();
//    	
//    	switch (id) {
//    	case R.id.btnSubOk:
//			sendCommand("Sending OK...", JukeboxRequestType.MarkSubtitle, true);
//    		break;
//    	case R.id.btnSubNotOk:
//			sendCommand("Sending OK...", JukeboxRequestType.MarkSubtitle, false);    		
//    		break;
//    	case R.id.btnSubFullscreen:
//			sendCommand("Toggling fullscreen...", JukeboxRequestType.ToggleFullscreen);
//			break;
//    	}
//    }	
//    
}
