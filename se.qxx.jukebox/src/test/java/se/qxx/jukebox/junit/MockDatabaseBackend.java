package se.qxx.jukebox.junit;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.protodb.DBType;
import se.qxx.protodb.backend.DatabaseBackend;

public class MockDatabaseBackend extends DatabaseBackend {

	public MockDatabaseBackend() {
		super(StringUtils.EMPTY, StringUtils.EMPTY);
	}
	
	public MockDatabaseBackend(String driver, String connectionString) {
		super(driver, connectionString);
	}

	@Override
	public String getIdentityDefinition() {
		return "ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY";
	}

	@Override
	public int getIdentityValue(Connection conn) throws SQLException {
		return 1;
	}

	@Override
	public DBType getDBType() {
		return DBType.Unsupported;
	}
	
	@Override
	public String getStartBracket() {
		return "";
	}
	
	@Override
	public String getEndBracket() {
		return "";
	}


}
