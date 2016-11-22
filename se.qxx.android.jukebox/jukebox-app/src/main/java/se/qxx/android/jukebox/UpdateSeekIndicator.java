package se.qxx.android.jukebox;

import android.widget.SeekBar;
import android.widget.TextView;

public class UpdateSeekIndicator implements Runnable {
	private int progress = 0;
	private TextView tv;
	private SeekBar seekBar;
	public UpdateSeekIndicator(int progress, TextView view, SeekBar seekBar) {
		this.progress = progress;
		this.tv = view;
		this.seekBar = seekBar;
	}

	public UpdateSeekIndicator(int progress, TextView view) {
		this.progress = progress;
		this.tv = view;
	}

	
	@Override
	public void run() {
		int hours = progress / 3600;
		int minutes = (progress % 3600) / 60;
		int seconds = (progress % 3600) % 60;
		
		if (this.tv != null)
			this.tv.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
		
		if (this.seekBar != null)
			this.seekBar.setProgress(progress);
	}		
}
