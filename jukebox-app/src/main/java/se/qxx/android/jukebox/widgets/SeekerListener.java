package se.qxx.android.jukebox.widgets;

public interface SeekerListener {
    public void initializeSeeker();
	public void updateSeeker(long seconds, long duration);
	public void increaseSeeker(int advanceSeconds);
	public void setDuration(int seconds);
	public void startSeekerTimer();
    public void stopSeekerTimer();
}
