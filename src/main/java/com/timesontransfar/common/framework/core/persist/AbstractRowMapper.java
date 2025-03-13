/**
 * 
 */
package com.timesontransfar.common.framework.core.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author ationr
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractRowMapper implements RowMapper {
	protected String classType;
	protected int intClassType;
	protected String configFile;
	protected String configMethod;

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public abstract Object mapRow(ResultSet arg0, int arg1) throws SQLException;

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getConfigMethod() {
		return configMethod;
	}

	public void setConfigMethod(String configMethod) {
		this.configMethod = configMethod;
	}

	public int getIntClassType() {
		return intClassType;
	}

	public void setIntClassType(int intClassType) {
		this.intClassType = intClassType;
	}

}
