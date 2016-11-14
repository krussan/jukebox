package se.qxx.jukebox.junit;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.upgrade.Upgrader;

public class TestUpgrade {
	
	@Before public void setup() {
		DB.setDatabase("jukebox_test.db");
	}
	
	@Test public void upgrade() 
			throws SecurityException
			, IllegalArgumentException
			, ClassNotFoundException
			, SQLException
			, NoSuchMethodException
			, InstantiationException
			, IllegalAccessException
			, InvocationTargetException {
		
		//Upgrader.performUpgrade();
	}
}
