package se.qxx.android.jukebox;

import se.qxx.android.jukebox.adapters.MediaSubsLayoutAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SubSelectActivity extends JukeboxActivityBase implements OnItemClickListener, OnDismissListener {

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
	    
	    GUITools.setTextOnTextview(R.id.lblSubpickerFilename, md.getFilename(), this.getRootView());
		MediaSubsLayoutAdapter adapter = new MediaSubsLayoutAdapter(this, md); 
		ListView v = (ListView)findViewById(R.id.listSubtitlePicker);
		v.setAdapter(adapter);
		v.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Subtitle sub = (Subtitle)arg0.getItemAtPosition(arg2);	
		Logger.Log().d(String.format("Setting subtitle to %s", sub.getDescription()));
		Model.get().setCurrentSubtitle(sub.getDescription());
		
		this.sendCommand(this, "Setting subtitle", JukeboxRequestType.SetSubtitle);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
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
