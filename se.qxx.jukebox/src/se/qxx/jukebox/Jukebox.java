package se.qxx.jukebox;

public class Jukebox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		setupListening();
		
		startMainThread();
		
		stopListening();
		
	}
	
	private static void startMainThread()  {
		try {
			Thread t = new Thread(new Main());
			t.start();
			
			t.join();
		}
		catch (Exception e) {
			//log exception and exit
		}
	}
	
	private static void setupListening() {
		
	}
	
	private static void stopListening() {
		
	}

}
