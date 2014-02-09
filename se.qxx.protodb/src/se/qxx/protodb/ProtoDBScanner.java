package se.qxx.protodb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class ProtoDBScanner {

	String objectName;
	private MessageOrBuilder message = null;
	
	private List<FieldDescriptor> objectFields = new ArrayList<FieldDescriptor>();
	private List<String> objectFieldTargets = new ArrayList<String>();

	private List<FieldDescriptor> repeatedObjectFields = new ArrayList<FieldDescriptor>();
	private List<String> repeatedObjectFieldTargets = new ArrayList<String>();
	
	private List<FieldDescriptor> basicFields = new ArrayList<FieldDescriptor>();
	private List<FieldDescriptor> repeatedBasicFields = new ArrayList<FieldDescriptor>();
	private HashMap<String, Integer> objectIDs = new HashMap<String,Integer>();
	
	private List<String> fieldNames = new ArrayList<String>();
	// getObjectFields
	// getBasicFields
	// getRepeatedObjectFields
	// getRepeatedBasicFIelds
	public ProtoDBScanner(MessageOrBuilder b) {
		this.setMessage(b);
		this.scan(b);
	}

	private void scan(MessageOrBuilder b) {
			
		this.setObjectName(StringUtils.capitalize(b.getDescriptorForType().getName()));

		List<FieldDescriptor> fields = b.getDescriptorForType().getFields();
		for(FieldDescriptor field : fields) {
//			Object o = b.getField(field);
//			ProtoDBScanner dbInternal = null;
			JavaType jType = field.getJavaType();
			
			if (field.isRepeated())
			{
				if (jType == JavaType.MESSAGE) {
					Descriptor mt = field.getMessageType();
					DynamicMessage mg = DynamicMessage.getDefaultInstance(mt);
				
					if (mg instanceof MessageOrBuilder) {
						MessageOrBuilder target = (MessageOrBuilder)mg;
						ProtoDBScanner dbInternal = new ProtoDBScanner(target);			

						this.addRepeatedObjectField(field);		
						this.addRepeatedObjectFieldTarget(dbInternal.getObjectName());
					}
				}
				else {
					this.addRepeatedBasicField(field);
				}

			}
			else {
				if (jType == JavaType.MESSAGE) {
					MessageOrBuilder target = (MessageOrBuilder)this.getMessage().getField(field);
					ProtoDBScanner dbInternal = new ProtoDBScanner(target);			
					
					this.addObjectField(field);
					this.addObjectFieldTarget(dbInternal.getObjectName());
				}				
				else {
					this.addBasicField(field);
				}
			}
		}		
		
		for(FieldDescriptor field : this.getObjectFields()) {
			fieldNames.add(getObjectFieldName(field));
		}
		for(FieldDescriptor field : this.getBasicFields()) {
			fieldNames.add(getBasicFieldName(field));
		}
				
	}

	public String getBasicFieldName(FieldDescriptor field) {
		return field.getName().toLowerCase();
	}

	public String getObjectFieldName(FieldDescriptor field) {
		return "_" + getBasicFieldName(field) + "_ID";
	}

	public String getInsertStatement() {
		
		List<String> cols = new ArrayList<String>();
		for (String fieldName : this.getFieldNames()) 
			if (!fieldName.equalsIgnoreCase("ID"))
				cols.add(String.format("[%s]", fieldName));
		
		String[] params = new String[cols.size()];
		Arrays.fill(params, "?");
		
		String sql = String.format(
				"INSERT INTO %s (%s) VALUES (%s)",
				this.getObjectName(),
				StringUtils.join(cols, ","),
				StringUtils.join(params, ","));
		
		return sql;
	}
	
	public String getLinkTableInsertStatement(ProtoDBScanner other, String fieldName) {

		String sql = String.format(
				"INSERT INTO " + this.getLinkTableName(other, fieldName) + " ("
				+ "_" + this.getObjectName().toLowerCase() + "_ID,"
				+ "_" + other.getObjectName().toLowerCase() + "_ID"				
				+ ") VALUES (?, ?)");
		
		return sql;
	}	
	
	public String getCreateStatement() {
		String sql = String.format("CREATE TABLE %s ", this.getObjectName());
		List<String> cols = new ArrayList<String>();
		
		for(int i=0;i<this.getObjectFields().size();i++) {
			FieldDescriptor field = this.getObjectFields().get(i);
			String target = this.getObjectFieldTargets().get(i);
			
			cols.add(String.format("[%s] %s %s REFERENCES %s (ID)",
					getObjectFieldName(field), 
					"INTEGER", 
					field.isOptional() ? "NULL" : "NOT NULL",
					target));
		}
		
		for(FieldDescriptor field : this.getBasicFields()) {
			if (!field.getName().equalsIgnoreCase("ID"))
				cols.add(String.format("[%s] %s %s", 
					getBasicFieldName(field), 
					getDBType(field), 
					field.isOptional() ? "NULL" : "NOT NULL"));
		}
		
		
		sql += "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + StringUtils.join(cols, ",") + ")";
		;
		return sql;
	}
	
	public String getLinkTableName(ProtoDBScanner other, String fieldName) {
		return this.getObjectName() + other.getObjectName() + "_" + StringUtils.capitalize(fieldName.replace("_", ""));
	}
	
	public String getBasicLinkTableName(FieldDescriptor field) {
		return this.getObjectName() + "_" + StringUtils.capitalize(field.getName().replace("_", ""));
	}
	
	public String getLinkCreateStatement(ProtoDBScanner other, String fieldName) {
		return String.format("CREATE TABLE %s ("
				+ "_" + this.getObjectName().toLowerCase() + "_ID INTEGER NOT NULL REFERENCES %s (ID),"
				+ "_" + other.getObjectName().toLowerCase() + "_ID INTEGER NOT NULL REFERENCES %s (ID)"
				+ ")", 
				this.getLinkTableName(other, fieldName), 
				this.getObjectName(),
				other.getObjectName());
		
	}
	
	
	public String getBasicLinkCreateStatement(FieldDescriptor field) {
		return String.format("CREATE TABLE %s ("
				+ "_" + this.getObjectName().toLowerCase() + "_ID INTEGER NOT NULL REFERENCES %s (ID),"
				+ "value %s NOT NULL"
				+ ")", 
				this.getBasicLinkTableName(field), 
				this.getObjectName(),
				this.getDBType(field));
		
	}	
	
	public String getSelectStatement(int id) {
		return "SELECT " + StringUtils.join(this.getFieldNames(), ",") 
			+ " FROM " + this.getObjectName()
			+ " WHERE ID = ?";
	}	
	
	public String getLinkTableSelectStatement(ProtoDBScanner other, String fieldName) {
		return " SELECT A._" + other.getObjectName().toLowerCase() + "_ID AS ID"
			+  " FROM " + this.getLinkTableName(other, fieldName) + " A"
			+  " WHERE A._" + this.getObjectName().toLowerCase() + "_ID = ?";
	}
	
	public String getBasicLinkTableSelectStatement(FieldDescriptor field) {
		return " SELECT value FROM " + this.getBasicLinkTableName(field)
			+  " WHERE _" + this.getObjectName().toLowerCase() + "_ID = ?";
	}	
	
	
	private String getDBType(FieldDescriptor field) {
		JavaType jType = field.getJavaType();
		String type = "TEXT";
		if (jType == JavaType.BOOLEAN)
			type = "BOOLEAN";
		else if (jType == JavaType.DOUBLE)
			type = "DOUBLE";
		else if (jType == JavaType.ENUM)
			type = "TINYINT";
		else if (jType == JavaType.FLOAT)
			type = "FLOAT";
		else if (jType == JavaType.INT)
			type = "INTEGER";
		else if (jType == JavaType.LONG)
			type = "BIGINT";
		else
			type ="TEXT";
		
		return type;
	}
	
	public PreparedStatement compileLinkBasicArguments(String sql, int thisID, JavaType jType, Object value, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(sql);
		
		prep.setInt(1, thisID);
		
		compileArgument(2, prep, jType, value);
		
		return prep;
	}

	public PreparedStatement compileArguments(MessageOrBuilder b, String sql, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(sql);

		int c = 0;
		for(FieldDescriptor field : this.getObjectFields()) {
			int id = this.getObjectID(field.getName());
			prep.setInt(++c, id);
		}
		
		for(FieldDescriptor field : this.getBasicFields())
			if (!field.getName().equalsIgnoreCase("ID"))
				this.compileArgument(++c, prep, field.getJavaType(), b.getField(field));			
		
		return prep;
	}

	private void compileArgument(int i, PreparedStatement prep, JavaType jType, Object value) throws SQLException {
		if (jType == JavaType.BOOLEAN)
			prep.setBoolean(i, (boolean)value);
		else if (jType == JavaType.DOUBLE)
			prep.setDouble(i, (double)value);
		else if (jType == JavaType.ENUM)
			prep.setString(i, value.toString());
		else if (jType == JavaType.FLOAT)
			prep.setFloat(i, (float)value);
		else if (jType == JavaType.INT)
			prep.setInt(i, (int)value);
		else if (jType == JavaType.LONG)
			prep.setLong(i, (long)value);
		else
			prep.setString(i, value.toString());
			
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public Integer getObjectID(String fieldName) {
		return objectIDs.get(fieldName);
	}

	public void addObjectID(String fieldName, int id) {
		this.objectIDs.put(fieldName, id);
	}

	public List<FieldDescriptor> getObjectFields() {
		return objectFields;
	}

	public void addObjectField(FieldDescriptor field) {
		this.objectFields.add(field);
	}
	

	public List<String> getObjectFieldTargets() {
		return objectFieldTargets;
	}

	public void addObjectFieldTarget(String target) {
		this.objectFieldTargets.add(target);
	}
	
	public List<String> getRepeatedObjectFieldTargets() {
		return repeatedObjectFieldTargets;
	}

	public void addRepeatedObjectFieldTarget(String target) {
		this.repeatedObjectFieldTargets.add(target);
	}	
	

	public List<FieldDescriptor> getBasicFields() {
		return basicFields;
	}

	public void addBasicField(FieldDescriptor field) {
		this.basicFields.add(field);
	}

	public List<FieldDescriptor> getRepeatedObjectFields() {
		return repeatedObjectFields;
	}

	public void addRepeatedObjectField(FieldDescriptor field) {
		this.repeatedObjectFields.add(field);
	}

	public List<FieldDescriptor> getRepeatedBasicFields() {
		return repeatedBasicFields;
	}

	public void addRepeatedBasicField(FieldDescriptor field) {
		this.repeatedBasicFields.add(field);
	}

	public String getObjectName() {
		return objectName;
	}

	private void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	private MessageOrBuilder getMessage() {
		return message;
	}

	private void setMessage(MessageOrBuilder message) {
		this.message = message;
	}




}
