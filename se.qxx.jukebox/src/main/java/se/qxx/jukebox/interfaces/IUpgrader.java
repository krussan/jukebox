package se.qxx.jukebox.interfaces;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public interface IUpgrader {

	boolean upgradeRequired() throws ClassNotFoundException, SQLException;

	boolean databaseIsLaterVersion() throws ClassNotFoundException, SQLException;

	void performUpgrade() throws ClassNotFoundException, SQLException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException,
			DatabaseNotSupportedException;

}