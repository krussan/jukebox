package se.qxx.android.jukebox.widgets;

import android.widget.SeekBar;
import android.widget.TextView;

public class UpdateSeekIndicator implements Runnable {
	private long progress = 0;
	private long duration = 0;
	private TextView tv;
	private SeekBar seekBar;

	public UpdateSeekIndicator(long progress, long duration, TextView view, SeekBar seekBar) {
		this.progress = progress;
		this.duration = duration;
		this.tv = view;
		this.seekBar = seekBar;
	}

	public UpdateSeekIndicator(long progress, long duration, TextView view) {
		this.progress = progress;
		this.duration = duration;
		this.tv = view;
	}

	
	@Override
	public void run() {
		long hours = progress / 3600;
		long  minutes = (progress % 3600) / 60;
		long seconds = (progress % 3600) % 60;
		
		if (this.tv != null)
			this.tv.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
		
		if (this.seekBar != null) {
			this.seekBar.setProgress((int)progress);
			this.seekBar.setMax((int)duration);
		}

	}		
}
