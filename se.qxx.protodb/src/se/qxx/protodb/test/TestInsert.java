package se.qxx.protodb.test;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import se.qxx.protodb.ProtoDB;

import com.google.protobuf.ByteString;

public class TestInsert {
	ProtoDB db = null;
	
	private final String DATABASE_FILE = "protodb_test.db";
	
	@Before
	public void Setup() {
		
		File f = new File(DATABASE_FILE);
		f.delete();
		
	    db = new ProtoDB(DATABASE_FILE);
	}
	
	@Test
	public void TestSimple() {
		TestDomain.SimpleTest t = TestDomain.SimpleTest.newBuilder()
				.setBb(false)
				.setBy(ByteString.copyFrom(new byte[] {5,8,6}))
				.setDd(1467802579378.62352352)
				.setFf((float) 555444333.213)
				.setIl(999999998)
				.setIs(999999998)
				.setSs("ThisIsATest")
				.build();
		
		try {
			db.setupDatabase(t);

			PreparedStatement prep = "SELECT * FROM SimpleTest";
			
			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
