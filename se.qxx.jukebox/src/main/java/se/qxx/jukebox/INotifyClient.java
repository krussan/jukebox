package se.qxx.jukebox;

public interface INotifyClient {
	public void fileModified(FileRepresentation f);
	public void fileCreated(FileRepresentation f);
}
