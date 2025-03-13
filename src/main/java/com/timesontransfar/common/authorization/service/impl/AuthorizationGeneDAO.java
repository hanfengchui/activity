package com.timesontransfar.common.authorization.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.service.AuthRowMapper;
import com.timesontransfar.common.authorization.service.IAuthorizationGeneDAO;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AuthorizationGeneDAO implements IAuthorizationGeneDAO {
	
	private JdbcTemplate jdbcTemplate;
	private AbstractRowMapper rowMapper;
	private String allDataPermitGeneSql; //查询所有SQL语句的SQL语句
	private String allDataPermitSql;

	public AuthorizationGeneDAO() {
		super();
	}

	public List getAllDataPermitGene(String roleId) {
		//查询所有SQL语句的SQL语句：
		//SELECT * FROM TSM_CONDITION WHERE ROLE_ID=?
		
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(5);
	 	List geneList = (List)jdbcTemplate.query(this.allDataPermitGeneSql,new Object[]{roleId},
				new RowMapperResultSetExtractor(rowMapperIn));
	 	rowMapperIn = null;
		return geneList;
	}

	/**
	 * 查询角色拥有的所有数据权限
	 * @param roleId
	 * @return
	 */
	public List getAllDataPermit(String roleId){
/*		SELECT B.DATAPERMIT_RELA_ID,A.OBJ_ID,A.OPERTYPE,B.ISPRIVATE
		FROM TSM_ROLE_DATAPERMIT_OPER A,TSM_ROLE_DATAPERMIT_RELA B
		WHERE A.DATAOPERID=B.DATAOPERID AND A.STATE=1 AND B.ROLE_ID=?
*/
		List permitList=(List)jdbcTemplate.query(this.allDataPermitSql,new Object[]{roleId},
				new RowMapperResultSetExtractor(new AllDataPermitRowMapper()));
		return permitList;
	}

	class AllDataPermitRowMapper extends AbstractRowMapper{
		public Object mapRow(ResultSet rs, int index) throws SQLException{
			TsmEntityPermit permit=new TsmEntityPermit();
			permit.setId(rs.getString("DATAPERMIT_RELA_ID"));
			permit.setObjId(rs.getString("OBJ_ID"));
			permit.setOperType(rs.getInt("OPERTYPE"));
			permit.setPrivate(rs.getBoolean("ISPRIVATE"));
			return permit;
		}
	}

	public String getAllDataPermitGeneSql() {
		return allDataPermitGeneSql;
	}

	public void setAllDataPermitGeneSql(String allDataPermitGeneSql) {
		this.allDataPermitGeneSql = allDataPermitGeneSql;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public AbstractRowMapper getRowMapper() {
		return rowMapper;
	}

	public void setRowMapper(AbstractRowMapper rowMapper) {
		this.rowMapper = rowMapper;
	}
	/**
	 * @return 返回 allDataPermitSql。
	 */
	public String getAllDataPermitSql() {
		return allDataPermitSql;
	}
	/**
	 * @param allDataPermitSql 要设置的 allDataPermitSql。
	 */
	public void setAllDataPermitSql(String allDataPermitSql) {
		this.allDataPermitSql = allDataPermitSql;
	}
}
