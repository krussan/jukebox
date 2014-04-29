package se.qxx.protodb;

import java.rmi.UnexpectedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import se.qxx.protodb.exceptions.IDFieldNotFoundException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class ProtoDB {
	private String connectionString = "jdbc:sqlite:jukebox.db";
	
	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  PROPS
	//---------------------------------------------------------------------------------


	private String getConnectionString() {
		return connectionString;
	}
	
	private void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	
	//---------------------------------------------------------------------------------
	//------------------------------------------------------------------ CONSTRUCTORS
	//---------------------------------------------------------------------------------
	
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
	
	/***
	 * Database function for retrieving column information 
	 * @param table
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private ResultSet retreiveColumns(String table, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(String.format("PRAGMA table_info('%s')", table));
		
		return prep.executeQuery();
	}

//	/***
//	 * Creates a list of table columns mapped to a java type
//	 * @param table
//	 * @param conn
//	 * @throws SQLException
//	 */
//	protected void setupColumns(String table, Connection conn) throws SQLException {
//		ResultSet rs = retreiveColumns(table, conn);
//		
//		Map<String, Type> map = new HashMap<String, Type>();
//		List<String> ret = new ArrayList<String>();
//
//		while (rs.next()) {
//			ret.add(rs.getString("name"));
//			
//			String columnName = rs.getString("name");
//			String type = rs.getString("type");
//			
//			Class<?> c = String.class;
//			if (StringUtils.equalsIgnoreCase(type, "BOOL"))
//				c = Boolean.class;
//			if (StringUtils.equalsIgnoreCase(type, "BOOLEAN"))
//				c = Boolean.class;			
//			else if (StringUtils.equalsIgnoreCase(type, "DATE"))
//				c = Date.class;
//			else if (StringUtils.equalsIgnoreCase(type, "DATETIME"))
//				c = Date.class;			
//			else if (StringUtils.equalsIgnoreCase(type, "INT"))
//				c = Integer.class;
//			else if (StringUtils.equalsIgnoreCase(type, "INTEGER"))
//				c = Integer.class;
//			else if (StringUtils.equalsIgnoreCase(type, "TINYINT"))
//				c = Integer.class;
//			else if (StringUtils.equalsIgnoreCase(type, "SMALLINT"))
//				c = Integer.class;
//			else if (StringUtils.equalsIgnoreCase(type, "MEDIUMINT"))
//				c = Integer.class;
//			else if (StringUtils.equalsIgnoreCase(type, "BIGINT"))
//				c = BigInteger.class;
//			else if (StringUtils.equalsIgnoreCase(type, "UNSIGNED BIG INT"))
//				c = BigInteger.class;
//			else if (StringUtils.equalsIgnoreCase(type, "INT2"))
//				c = Integer.class;
//			else if (StringUtils.equalsIgnoreCase(type, "INT8"))
//				c = Integer.class;	
//			else if (StringUtils.equalsIgnoreCase(type, "BLOB"))
//				c = Byte.class;
//			else if (StringUtils.equalsIgnoreCase(type, "REAL"))
//				c = Float.class;
//			else if (StringUtils.equalsIgnoreCase(type, "DOUBLE"))
//				c = Double.class;
//			else if (StringUtils.equalsIgnoreCase(type, "DOUBLE PRECISION"))
//				c = Double.class;
//			else if (StringUtils.equalsIgnoreCase(type, "FLOAT"))
//				c = Float.class;
//			else if (StringUtils.equalsIgnoreCase(type, "NUMERIC"))
//				c = Float.class;
//			else if (StringUtils.startsWithIgnoreCase(type, "NUMERIC"))
//				c = Float.class;
//			else if (StringUtils.startsWithIgnoreCase(type, "DECIMAL"))
//				c = Float.class;
//						
//			map.put(columnName, c);
//		}
//	
////		this.setColumnMap(map);
////		this.setColumns(ret);
//	}

	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  INIT
	//---------------------------------------------------------------------------------
	
	/***
	 * Initializes a database connection (SQLite)
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected Connection initialize() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
	    return DriverManager.getConnection(this.getConnectionString());				
	}
	
	/***
	 * Disconnects the database and the conneciton object
	 * @param conn
	 */
	private void disconnect(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	/***
	 * Internal function for setting the database connection string
	 * @param databaseFilename
	 */
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

	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  SETUP
	//---------------------------------------------------------------------------------

	/***
	 * Purges the database from all tables and sets up the whole database structure from
	 * one given protobuf class.
	 * @param b
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IDFieldNotFoundException 
	 */
	public void setupDatabase(MessageOrBuilder b) throws SQLException,ClassNotFoundException, IDFieldNotFoundException {
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
	
	
	/**
	 * Purges the database from all tables and sets up the whole database structure from
	 * one given protobuf class.
	 * @throws SQLException 
	 * @throws IDFieldNotFoundException 
	 * @throws UnexpectedException 
	 */
	private void setupDatabase(MessageOrBuilder b, Connection conn) throws SQLException, IDFieldNotFoundException {
		ProtoDBScanner scanner = new ProtoDBScanner(b);
		
		// check fields for ID field - this has to be present
		Boolean idFieldFound = false;
		for (FieldDescriptor field : scanner.getBasicFields()) {
			if (field.getName().equalsIgnoreCase("ID") 
					&& field.getJavaType() == JavaType.INT
					&& field.isRequired())
				idFieldFound = true;
		}
		if (!idFieldFound)
			throw new IDFieldNotFoundException(scanner.getObjectName());
		
		// setup all sub objects
		for(FieldDescriptor field : scanner.getObjectFields()) {
			if (!field.isRepeated()) {
				if (field.getJavaType() == JavaType.MESSAGE)
					setupDatabase((MessageOrBuilder)b.getField(field), conn);
				else if (field.getJavaType() == JavaType.ENUM){
					setupDatabase(field.getEnumType(), conn);
				}
			}
		}
		
		// setup blob data if blobs exist
		if (scanner.getBlobFields().size() > 0)
			setupBlobdata(conn);
			
		// setup this object
		if (!tableExist(scanner.getObjectName(), conn)) {
			executeStatement(scanner.getCreateStatement(), conn);
		}
		
		// setup all repeated fields as many-to-many relations
		for(FieldDescriptor field : scanner.getRepeatedObjectFields()) {
			if (field.getJavaType() == JavaType.MESSAGE) {
				Descriptor mt = field.getMessageType();
				DynamicMessage mg = DynamicMessage.getDefaultInstance(mt);
				if (mg instanceof MessageOrBuilder) {
					MessageOrBuilder b2 = (MessageOrBuilder)mg;
					
					// create other object
					setupDatabase(b2, conn);
					
					// create link table
					ProtoDBScanner other = new ProtoDBScanner(b2);
					if (!tableExist(scanner.getLinkTableName(other, field.getName()), conn))
						executeStatement(scanner.getLinkCreateStatement(other, field.getName()), conn);
				}
			}
			else if (field.getJavaType() == JavaType.ENUM) {
				setupDatabase(field.getEnumType(), conn);
				
				if (!tableExist(scanner.getEnumLinkTableName(field), conn)) {
					PreparedStatement prep = conn.prepareStatement(
						scanner.getEnumLinkCreateStatement(field));
				
					prep.execute();
				}
			}
		}
		
		for (FieldDescriptor field : scanner.getRepeatedBasicFields()) {
			executeStatement(scanner.getBasicLinkCreateStatement(field), conn);
		}

	}
	
	private void setupDatabase(EnumDescriptor fieldName, Connection conn) throws SQLException {
		String tableName = StringUtils.capitalize(fieldName.getName());
		if (!tableExist(tableName, conn)) {
			String sql = "CREATE TABLE " + tableName + "(" 
					+ "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "value TEXT NOT NULL)";
			
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.execute();
			
			for (EnumValueDescriptor value : fieldName.getValues()) {
				sql = "INSERT INTO " + tableName + " (value) VALUES (?)";
				prep = conn.prepareStatement(sql);
				prep.setString(1, value.getName());
				
				prep.execute();
			}
		}
	}

	private void setupBlobdata(Connection conn) throws SQLException {
		if (!tableExist("BlobData", conn))
			executeStatement("CREATE TABLE BlobData (ID INTEGER PRIMARY KEY AUTOINCREMENT, data BLOB)", conn);
	}

	private void executeStatement(String sql, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(sql);
		prep.execute();			
	}
	
	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  GET
	//---------------------------------------------------------------------------------

	/***
	 * 
	 * @param <T>
	 * @param id
	 * @param desc
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public <T extends Message> T get(int id, T instance) throws ClassNotFoundException, SQLException{
		Connection conn = null;
		T msg = null;
		
		try {
			conn = this.initialize();
			
			msg = get(id, instance, conn);
			
		}
		finally {
			this.disconnect(conn);
		}		
		
		return msg;
	}

 	/***
	 * 
	 * @param id
	 * @return
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	private <T extends Message> T get(int id, T instance, Connection conn) throws SQLException{
		Builder b = instance.newBuilderForType();
		
		//DynamicMessage d = DynamicMessage.getDefaultInstance(desc);
		ProtoDBScanner scanner = new ProtoDBScanner(instance);
		
		
		// populate list of sub objects
		for (FieldDescriptor field : scanner.getRepeatedObjectFields()) {
			getLinkObject(id, b, scanner, field, conn);
		}

		// populate list of basic types
		for (FieldDescriptor field : scanner.getRepeatedBasicFields()) {
			PreparedStatement prep = conn.prepareStatement(scanner.getBasicLinkTableSelectStatement(field));
			prep.setInt(1, id);
			
			ResultSet rs = prep.executeQuery();
			
			while (rs.next()) {
				b.addRepeatedField(field, rs.getObject("value"));
			}
			rs.close();
		}			
		
		
		PreparedStatement prep = conn.prepareStatement(
				scanner.getSelectStatement(id));
		
		prep.setInt(1, id);
		
		b.setField(scanner.getIdField(), id);
		
		ResultSet rs = prep.executeQuery();
		while(rs.next()) {
			// populate object fields
			for (FieldDescriptor field : scanner.getObjectFields()) {
				int otherID = rs.getInt(scanner.getObjectFieldName(field));
				
				if (field.getJavaType() == JavaType.MESSAGE) {
					DynamicMessage innerInstance = DynamicMessage.getDefaultInstance(field.getMessageType());
					DynamicMessage otherMsg = get(otherID, innerInstance, conn);
					b.setField(field, otherMsg);
				}
				else if (field.getJavaType() == JavaType.ENUM){
					b.setField(field, field.getEnumType().findValueByNumber(otherID));
				}
			}
			
			// populate blobs
			for (FieldDescriptor field : scanner.getBlobFields()) {
				int otherID = rs.getInt(scanner.getObjectFieldName(field));
				byte[] data = getBlob(otherID, conn);
				
				if (data != null)
					b.setField(field, ByteString.copyFrom(data));
			}
			
			// populate basic fields			
			for (FieldDescriptor field : scanner.getBasicFields()) {
				if (field.getName().equalsIgnoreCase("ID")) {
					b.setField(field, id);
				}
				else {
					Object o = rs.getObject(field.getName().toLowerCase());
					if (field.getJavaType() == JavaType.FLOAT)
						b.setField(field, ((Double)o).floatValue());
					else if (field.getJavaType() == JavaType.LONG)
						b.setField(field, ((Integer)o).longValue());
					else if (field.getJavaType() == JavaType.BOOLEAN ) {
						if (o instanceof Integer) 
							b.setField(field, ((int)o) == 1 ? true : false);
						else
							b.setField(field, ((String)o).equals("Y") ? true : false);	
					}
						
					else
						b.setField(field, o);
					
					
					;
				}
			}			
		}
		return (T) b.build();
	}

	private byte[] getBlob(int otherID, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT data FROM BlobData WHERE ID = ?");
		prep.setInt(1, otherID);
		byte[] data = null;
		
		ResultSet rs = prep.executeQuery();
		while(rs.next()){
			data = rs.getBytes("data");
		}
		rs.close();

		
		return data;
	}

	protected void getLinkObject(int id
			, Builder b
			, ProtoDBScanner scanner			
			, FieldDescriptor field
			, Connection conn)
			throws SQLException {
		
		Descriptor mt = field.getMessageType();
		DynamicMessage mg = DynamicMessage.getDefaultInstance(mt);
		
		if (mg instanceof MessageOrBuilder) {
			MessageOrBuilder b2 = (MessageOrBuilder)mg;
			ProtoDBScanner other = new ProtoDBScanner(b2);
		
			if (field.isRepeated()) {
				// get select statement for link table
				PreparedStatement prep = conn.prepareStatement(scanner.getLinkTableSelectStatement(other, field.getName()));
				prep.setInt(1, id);
				
				ResultSet rs = prep.executeQuery();
				
				while(rs.next()) {
					// get sub objects
					DynamicMessage otherMsg = get(rs.getInt("ID"), mg, conn);
					b.addRepeatedField(field, otherMsg);
				}
				
				rs.close();
			}
		}
	}

	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  SAVE
	//---------------------------------------------------------------------------------

	/***
	 * Saves a protobuf class to database.
	 * @param b
	 * @return 
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public <T extends Message> T save(T b) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		T nb;
		
		try {
			conn = this.initialize();
			conn.setAutoCommit(false);
			
			nb = this.save(b, conn);
			
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
		
		return nb;

	}

	/***
	 * Internal save function. Saves a protobuf class to database.
	 * @param b
	 * @param conn
	 * @return 
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	private <T extends Message> T save(T b, Connection conn) throws SQLException, ClassNotFoundException {
		//TODO: We need to set the ID property of each object
		// without this subsequent calls to save will corrupt the database.

		// create a new builder to store the new object
		Builder mainBuilder = b.newBuilderForType();
		ProtoDBScanner scanner = new ProtoDBScanner(b);
		
		//check for existence. UPDATE if present!
		Boolean objectExist = checkExisting(scanner, conn);
		
		// getObjectFields
		// getBasicFields
		// getRepeatedObjectFields
		// getRepeatedBasicFIelds
		
		// save underlying objects
		for(FieldDescriptor field : scanner.getObjectFields()) {
			String fieldName = field.getName();
			Object o = b.getField(field);
			
			if (field.getJavaType() == JavaType.MESSAGE && !field.isRepeated()) {
				ProtoDBScanner other = new ProtoDBScanner((Message)o);
				Message ob = save((Message)o, conn);				
				scanner.addObjectID(fieldName, (int)ob.getField(other.getIdField()));
				
				//subBuilder.setField(other.getIdField(), objectID);
				mainBuilder.setField(field, ob);
				
			}
			else if (field.getJavaType() == JavaType.ENUM && !field.isRepeated()) {
				int enumID = saveEnum(field, ((EnumValueDescriptor)o).getName(), conn);
				mainBuilder.setField(field, ((EnumValueDescriptor)o));
				scanner.addObjectID(fieldName, enumID);
			}
		}
		

		// delete blobs
		if (objectExist)
			deleteBlobs(scanner, conn);

		// save blobs
		for(FieldDescriptor field : scanner.getBlobFields()) {
			String fieldName = field.getName();
			
			ByteString bs = (ByteString)b.getField(field);
			int blobID = saveBlob(bs.toByteArray(), conn);
			scanner.addBlobID(fieldName, blobID);
			mainBuilder.setField(field, bs);
		}		
		
		// save this object
		int thisID = saveThisObject(b, scanner, objectExist, conn);
		mainBuilder.setField(scanner.getIdField(), thisID);
		
		// populate basic fields in return message
		for(FieldDescriptor field : scanner.getBasicFields())
			if (!field.getName().equalsIgnoreCase("ID"))
				mainBuilder.setField(field, b.getField(field));
		
		// save underlying repeated objects
		for(FieldDescriptor field : scanner.getRepeatedObjectFields()) {
			// for each repeated field get insert statement according to _this_ID, _other_ID
			int fieldCount = b.getRepeatedFieldCount(field);
			for (int i=0;i<fieldCount;i++) {
				Object mg = b.getRepeatedField(field, i);
				if (mg instanceof Message) {
					Message b2 = (Message)mg;
					ProtoDBScanner other = new ProtoDBScanner(b2);
					
					// save other object
					Message ob = save(b2, conn);
					
					// delete from link table
					deleteLinkObject(scanner, other, field, conn);
					
					// save link table
					int otherID = (int)ob.getField(other.getIdField());
					saveLinkObject(scanner, other, field, thisID, otherID, conn);
					
					mainBuilder.addRepeatedField(field, ob);
				}
			}
		}
		
		// save underlying repeated basic types
		for (FieldDescriptor field : scanner.getRepeatedBasicFields()) {
			// delete from link table
			deleteBasicLinkObject(scanner, field, conn);
			
			// add each value to link table
			int fieldCount = b.getRepeatedFieldCount(field);
			for (int i=0;i<fieldCount;i++) {
				Object value = b.getRepeatedField(field, i);
				saveLinkBasic(scanner, thisID, field, value, conn);
				mainBuilder.addRepeatedField(field, value);
			}			
		}
				
		return (T)mainBuilder.build();
	}
	
	private void deleteBasicLinkObject(ProtoDBScanner scanner, FieldDescriptor field, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(scanner.getBasicLinkTableDeleteStatement(field));
		prep.setInt(1, scanner.getIdValue());
		
		prep.execute();
	}

	private void deleteLinkObject(ProtoDBScanner scanner, 
			ProtoDBScanner other,
			FieldDescriptor field,
			Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(scanner.getLinkTableDeleteStatement(other, field.getName()));
		prep.setInt(1, scanner.getIdValue());
		
		prep.execute();
	}

	private void deleteBlobs(ProtoDBScanner scanner, Connection conn) throws SQLException {
		for (FieldDescriptor field : scanner.getBlobFields()) {
			String sql = "DELETE FROM BlobData WHERE ID IN (SELECT " + scanner.getObjectFieldName(field) + " FROM " + scanner.getObjectName() + " WHERE ID = ?)";
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setInt(1, scanner.getIdValue());
			
			prep.execute();
		}		
	}
	
	private Boolean checkExisting(ProtoDBScanner scanner, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT COUNT(*) FROM " + scanner.getObjectName() + " WHERE ID = ?");
		prep.setInt(1, scanner.getIdValue());
		
		ResultSet rs = prep.executeQuery();
		Boolean exists = false;
		if (rs.next())
			exists = rs.getInt(1) > 0;
			
		rs.close();
		prep.close();
		return exists;
	}

	private int saveBlob(byte[] data, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("INSERT INTO BlobData (data) VALUES (?)");
		prep.setBytes(1, data);
		prep.execute();
		
		return getIdentity(conn);
	}
	
	private int saveEnum(FieldDescriptor field, String value, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT ID FROM " + field.getName() + " WHERE value = ?");
		prep.setString(1, value);
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt(1);
		else
			return -1;
		
	}

	/***
	 * Saves a repeated list of basic types to link table
	 * @param scanner
	 * @param thisID
	 * @param field
	 * @param value
	 * @param conn
	 * @throws SQLException
	 */
	protected void saveLinkBasic(ProtoDBScanner scanner
			, int thisID
			, FieldDescriptor field
			, Object value
			, Connection conn)
			throws SQLException {
		
		
		PreparedStatement prep = 
			scanner.compileLinkBasicArguments(
				scanner.getBasicLinkInsertStatement(field)
					, thisID
					, field.getJavaType()
					, value
					, conn);
		
		prep.execute();
	}

	/***
	 * Saves a repeated list of objects to link table (many-to-many)
	 * @param b2
	 * @param scanner
	 * @param field
	 * @param thisID
	 * @param otherID
	 * @param conn
	 * @throws SQLException
	 */
	private void saveLinkObject(
			  ProtoDBScanner scanner
			, ProtoDBScanner other
			, FieldDescriptor field
			, int thisID
			, int otherID
			, Connection conn) throws SQLException {
		scanner.getLinkTableInsertStatement(other, field.getName());
		
		PreparedStatement prep = conn.prepareStatement(scanner.getLinkTableInsertStatement(other, field.getName()));
		prep.setInt(1, thisID);
		prep.setInt(2, otherID);
		
		prep.execute();
	}
	
	/***
	 * Saves the original object (without references to other objects or lists)
	 * @param b
	 * @param scanner
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private int saveThisObject(MessageOrBuilder b, ProtoDBScanner scanner, Boolean objectExist, Connection conn) throws SQLException {
		// getInsertStatement
		String sql = scanner.getSaveStatement(objectExist);
		
		// prepareStatement
		PreparedStatement prep = scanner.compileArguments(b, sql, objectExist, conn);
		
		// execute
		prep.execute();
		
		int id = -1;
		if (objectExist)
			id = scanner.getIdValue();
		else
			id = getIdentity(conn);
		
		return id;
	}

	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  DELETE
	//---------------------------------------------------------------------------------

	/***
	 * Deletes a protobuf class to database.
	 * @param b
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void delete(MessageOrBuilder b) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		
		try {
			conn = this.initialize();
			conn.setAutoCommit(false);
			
			this.delete(b, conn);
			
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

	/***
	 * Internal delete function. Deletes protobuf class from database.
	 * @param b
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	private void delete(MessageOrBuilder b, Connection conn) throws SQLException, ClassNotFoundException {
		ProtoDBScanner scanner = new ProtoDBScanner(b);
				
		// delete underlying objects
		for(FieldDescriptor field : scanner.getObjectFields()) {
			Object o = b.getField(field);

			if (field.getJavaType() == JavaType.MESSAGE && !field.isRepeated()) {
				delete((MessageOrBuilder)o, conn);
			}
		}		

		deleteBlobs(scanner, conn);
		
		// delete underlying repeated objects
		for(FieldDescriptor field : scanner.getRepeatedObjectFields()) {
			int fieldCount = b.getRepeatedFieldCount(field);
			for (int i=0;i<fieldCount;i++) {
				Object mg = b.getRepeatedField(field, i);
				if (mg instanceof MessageOrBuilder) {
					MessageOrBuilder b2 = (MessageOrBuilder)mg;
					ProtoDBScanner other = new ProtoDBScanner(b2);
					
					// delete other object
					delete(b2, conn);
					
					// delete from link table
					deleteLinkObject(scanner, other, field, conn);
				}
			}
		}
		
		// delete underlying repeated basic types
		for (FieldDescriptor field : scanner.getRepeatedBasicFields()) {
			// delete from link table
			deleteBasicLinkObject(scanner, field, conn);			
		}
				
	}

	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  SEARCH
	//---------------------------------------------------------------------------------

	/***
	 * 
	 * @param desc
	 * @param fieldName
	 * @param searchFor
	 * @param isLikeOperator
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws SearchFieldNotFoundException 
	 */
	public <T extends Message> List<T> find(T instance, String fieldName, Object searchFor, Boolean isLikeOperator) throws ClassNotFoundException, SQLException, SearchFieldNotFoundException {
		// if field is repeated -> search link objects
		Connection conn = null;
		List<T> result = new ArrayList<T>();
		
		try {
			conn = this.initialize();
			
			result = this.find(instance, fieldName, searchFor, isLikeOperator, conn);
			
		}
		finally {
			this.disconnect(conn);
		}
		
		return result;		
	}
	
	private <T extends Message> List<T> find(T instance, String fieldName, Object searchFor, Boolean isLikeFilter, Connection conn) throws SearchFieldNotFoundException, SQLException, ClassNotFoundException {
		List<T> result = new ArrayList<T>();
//		DynamicMessage dm = DynamicMessage.getDefaultInstance(desc);
		ProtoDBScanner scanner = new ProtoDBScanner(instance);
		
		FieldDescriptor matchingField = null;

		// Split fieldName on dot (.) to be able to search sub objects
		// fieldName should then be on the form <object>.[<object>...].fieldName
		String[] fieldParts = StringUtils.split(fieldName, ".");
		PreparedStatement prep = null;
		
		if (fieldParts.length == 1) {
			for (FieldDescriptor field : scanner.getBasicFields()) {
				if (field.getName().equalsIgnoreCase(fieldName)) {
					matchingField = field;
					break;
				}
			}
			
			if (matchingField == null)
				throw new SearchFieldNotFoundException(fieldName, scanner.getObjectName());
			
			prep = conn.prepareStatement(scanner.getSearchStatement(matchingField, isLikeFilter));
			if (matchingField.getJavaType() == JavaType.BOOLEAN)
				prep.setString(1, (Boolean)searchFor ? "Y": "N");
			else
				prep.setObject(1, searchFor);

			//TODO: check repeated basic fields
		}
		else {
			// object fields
			for (FieldDescriptor field : scanner.getObjectFields()) {
				if (field.getName().equalsIgnoreCase(fieldParts[0])) {
					matchingField = field;
					
					List<DynamicMessage> matchingSubObjects = null;
					List<Integer> ids = new ArrayList<Integer>();
					if (field.getJavaType() == JavaType.MESSAGE) {
						DynamicMessage innerInstance = DynamicMessage.getDefaultInstance(field.getMessageType());
						matchingSubObjects = 
							find(innerInstance,
								StringUtils.join(ArrayUtils.subarray(fieldParts, 1, fieldParts.length), "."),
								searchFor,
								isLikeFilter,
								conn);
						
						for (DynamicMessage m : matchingSubObjects)
							for (FieldDescriptor f : m.getDescriptorForType().getFields())
								if (f.getName().equalsIgnoreCase("ID"))
									ids.add((int)m.getField(f));
						
					}
					else if (field.getJavaType() == JavaType.ENUM) {
						ids =
							find(field.getEnumType(),
								scanner,
								StringUtils.join(ArrayUtils.subarray(fieldParts, 1, fieldParts.length), "."),
								searchFor,
								conn);
					}
					
					// get all messages of this type that have matching sub objects
					prep = conn.prepareStatement(scanner.getSearchStatementSubObject(field, ids));
				}
			}
			
			if (matchingField == null) {
				// check repeated fields
				for (FieldDescriptor f : scanner.getRepeatedObjectFields()) {
					//find sub objects that match the criteria
					if (f.getName().equalsIgnoreCase(fieldParts[0])) {
						matchingField = f;
						List<DynamicMessage> dmObjects = find(DynamicMessage.getDefaultInstance(f.getMessageType())
								, StringUtils.join(ArrayUtils.subarray(fieldParts, 1, fieldParts.length), ".")
								, searchFor
								, isLikeFilter
								, conn);
						
						if (dmObjects.size() > 0) {
							List<Integer> ids = new ArrayList<Integer>();
							FieldDescriptor idField = f.getMessageType().findFieldByName("ID");
							if (idField != null) 
								for (DynamicMessage dmpart : dmObjects)
									ids.add((int)dmpart.getField(idField));
							
							
							// find id:s of obejcts
//							for (DynamicMessage dmpart : dmObjects) {
//								for (FieldDescriptor idfield : dmpart.getDescriptorForType().getFields()) {
//									if (idfield.getName().equalsIgnoreCase("ID")) {
//										ids.add((int)dmpart.getField(idfield));
//									}
//								}
//							}
							
							//find main objects that contain the sub objects (ID-wise)
							ProtoDBScanner other = new ProtoDBScanner(dmObjects.get(0));
							prep = conn.prepareStatement(scanner.getSearchStatementLinkObject(f, other, ids));
						}
					}
				}
				
				if (matchingField == null)
					throw new SearchFieldNotFoundException(fieldName, scanner.getObjectName());
				
			}
			
			//object.fieldName
		}
			
		if (prep != null) {
			ResultSet rs = prep.executeQuery();
			List<Integer> ids = new ArrayList<Integer>();
			while (rs.next()) {
				ids.add(rs.getInt(1));
			}
			
			for (int i : ids) {
				result.add(this.get(i, instance, conn));
			}
		}
		
		return result;
	}
	

	private List<Integer> find(
			EnumDescriptor enumType, 
			ProtoDBScanner scanner,
			String fieldName,
			Object searchFor, 
			Connection conn) throws SearchFieldNotFoundException, SQLException {
		
		List<Integer> ids = new ArrayList<Integer>();
		String[] fieldParts = StringUtils.split(fieldName, ".");
		if (fieldParts.length == 1) {
			PreparedStatement prep = conn.prepareStatement(scanner.getSearchStatement(enumType));
			prep.setString(1, searchFor.toString());

			ResultSet rs = prep.executeQuery();
			
			while (rs.next()) {
				ids.add(rs.getInt(1));
			}
			rs.close();
		}
		else {
			throw new SearchFieldNotFoundException(fieldName, enumType.toString());
		}
		return ids;
	}
	

	
	//---------------------------------------------------------------------------------
	//----------------------------------------------------------------------  HELPERS
	//---------------------------------------------------------------------------------

	/***
	 * Internal function to get the latest inserted row ID
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static int getIdentity(Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement("SELECT last_insert_rowid()");
		ResultSet rs = prep.executeQuery();
		
		if (rs.next())
			return rs.getInt(1);
		else
			return -1;
	}
	
	
	/***
	 * Internal function to check if table exists
	 * @param tableName
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
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
	
}
