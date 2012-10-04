package se.qxx.android.jukebox;

import android.widget.TextView;

public class UpdateSeekIndicator implements Runnable {
	private int progress = 0;
	private TextView tv;
	public UpdateSeekIndicator(int progress, TextView view) {
		this.progress = progress;
		this.tv = view;
	}
	
	@Override
	public void run() {
		int hours = progress / 3600;
		int minutes = (progress % 3600) / 60;
		int seconds = (progress % 3600) % 60;
		
		this.tv.setText(String.format("%s:%s:%s", hours, minutes, seconds));				
	}		
}
