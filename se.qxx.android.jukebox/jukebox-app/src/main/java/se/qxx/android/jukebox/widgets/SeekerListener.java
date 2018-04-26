package se.qxx.android.jukebox.widgets;

public interface SeekerListener {
	public void updateSeeker(int seconds);
	public void increaseSeeker(int advanceSeconds);
	public void setDuration(int seconds);
	public void startSeekerTimer();
    public void stopSeekerTimer();
}
