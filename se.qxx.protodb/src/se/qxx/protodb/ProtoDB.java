package se.qxx.protodb;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class ProtoDB {
	private String connectionString = "jdbc:sqlite:jukebox.db";


	private String getConnectionString() {
		return connectionString;
	}
	
	private void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}
	
	
	protected ProtoDB() {
	}
	
	public ProtoDB(String databaseFilename) {
		this.setDatabase(databaseFilename);
	}	
	
//	protected List<String> getColumnList(Connection conn) throws SQLException {
//		List<String> ret = this.getColumns();
//		if (ret == null || ret.size() == 0)
//			setupColumns(this.getTable(), conn);
//		
//		return this.getColumns();
//	}
	
	public List<Pair<String, String>> retreiveColumns(String table) throws SQLException, ClassNotFoundException {
		List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
		
		Connection conn = null;
		ResultSet rs =null;
		try {
			conn = this.initialize();
			
			rs = retreiveColumns(table, conn);		
			
			while(rs.next()) {
				String fieldName = rs.getString("name");
				String fieldType = rs.getString("type");

				Pair<String,String> p = new MutablePair<String, String>(fieldName, fieldType);
				ret.add(p);
			}
		}
		finally {
			this.disconnect(conn);
		}
		
		return ret;
	}
	
	private ResultSet retreiveColumns(String table, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(String.format("PRAGMA table_info('%s')", table));
		
		return prep.executeQuery();
	}
	
	protected void setupColumns(String table, Connection conn) throws SQLException {
		ResultSet rs = retreiveColumns(table, conn);
		
		Map<String, Type> map = new HashMap<String, Type>();
		List<String> ret = new ArrayList<String>();

		while (rs.next()) {
			ret.add(rs.getString("name"));
			
			String columnName = rs.getString("name");
			String type = rs.getString("type");
			
			Class<?> c = String.class;
			if (StringUtils.equalsIgnoreCase(type, "BOOL"))
				c = Boolean.class;
			if (StringUtils.equalsIgnoreCase(type, "BOOLEAN"))
				c = Boolean.class;			
			else if (StringUtils.equalsIgnoreCase(type, "DATE"))
				c = Date.class;
			else if (StringUtils.equalsIgnoreCase(type, "DATETIME"))
				c = Date.class;			
			else if (StringUtils.equalsIgnoreCase(type, "INT"))
				c = Integer.class;
			else if (StringUtils.equalsIgnoreCase(type, "INTEGER"))
				c = Integer.class;
			else if (StringUtils.equalsIgnoreCase(type, "TINYINT"))
				c = Integer.class;
			else if (StringUtils.equalsIgnoreCase(type, "SMALLINT"))
				c = Integer.class;
			else if (StringUtils.equalsIgnoreCase(type, "MEDIUMINT"))
				c = Integer.class;
			else if (StringUtils.equalsIgnoreCase(type, "BIGINT"))
				c = BigInteger.class;
			else if (StringUtils.equalsIgnoreCase(type, "UNSIGNED BIG INT"))
				c = BigInteger.class;
			else if (StringUtils.equalsIgnoreCase(type, "INT2"))
				c = Integer.class;
			else if (StringUtils.equalsIgnoreCase(type, "INT8"))
				c = Integer.class;	
			else if (StringUtils.equalsIgnoreCase(type, "BLOB"))
				c = Byte.class;
			else if (StringUtils.equalsIgnoreCase(type, "REAL"))
				c = Float.class;
			else if (StringUtils.equalsIgnoreCase(type, "DOUBLE"))
				c = Double.class;
			else if (StringUtils.equalsIgnoreCase(type, "DOUBLE PRECISION"))
				c = Double.class;
			else if (StringUtils.equalsIgnoreCase(type, "FLOAT"))
				c = Float.class;
			else if (StringUtils.equalsIgnoreCase(type, "NUMERIC"))
				c = Float.class;
			else if (StringUtils.startsWithIgnoreCase(type, "NUMERIC"))
				c = Float.class;
			else if (StringUtils.startsWithIgnoreCase(type, "DECIMAL"))
				c = Float.class;
						
			map.put(columnName, c);
		}
	
//		this.setColumnMap(map);
//		this.setColumns(ret);
	}
	
	protected Connection initialize() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
	    return DriverManager.getConnection(this.getConnectionString());				
	}
	
	private void disconnect(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}
	
	private void setDatabase(String databaseFilename) {
		this.setConnectionString(String.format("jdbc:sqlite:%s", databaseFilename));
	}
	
