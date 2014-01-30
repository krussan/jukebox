package se.qxx.protodb;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.RepeatedFieldBuilder;

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

	@SuppressWarnings("unchecked")
	private void scan(MessageOrBuilder b) {
			
		this.setObjectName(StringUtils.capitalize(b.getDescriptorForType().getName()));

		List<FieldDescriptor> fields = b.getDescriptorForType().getFields();
		for(FieldDescriptor field : fields) {
//			Object o = b.getField(field);
//			ProtoDBScanner dbInternal = null;
			JavaType jType = field.getJavaType();
			
			if (field.isRepeated())
			{
				
//				java.lang.reflect.Field = (RepeatedMessage) this.getMessage().getField(field)
//				ParameterizedType targetType = (ParameterizedType) target.get
				Descriptor desc = field.getContainingType();
				Class<?> cls;
				Object obj;
				
				try {
					String clazzName = desc.getFullName();
					cls = Class.forName(clazzName);
					obj = cls.newInstance();
				
					if (obj instanceof GeneratedMessage) {
							MessageOrBuilder target = (MessageOrBuilder)this.getMessage().getField(field);
							ProtoDBScanner dbInternal = new ProtoDBScanner(target);			
						
							this.addRepeatedObjectField(field);		
							this.addRepeatedObjectFieldTarget(dbInternal.getObjectName());
					}
					else {
						this.addRepeatedBasicField(field);
					}	
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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

	protected String getBasicFieldName(FieldDescriptor field) {
		return field.getName().toLowerCase();
	}

	protected String getObjectFieldName(FieldDescriptor field) {
		return "_" + getBasicFieldName(field) + "_ID";
	}

	public String getInsertStatement() {
		
		String[] params = new String[this.getFieldNames().size()];
		Arrays.fill(params, "?");
		
		String sql = String.format(
				"INSERT INTO %s (%s) VALUES (%s)",
				this.getObjectName(),
				StringUtils.join(this.getFieldNames(), ","));
		
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
					"INT", 
					field.isOptional() ? "NULL" : "NOT NULL",
					target));
		}
		
		for(FieldDescriptor field : this.getBasicFields()) {
			cols.add(String.format("[%s] %s %s", 
					getBasicFieldName(field), 
					getDBType(field), 
					field.isOptional() ? "NULL" : "NOT NULL"));
		}
		
		
		sql += "(ID int PRIMARY KEY NOT NULL, " + StringUtils.join(cols, ",") + ")";
		
		return sql;
	}
	
	public String getLinkCreateStatement(MessageOrBuilder other) {
		ProtoDBScanner scanner = new ProtoDBScanner(other);
		return String.format("CREATE TABLE %s%s ("
				+ "_" + this.getObjectName().toLowerCase() + "_ID INT NOT NULL REFERENCES %s (ID),"
				+ "_" + scanner.getObjectName().toLowerCase() + "_ID INT NOT NULL REFERENCES %s (ID)"
				+ ")", 
				this.getObjectName(), 
				scanner.getObjectName(),
				this.getObjectName(),
				scanner.getObjectName());
		
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
			type = "INT";
		else if (jType == JavaType.LONG)
			type = "BIGINT";
		else
			type ="TEXT";
		
		return type;
	}

	public PreparedStatement compileArguments(MessageOrBuilder b, String sql, Connection conn) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(sql);

		int c = 0;
		for(FieldDescriptor field : this.getObjectFields()) {
			int id = this.getObjectID(field.getName());
			prep.setInt(++c, id);
		}
		
		for(FieldDescriptor field : this.getBasicFields())
			this.compileArgument(++c, prep, field, b.getField(field));			
		
		return prep;
	}

	private void compileArgument(int i, PreparedStatement prep, FieldDescriptor field, Object value) throws SQLException {
		JavaType jType = field.getJavaType();
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
