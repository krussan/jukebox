package se.qxx.jukebox;

import se.qxx.jukebox.upgrade.Upgrader;

public class Jukebox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Arguments.initialize(args);
		startMainThread();		
	}
	
	private static void startMainThread()  {
		try {
			
			if (!Upgrader.upgradeRequired()) {
				System.out.println("No upgrade required... continuing...");
				Thread t = new Thread(new Main());
				t.start();
				
				t.join();
			}
			else {
				System.out.println("Upgrade required");
				Upgrader.performUpgrade();
			}
		}
		catch (Exception e) {
			//log exception and exit
			System.out.println("Error when starting up ::");
			e.printStackTrace();
		}
	}
	


}