//	private Builder<?> mapResultSet(Builder<?> b, ResultSet rs, Connection conn) throws SQLException {
//		List<FieldDescriptor> fields = b.getDescriptorForType().getFields();
//
//		for(FieldDescriptor field : fields) {
//			String fieldName = field.getName();
//			String fieldNameRep = fieldName.replace("_", "").replace(".","");
//			List<String> columnList = this.getColumnList(conn);
//			for (String s : columnList) {
//				String sRep = s.replace("_", "").replace(".","");
//				if (StringUtils.equalsIgnoreCase(sRep, fieldNameRep)) {
//					b.setField(field, rs.getObject(fieldName));
//					break;
//				}
//					
//			}
//		}		
//		
//		return b;
//	}
//	
	
	public void setupDatabase(MessageOrBuilder b) throws SQLException,ClassNotFoundException {
		Connection conn = null;
		
		try {
			conn = this.initialize();
			conn.setAutoCommit(false);
			
			this.setupDatabase(b, conn);
			
			conn.commit();
		}
		catch (SQLException e) {			
			try {
				conn.rollback();
			} catch (SQLException sqlEx) {}
			
			throw e;
		}
		finally {
			this.disconnect(conn);
		}
	}
	
	private boolean tableExist(String tableName, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT COUNT(1) FROM sqlite_master WHERE type='table' AND name=?");
		prep.setString(1, tableName);
		
		boolean b = false;
		ResultSet rs = prep.executeQuery();
		if (rs.next())
			if (rs.getInt(1)==1)
				b = true;
		
		return b;
		
	}
	/**
	 * Purges the database from all tables and sets up the whole database structure from
	 * one given protobuf class.
	 * @throws SQLException 
	 */
	private void setupDatabase(MessageOrBuilder b, Connection conn) throws SQLException {
		ProtoDBScanner scanner = new ProtoDBScanner(b);
		
		// setup all sub objects
		for(FieldDescriptor field : scanner.getObjectFields()) {
//			String fieldName = field.getName();
//			Object o = b.getField(field);
			
			if (field.getJavaType() == JavaType.MESSAGE && !field.isRepeated()) {
				setupDatabase((MessageOrBuilder)b.getField(field), conn);
			}
		}
			
		// setup this object
		if (!tableExist(scanner.getObjectName(), conn)) {
			executeStatement(scanner.getCreateStatement(), conn);
		}
		
		// setup all repeated fields as many-to-many relations
		for(FieldDescriptor field : scanner.getRepeatedObjectFields()) {
			if (field.getJavaType() == JavaType.MESSAGE && field.isRepeated()) {
				MessageOrBuilder b2 = (MessageOrBuilder)b.getField(field);
				
				// create other object
				setupDatabase(b2, conn);
				
				// create link table
				executeStatement(scanner.getLinkCreateStatement(b2), conn);
			}
		}

	}
	
	private void executeStatement(String sql, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(sql);
		prep.execute();			
	}

	public int save(MessageOrBuilder b) {
		Connection conn = null;
		int id = -1;
		
		try {
			conn = this.initialize();
			conn.setAutoCommit(false);
			
			id = this.save(b, conn);
			
			conn.commit();
		}
		catch (Exception e) {			
			try {
				conn.rollback();
			} catch (SQLException sqlEx) {}
			
			return -1;
		}
		finally {
			this.disconnect(conn);
		}
		
		return id;

	}
	
	private int save(MessageOrBuilder b, Connection conn) throws SQLException {
		ProtoDBScanner scanner = new ProtoDBScanner(b);

		// getObjectFields
		// getBasicFields
		// getRepeatedObjectFields
		// getRepeatedBasicFIelds
		
		// save underlying objects
		for(FieldDescriptor field : scanner.getObjectFields()) {
			String fieldName = field.getName();
			Object o = b.getField(field);

			if (o instanceof MessageOrBuilder && !field.isRepeated()) {
				int id = save((MessageOrBuilder)o);
				scanner.addObjectID(fieldName, id);
			}
		}		

		// save this object
		int id = saveThisObject(b, scanner, conn);
		
		// save underlying repeated objects
		
		// save underlying repeated basic types

		return -1;
	}
	
	private int saveThisObject(MessageOrBuilder b, ProtoDBScanner scanner, Connection conn) throws SQLException {
		// getInsertStatement
		String sql = scanner.getInsertStatement();
		
		// prepareStatement
		PreparedStatement prep = scanner.compileArguments(b, sql, conn);
		
		// execute
		prep.execute();
		
		return getIdentity(conn);
	}

	private static int getIdentity(Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt(1);
		else
			return -1;
	}
}
