package se.qxx.jukebox;

import se.qxx.jukebox.upgrade.Upgrader;

public class Jukebox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Arguments.initialize(args);
		
		if (Arguments.get().isHelpRequested()) {
			displayHelp();
			return;
		}
		
		if (Arguments.get().isPurgeMode()) {
			purge();
			return;
		}
					
		startMainThread();
			
	}
	
	private static void displayHelp() {
		System.out.println("");
		System.out.println("Jukebox starter - run.sh");
		System.out.println("");
		System.out.println("   run.sh [-ds] [-di] [-dt] [--purge] [--help]");
		System.out.println("");
		System.out.println("\t-ds\tDisable subtitle downloader");
		System.out.println("\t-di\tDisable imdb identifier");
		System.out.println("\t-ds\tDisable tcp listener");
		System.out.println("\t--purge\tPurges all content from database and exit");
		System.out.println("\t--help\tDisplays this help");
		System.out.println("");
	}
	
	private static void purge() {
		System.out.println("Purging database ....");
		DB.purgeDatabase();
		System.out.println("Done !");
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
