package se.qxx.jukebox.interfaces;

public interface ICleaner {

	IArguments getArguments();

	void setArguments(IArguments arguments);

	IDatabase getDatabase();

	void setDatabase(IDatabase database);

	int getJukeboxPriority();

}