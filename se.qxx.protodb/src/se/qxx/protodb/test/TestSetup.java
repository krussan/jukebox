package se.qxx.protodb.test;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.test.Testdomain.SimpleTest;

public class TestSetup {

	ProtoDB db = null;
	
	private final String DATABASE_FILE = "protodb_test.db";
	private final String[] SIMPLE_FIELD_NAMES = {"ID", "dd", "ff", "is", "il", "bb", "ss", "by"};
	private final String[] SIMPLE_FIELD_TYPES = {"INT", "DOUBLE", "FLOAT", "INT", "BIGINT", "BOOLEAN", "TEXT", "TEXT"};

	private final String[] OBJECTONE_FIELD_NAMES = {"ID", "_testone_ID", "oois"};
	private final String[] OBJECTONE_FIELD_TYPES = {"INT", "INT", "INT"};

	private final String[] OBJECTTWO_FIELD_NAMES = {"ID", "_testone_ID", "_testtwo_ID", "otis"};
	private final String[] OBJECTTWO_FIELD_TYPES = {"INT", "INT", "INT", "INT"};

	@Before
	public void Setup() {
		
		File f = new File(DATABASE_FILE);
		f.delete();
		
	    db = new ProtoDB(DATABASE_FILE);
		
	}
	

	@Test
	public void TestSimple() {
		
		Testdomain.SimpleTest t = Testdomain.SimpleTest.newBuilder().build();
		
		try {
			db.setupDatabase(t);

			// test if database structure is the one we want
			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


	@Test
	public void TestObjectOne() {
		Testdomain.ObjectOne t = Testdomain.ObjectOne.newBuilder().build();

		try {
			db.setupDatabase(t);
			
			testTableStructure(db, "ObjectOne", OBJECTONE_FIELD_NAMES, OBJECTONE_FIELD_TYPES);
			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	
	
	@Test
	public void TestObjectTwo() {
		// test boolean
		Testdomain.ObjectTwo t = Testdomain.ObjectTwo.newBuilder().build();

		try {
			db.setupDatabase(t);
			
			testTableStructure(db, "ObjectTwo", OBJECTTWO_FIELD_NAMES, OBJECTTWO_FIELD_TYPES);
			testTableStructure(db, "ObjectOne", OBJECTONE_FIELD_NAMES, OBJECTONE_FIELD_TYPES);
			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// test if database structure is the one we want
	}		
	
	private void testTableStructure(
			ProtoDB db,
			String expectedTableName,
			String[] fieldNames, 
			String[] fieldTypes) throws ClassNotFoundException, SQLException {
		List<Pair<String, String>> cols = db.retreiveColumns(expectedTableName);
		
		int c = 0;		
		for(Pair<String, String> col : cols) {
			if(!StringUtils.equalsIgnoreCase(col.getLeft(), fieldNames[c]) ||
				!StringUtils.equalsIgnoreCase(col.getRight(), fieldTypes[c]))
				fail(String.format("Unexpected field :: %s type :: %s. Expected %s :: %s",
						col.getLeft(), col.getRight(),
						fieldNames[c], fieldTypes[c]));
			c++;
		}
	}	
	
}
