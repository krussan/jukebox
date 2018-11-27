package se.qxx.jukebox.upgrade;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Stack;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IUpgrader;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

@Singleton
public class Upgrader implements IUpgrader {

	private IDatabase database;
	
	@Inject
	public Upgrader(IDatabase database) {
		this.setDatabase(database);
	}
	
	public IDatabase getDatabase() {
		return database;
	}
	public void setDatabase(IDatabase database) {
		this.database = database;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.upgrade.IUpgrader#upgradeRequired()
	 */
	@Override
	public boolean upgradeRequired() throws ClassNotFoundException, SQLException {
		Version currentVersion = this.getDatabase().getVersion();
		Version thisVersion = new Version();

		return !thisVersion.isEqualTo(currentVersion);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.upgrade.IUpgrader#databaseIsLaterVersion()
	 */
	@Override
	public boolean databaseIsLaterVersion() throws ClassNotFoundException, SQLException {
		Version currentVersion = this.getDatabase().getVersion();
		Version thisVersion = new Version();

		return thisVersion.isLessThan(currentVersion);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.upgrade.IUpgrader#performUpgrade()
	 */
	@Override
	public void performUpgrade() 
			throws ClassNotFoundException
				 , SQLException
				 , SecurityException
				 , NoSuchMethodException
				 , IllegalArgumentException
				 , InstantiationException
				 , IllegalAccessException
				 , InvocationTargetException, DatabaseNotSupportedException {
		
		Version currentVersion = this.getDatabase().getVersion();
		Version thisVersion = new Version();
		
		Stack<Version> upgradeStack = new Stack<Version>();
		
		Version loopVersion = thisVersion;

		
		while (!loopVersion.isEqualTo(currentVersion)) {
			System.out.println(String.format("Adding %s to upgrade stack", loopVersion.toString()));
			upgradeStack.add(loopVersion);
			
			loopVersion = getClazz(loopVersion).getPreviousVersion();
		}

		System.out.println("Making backup of database...");
		try {
			String backupFilename = this.getDatabase().backup();
		
			while (!upgradeStack.isEmpty()) {
				Version upgradeVersion = upgradeStack.pop();
				IIncrimentalUpgrade c = getClazz(upgradeVersion);
				
				try {
					System.out.println(String.format("Upgrading to version %s", upgradeVersion.toString()));
					c.performUpgrade();
					
					this.getDatabase().setVersion(upgradeVersion);
				}
				catch (Exception e) {

					System.out.println("Upgrade failed for version %s");
					e.printStackTrace();

					System.out.println("Starting rollback...");

					try {
						this.getDatabase().restore(backupFilename);
					} catch (IOException e2) {
						System.out.println("ROLLBACK FAILED! CORRUPT STATE!");
						e2.printStackTrace();
					}
					
					return;
				}
			}
			
			System.out.println("Upgrade complete. Please restart application");
		
		} catch (IOException e1) {
			System.out.println("Error while managing backup of database. No upgrade performed. Exiting...");
		}

		
	} 
	
	private IIncrimentalUpgrade getClazz(Version ver) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> c = Class.forName(ver.getUpgradeClazzName());
		Class<?>[] parTypes = new Class<?>[] {IDatabase.class};

		Object[] args = new Object[] {this.getDatabase()};
		Constructor<?> con = c.getConstructor(parTypes);
		Object o = con.newInstance(args);
	
		return (IIncrimentalUpgrade)o;	
	}
}
