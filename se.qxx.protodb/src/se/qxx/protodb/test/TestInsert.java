package se.qxx.protodb.test;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

import com.google.protobuf.ByteString;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

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
				.setID(-1)
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
			
			int id = db.save(t);

			// check to see if the save was successful (ID should be greater than 0)
			assertNotEquals(id, -1);
			
			
			DynamicMessage dm = db.get(id, TestDomain.SimpleTest.getDescriptor());
			TestDomain.SimpleTest st = TestDomain.SimpleTest.parseFrom(dm.toByteString());
			
			assertEquals(t.getBb(), st.getBb());
			assertEquals(t.getDd(), st.getDd(), 0.0);
			assertEquals(t.getFf(), st.getFf(), 0.0);
			assertEquals(t.getIl(), st.getIl());
			assertEquals(t.getIs(), st.getIs());
			assertEquals(t.getSs(), st.getSs());
			assertArrayEquals(t.getBy().toByteArray(), st.getBy().toByteArray());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException | InvalidProtocolBufferException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void TestRepSimpleList() {		
		TestDomain.RepSimpleList t = TestDomain.RepSimpleList.newBuilder()
				.setID(-1)
				.setHappycamper(789)
				.addAllListOfStrings(Arrays.asList(new String[] {"simple", "types", "are", "fun"}))
				.build();
		
		try {
			db.setupDatabase(t);
			
			int id = db.save(t);

			// check to see if the save was successful (ID should be greater than 0)
			assertNotEquals(id, -1);
			
			DynamicMessage dm = db.get(id, TestDomain.RepSimpleList.getDescriptor());
			TestDomain.RepSimpleList st = TestDomain.RepSimpleList.parseFrom(dm.toByteString());
			
			assertEquals(t.getHappycamper(), st.getHappycamper());
			assertArrayEquals(
				t.getListOfStringsList().toArray(new String[] {}), 
				st.getListOfStringsList().toArray(new String[] {}));
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException | InvalidProtocolBufferException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
		

	@Test
	public void TestObjectOne() {
		TestDomain.ObjectOne t = TestDomain.ObjectOne.newBuilder()
				.setID(-1)
				.setOois(986)
				.setTestOne(TestDomain.SimpleTest.newBuilder()
						.setID(-1)
						.setBb(false)
						.setBy(ByteString.copyFrom(new byte[] {5,8,6}))
						.setDd(1467802579378.62352352)
						.setFf((float) 555444333.213)
						.setIl(999999998)
						.setIs(999999998)
						.setSs("ThisIsATestOfObjectOne")
				).build();
		
		TestDomain.SimpleTest tt = t.getTestOne();

		try {
			db.setupDatabase(t);
			
			int id = db.save(t);
			
			
			DynamicMessage dm = db.get(id, TestDomain.ObjectOne.getDescriptor());
			TestDomain.ObjectOne oo = TestDomain.ObjectOne.parseFrom(dm.toByteString());
			TestDomain.SimpleTest st = oo.getTestOne();
			
			assertEquals(t.getOois(), oo.getOois());
			
			assertEquals(tt.getBb(), st.getBb());
			assertEquals(tt.getDd(), st.getDd(), 0.0);
			assertEquals(tt.getFf(), st.getFf(), 0.0);
			assertEquals(tt.getIl(), st.getIl());
			assertEquals(tt.getIs(), st.getIs());
			assertEquals(tt.getSs(), st.getSs());
			assertArrayEquals(tt.getBy().toByteArray(), st.getBy().toByteArray());

			
			assertNotEquals(id, -1);
			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException | InvalidProtocolBufferException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	
	
	private void assertNotEquals(int a, int b) {
		String msg = String.format("Expected not to be actual. %s == %s", a, b);
		assertFalse(msg, a == b);
	}


	@Test
	public void TestSimpleUpdate() {		
		TestDomain.SimpleTest t = TestDomain.SimpleTest.newBuilder()
				.setID(-1)
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
			
			int id1 = db.save(t);
			// check to see if the save was successful (ID should be greater than 0)
			assertNotEquals(id1, -1);
			
			t = TestDomain.SimpleTest.newBuilder(t).setSs("ThisIsTheUpdateTest").setID(id1).build();
			
			int id2 = db.save(t);
			assertNotEquals(id2, -1);			
			assertEquals(id1, id2);
		
			DynamicMessage dm = db.get(id2, TestDomain.SimpleTest.getDescriptor());
			TestDomain.SimpleTest st = TestDomain.SimpleTest.parseFrom(dm.toByteString());
			
			assertEquals(t.getBb(), st.getBb());
			assertEquals(t.getDd(), st.getDd(), 0.0);
			assertEquals(t.getFf(), st.getFf(), 0.0);
			assertEquals(t.getIl(), st.getIl());
			assertEquals(t.getIs(), st.getIs());
			assertEquals(t.getSs(), st.getSs());
			assertArrayEquals(t.getBy().toByteArray(), st.getBy().toByteArray());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException | InvalidProtocolBufferException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void TestEnumOne() {
		TestDomain.EnumOne t = TestDomain.EnumOne.newBuilder()
			.setID(-1)
			.setRating(TestDomain.Rating.PositiveMatch)
			.setTitle("ThisIsAnEnumTitle")
			.build();
		
		try {
			db.setupDatabase(t);
			
			int id = db.save(t);	
			
			DynamicMessage dm = db.get(id, TestDomain.EnumOne.getDescriptor());
			TestDomain.EnumOne oo = TestDomain.EnumOne.parseFrom(dm.toByteString());
			
			assertEquals(t.getRating().toString(), oo.getRating().toString());
			assertEquals(t.getTitle(), oo.getTitle());
			assertNotEquals(id, -1);
						
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException | InvalidProtocolBufferException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}	

}
