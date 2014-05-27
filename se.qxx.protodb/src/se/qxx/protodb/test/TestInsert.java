package se.qxx.protodb.test;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

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
			
			TestDomain.SimpleTest tt = db.save(t);

			// check to see if the save was successful (ID should be greater than 0)
			assertNotEquals(tt.getID(), -1);
			
			
			TestDomain.SimpleTest st = db.get(tt.getID(), TestDomain.SimpleTest.getDefaultInstance());
			
			assertEquals(t.getBb(), st.getBb());
			assertEquals(t.getDd(), st.getDd(), 0.0);
			assertEquals(t.getFf(), st.getFf(), 0.0);
			assertEquals(t.getIl(), st.getIl());
			assertEquals(t.getIs(), st.getIs());
			assertNotEquals(t.getSs(), st.getSs());
			assertArrayEquals(t.getBy().toByteArray(), st.getBy().toByteArray());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException e) {
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
			
			TestDomain.RepSimpleList tt = db.save(t);

			// check to see if the save was successful (ID should be greater than 0)
			assertNotEquals(tt.getID(), -1);
			
			TestDomain.RepSimpleList st = db.get(tt.getID(), TestDomain.RepSimpleList.getDefaultInstance());
			
			assertEquals(t.getHappycamper(), st.getHappycamper());
			assertArrayEquals(
				t.getListOfStringsList().toArray(new String[] {}), 
				st.getListOfStringsList().toArray(new String[] {}));
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException e) {
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
			
			TestDomain.ObjectOne ttt = db.save(t);
			
			assertNotEquals(ttt.getID(), -1);
			
			TestDomain.ObjectOne oo = db.get(ttt.getID(), TestDomain.ObjectOne.getDefaultInstance());
			TestDomain.SimpleTest st = oo.getTestOne();
			
			assertEquals(t.getOois(), oo.getOois());
			
			assertEquals(tt.getBb(), st.getBb());
			assertEquals(tt.getDd(), st.getDd(), 0.0);
			assertEquals(tt.getFf(), st.getFf(), 0.0);
			assertEquals(tt.getIl(), st.getIl());
			assertEquals(tt.getIs(), st.getIs());
			assertEquals(tt.getSs(), st.getSs());
			assertArrayEquals(tt.getBy().toByteArray(), st.getBy().toByteArray());

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException  e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	
	
	private void assertNotEquals(int a, int b) {
		String msg = String.format("Expected not to be actual. %s == %s", a, b);
		assertFalse(msg, a == b);
	}

	private void assertNotEquals(String a, String b) {
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
			
			TestDomain.SimpleTest tt = db.save(t);
			// check to see if the save was successful (ID should be greater than 0)
			assertNotEquals(tt.getID(), -1);
			
			tt = TestDomain.SimpleTest.newBuilder(tt)
				.setSs("ThisIsTheUpdateTest")
				.build();
			
			TestDomain.SimpleTest t2 = db.save(tt);
			assertNotEquals(t2.getID(), -1);			
			assertEquals(tt.getID(), t2.getID());
		
			TestDomain.SimpleTest st = db.get(t2.getID(), TestDomain.SimpleTest.getDefaultInstance());
			
			assertEquals(t.getBb(), st.getBb());
			assertEquals(t.getDd(), st.getDd(), 0.0);
			assertEquals(t.getFf(), st.getFf(), 0.0);
			assertEquals(t.getIl(), st.getIl());
			assertEquals(t.getIs(), st.getIs());
			assertNotEquals(t.getSs(), st.getSs());
			assertArrayEquals(t.getBy().toByteArray(), st.getBy().toByteArray());
			
//			PreparedStatement prep = "SELECT * FROM SimpleTest";
//			
//			testTableStructure(db, "SimpleTest", SIMPLE_FIELD_NAMES, SIMPLE_FIELD_TYPES);

			
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException e) {
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
			
			TestDomain.EnumOne oo = db.save(t);
			//int id = db.save(t);	
			
			assertNotEquals(oo.getID(), -1);
			assertEquals(t.getRating().toString(), oo.getRating().toString());
			assertEquals(t.getTitle(), oo.getTitle());
			
			TestDomain.EnumOne pp = db.get(oo.getID(), TestDomain.EnumOne.getDefaultInstance());
			
			assertEquals(oo.getID(), pp.getID());
			assertEquals(oo.getRating().toString(), pp.getRating().toString());
			assertEquals(oo.getTitle(), pp.getTitle());
						
		} catch (SQLException | ClassNotFoundException | IDFieldNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}	

}
