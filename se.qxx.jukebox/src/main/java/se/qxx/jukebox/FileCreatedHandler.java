package se.qxx.jukebox;

public class FileCreatedHandler implements INotifyClient {

	@Override
	public void fileModified(FileRepresentation f) {
		
	}
 
	@Override
	public void fileCreated(FileRepresentation f)  {
		MovieIdentifier.get().addFile(f);
	}


	
}
