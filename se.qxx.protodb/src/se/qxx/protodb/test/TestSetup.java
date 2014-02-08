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
import se.qxx.protodb.test.TestDomain.SimpleTest;

public class TestSetup {

	ProtoDB db = null;
	
	private final String DATABASE_FILE = "protodb_test.db";
	private final String[] SIMPLE_FIELD_NAMES = {"ID", "dd", "ff", "is", "il", "bb", "ss", "by"};
	private final String[] SIMPLE_FIELD_TYPES = {"INTEGER", "DOUBLE", "FLOAT", "INTEGER", "BIGINT", "BOOLEAN", "TEXT", "TEXT"};

	private final String[] OBJECTONE_FIELD_NAMES = {"ID", "_testone_ID", "oois"};
	private final String[] OBJECTONE_FIELD_TYPES = {"INTEGER", "INTEGER", "INTEGER"};

	private final String[] OBJECTTWO_FIELD_NAMES = {"ID", "_testone_ID", "_testtwo_ID", "otis"};
	private final String[] OBJECTTWO_FIELD_TYPES = {"INTEGER", "INTEGER", "INTEGER", "INTEGER"};

	private final String[] REPOBJECTONE_FIELD_NAMES = {"ID", "happycamper"};
	private final String[] REPOBJECTONE_FIELD_TYPES = {"INTEGER", "INTEGER"};

	private final String[] REPOBJECTONE_LINK_FIELD_NAMES = {"_repobjectone_ID", "_simpletwo_ID"};
	private final String[] REPOBJECTONE_LINK_FIELD_TYPES = {"INTEGER", "INTEGER"};

	private final String[] SIMPLETWO_FIELD_NAMES = {"ID", "title", "director"};
	private final String[] SIMPLETWO_FIELD_TYPES = {"INTEGER", "TEXT", "TEXT"};

	private final String[] REPSIMPLELIST_FIELD_NAMES = {"ID", "happycamper"};
	private final String[] REPSIMPLELIST_FIELD_TYPES = {"INTEGER", "INTEGER"};
	
	private final String[] REPSIMPLELIST_LISTOFSTRINGS_FIELD_NAMES = {"_repsimplelist_ID", "value"};
	private final String[] REPSIMPLELIST_LISTOFSTRINGS_FIELD_TYPES = {"INTEGER", "TEXT"};
	
	@Before
	public void Setup() {
		
		File f = new File(DATABASE_FILE);
		f.delete();
		
	    db = new ProtoDB(DATABASE_FILE);
	}
	

	@Test
	public void TestSimple() {
		
		TestDomain.SimpleTest t = TestDomain.SimpleTest.newBuilder().build();
		
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
		TestDomain.ObjectOne t = TestDomain.ObjectOne.newBuilder().build();

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
		TestDomain.ObjectTwo t = TestDomain.ObjectTwo.newBuilder().build();

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
	
	@Test
	public void TestRepObjectOne() {
		TestDomain.RepObjectOne t = TestDomain.RepObjectOne.newBuilder()
				.setHappycamper(42)
				.build();
		try {
			db.setupDatabase(t);
			
			testTableStructure(db, "SimpleTwo", SIMPLETWO_FIELD_NAMES, SIMPLETWO_FIELD_TYPES);
			testTableStructure(db, "RepObjectOne", REPOBJECTONE_FIELD_NAMES, REPOBJECTONE_FIELD_TYPES);			
			testTableStructure(db, "RepObjectOneSimpleTwo_ListOfObjects", REPOBJECTONE_LINK_FIELD_NAMES, REPOBJECTONE_LINK_FIELD_TYPES);
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	@Test
	public void TestRepSimpleList() {
		TestDomain.RepSimpleList t = TestDomain.RepSimpleList.newBuilder()
				.setHappycamper(44)
				.build();
		
		try {
			db.setupDatabase(t);
			
			testTableStructure(db, "RepSimpleList", REPSIMPLELIST_FIELD_NAMES, REPSIMPLELIST_FIELD_TYPES);
			testTableStructure(db, "RepSimpleList_ListOfStrings", REPSIMPLELIST_LISTOFSTRINGS_FIELD_NAMES, REPSIMPLELIST_LISTOFSTRINGS_FIELD_TYPES);			
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	private void testTableStructure(
			ProtoDB db,
			String expectedTableName,
			String[] fieldNames, 
			String[] fieldTypes) throws ClassNotFoundException {
		try {
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
		catch (SQLException e) {
			fail(String.format("Table %s failed the test. Probably because it does not exist. Msg :: %s", expectedTableName, e.getMessage()));
		}
	}	
	
}
