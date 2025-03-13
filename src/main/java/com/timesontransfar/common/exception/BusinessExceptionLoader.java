package com.timesontransfar.common.exception;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.exceptions.CacheException;
@SuppressWarnings("rawtypes")
public class BusinessExceptionLoader implements InitializingBean {
	private ICache cache;
	private String allExceptionSql;
	private JdbcTemplate jdbcTemplate;

	public BusinessExceptionLoader() {
		super();
		// Auto-generated constructor stub
	}

	public void afterPropertiesSet() throws Exception {
		// Auto-generated method stub
		this.loadAllException();
	}

	public ICache getCache() {
		return cache;
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}

	public String getAllExceptionSql() {
		return allExceptionSql;
	}

	public void setAllExceptionSql(String allExceptionSql) {
		this.allExceptionSql = allExceptionSql;
	}

	private void loadAllException(){
/*		查询SQL:
		SELECT * FROM PUB_EXCEPTION
		EXCEP_ID VARCHAR2(32)   唯一ID
		EXCEP_NAME VARCHAR2(32) 中文名称
		EXCEP_TYPE VARCHAR2(32) 类型
		SYSTEM NUMERIC(1)  是否系统异常
		DESCRIPTION VARCHAR2(100) 描述
		POSSIBLECAUSE VARCHAR2(100)可能原因
*/
		List queryList=this.jdbcTemplate.queryForList(this.allExceptionSql);
		try{
			for(int i=0;i<queryList.size();i++){
				Map queryMap = (Map)queryList.get(i);
				BusinessExceptionObject object = new BusinessExceptionObject();
				String id=(String)queryMap.get("EXCEP_ID");
				object.setId(id);
				object.setName((String)queryMap.get("EXCEP_NAME"));
				object.setType((String)queryMap.get("EXCEP_TYPE"));
				object.setSystem(((Boolean)queryMap.get("SYSTEM")).booleanValue());
				object.setDescription((String)queryMap.get("DESCRIPTION"));
				object.setPossibleCause((String)queryMap.get("POSSIBLECAUSE"));
				synchronized(this.cache){
					this.cache.put(id,object);
				}
			}
		}catch(CacheException e){
		//异常处理逻辑
		}
	}

}
