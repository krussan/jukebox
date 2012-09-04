package se.qxx.jukebox.upgrade;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Stack;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.Version;

public class Upgrader {

	public static boolean upgradeRequired() throws ClassNotFoundException, SQLException {
		Version currentVersion = DB.getVersion();
		Version thisVersion = new Version();

		return !thisVersion.isEqualTo(currentVersion);
	}
	
	public static void performUpgrade() 
			throws ClassNotFoundException
				 , SQLException
				 , SecurityException
				 , NoSuchMethodException
				 , IllegalArgumentException
				 , InstantiationException
				 , IllegalAccessException
				 , InvocationTargetException {
		
		Version currentVersion = DB.getVersion();
		Version thisVersion = new Version();
		
		Stack<Version> upgradeStack = new Stack<Version>();
		
		Version loopVersion = thisVersion;

		
		while (!loopVersion.isEqualTo(currentVersion)) {
			System.out.println(String.format("Adding %s to upgrade stack", loopVersion.toString()));
			upgradeStack.add(loopVersion);
			
			loopVersion = getClazz(loopVersion).getPreviousVersion();
		}
				
		//TODO: make backup of database
		while (!upgradeStack.isEmpty()) {
			Version upgradeVersion = upgradeStack.pop();
			IIncrimentalUpgrade c = getClazz(upgradeVersion);
			
			try {
				System.out.println(String.format("Upgrading to version %s", upgradeVersion.toString()));
				c.performUpgrade();
				
				DB.setVersion(upgradeVersion);
			}
			catch (Exception e) {
				//TODO: rollback database to made copy
				Log.Error(
						String.format("Upgrade failed for version %s"
								, upgradeVersion.toString())
						, LogType.MAIN
						, e);
				
				return;
			}
		}
		
		System.out.println("Upgrade complete. Please restart application");
		
	}
	
	private static IIncrimentalUpgrade getClazz(Version ver) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> c = Class.forName(ver.getUpgradeClazzName());
		Class<?>[] parTypes = new Class<?>[] {};
		Object[] args = new Object[] {};
		Constructor<?> con = c.getConstructor(parTypes);
		Object o = con.newInstance(args);
	
		return (IIncrimentalUpgrade)o;	
	}
	
	public static void runDatabasescripts(String[] dbScripts) throws UpgradeFailedException {
		System.out.println("Upgrading database...");
		int nrOfScripts = dbScripts.length;
		for (int i=0; i<dbScripts.length;i++) {
			System.out.println(String.format("Running script\t\t[%s/%s]", i + 1, nrOfScripts));
			if (!DB.executeUpgradeStatement(dbScripts[i]))
				throw new UpgradeFailedException();
		}
	}
}
