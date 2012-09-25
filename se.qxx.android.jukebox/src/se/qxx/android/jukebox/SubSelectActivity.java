package se.qxx.android.jukebox;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SubSelectActivity extends JukeboxActivityBase implements OnSeekBarChangeListener {

	@Override
	protected View getRootView() {
		return findViewById(R.id.rootSubSelect);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subselect);
        
        initializeView();
    }

	private void initializeView() {
	    Movie m = Model.get().getCurrentMovie();

	    View rootView = this.getRootView();
	    
	    if (!m.getImage().isEmpty()) 
	    	GUITools.setImageOnImageView(R.id.imgSubPoster, m.getImage().toByteArray(), rootView);
	    	    
	    int duration = m.getDuration();
	    int hours = duration / 60;
	    int minutes = duration % 60;
	    
	    GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), rootView);
	    GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), rootView);
	    GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()) , rootView);
	    
	    SeekBar sb = (SeekBar)findViewById(R.id.seekBarDuration);
	    if (sb != null) {
	    	sb.setMax(m.getMetaDuration());
	    	sb.setOnSeekBarChangeListener(this);
	    }
	    
	    //TODO: setProgress if movie is actually playing
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Update some textview with the current time
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//TODO: send seek command to server		
	}
    
}
