package se.qxx.protodb.test;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import se.qxx.protodb.ProtoDB;

import com.google.protobuf.ByteString;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class TestSelect {
	ProtoDB db = null;
	
	private final String DATABASE_FILE = "protodb_select_test.db";
	
	@Before
	public void Setup() {		
	    db = new ProtoDB(DATABASE_FILE);
	}
	
	@Test
	public void TestSimple() {	
		try {
			DynamicMessage dm = db.get(1, TestDomain.RepObjectOne.getDescriptor());
			TestDomain.RepObjectOne b = TestDomain.RepObjectOne.parseFrom(dm.toByteString());

			// happyCamper should be 3
			assertEquals(3, b.getHappycamper());
			
			// we should have two repeated objects
			assertEquals(2, b.getListOfObjectsCount());
			
			TestDomain.SimpleTwo o1 = b.getListOfObjects(0);
			assertEquals("thisIsATitle", o1.getTitle());
			assertEquals("madeByThisDirector", o1.getDirector());
			
			TestDomain.SimpleTwo o2 = b.getListOfObjects(1);
			assertEquals("thisIsAlsoATitle", o2.getTitle());
			assertEquals("madeByAnotherDirector", o2.getDirector());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);
		} catch (SQLException | ClassNotFoundException | InvalidProtocolBufferException  e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
