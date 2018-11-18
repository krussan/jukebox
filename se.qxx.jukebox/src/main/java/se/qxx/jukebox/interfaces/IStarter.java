package se.qxx.jukebox.interfaces;

public interface IStarter {
	public boolean checkStart();
	public boolean checkDatabase();
	public void purge();
}
