package se.qxx.jukebox;

public class Jukebox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Arguments a = Arguments.parse(args);
		startMainThread(a);		
	}
	
	private static void startMainThread(Arguments args)  {
		try {
			
			Thread t = new Thread(new Main(args));
			t.start();
			
			t.join();
		}
		catch (Exception e) {
			//log exception and exit
		}
	}
	


}
