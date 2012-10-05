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
	
	@Override
	public void run() {
		int hours = progress / 3600;
		int minutes = (progress % 3600) / 60;
		int seconds = (progress % 3600) % 60;
		
		this.tv.setText(String.format("%s:%s:%s", hours, minutes, seconds));
		this.seekBar.setProgress(progress);
	}		
}
