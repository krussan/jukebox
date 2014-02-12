package se.qxx.protodb.test;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;

import com.google.protobuf.ByteString;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;

public class TestSearch {
	ProtoDB db = null;
	
	private final String DATABASE_FILE = "protodb_select_test.db";
	
	@Before
	public void Setup() {		
	    db = new ProtoDB(DATABASE_FILE);
	}
	
	@Test
	public void TestSearchExact() {	
		try {
			List<DynamicMessage> result =
				db.find(
					TestDomain.SimpleTwo.getDescriptor(), 
					"director", 
					"madeByAnotherDirector", 
					false);
			
			// we should get one single result..
			assertEquals(1, result.size());
		
			TestDomain.SimpleTwo b = TestDomain.SimpleTwo.parseFrom(result.get(0).toByteString());

			assertEquals("thisIsAlsoATitle", b.getTitle());
			assertEquals("madeByAnotherDirector", b.getDirector());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);
		} catch (SQLException | ClassNotFoundException | InvalidProtocolBufferException | SearchFieldNotFoundException  e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	@Test
	public void TestSearchLike() {	
		try {
			List<DynamicMessage> result =
				db.find(
					TestDomain.SimpleTwo.getDescriptor(), 
					"director", 
					"madeBy%", 
					true);
			
			// we should get one single result..
			assertEquals(2, result.size());
		
			TestDomain.SimpleTwo o1 = TestDomain.SimpleTwo.parseFrom(result.get(0).toByteString());
			assertEquals("thisIsATitle", o1.getTitle());
			assertEquals("madeByThisDirector", o1.getDirector());
			
			TestDomain.SimpleTwo o2 = TestDomain.SimpleTwo.parseFrom(result.get(1).toByteString());
			assertEquals("thisIsAlsoATitle", o2.getTitle());
			assertEquals("madeByAnotherDirector", o2.getDirector());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);
		} catch (SQLException | ClassNotFoundException | InvalidProtocolBufferException | SearchFieldNotFoundException  e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
