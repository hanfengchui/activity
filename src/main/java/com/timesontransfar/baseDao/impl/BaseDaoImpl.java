package com.timesontransfar.baseDao.impl;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.timesontransfar.baseDao.IBaseDao;
import com.timesontransfar.baseDao.util.EmptyUtil;

/**
 * <p>Class Name: baseDao</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-12</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
@Component(value="baseDao")
public class BaseDaoImpl implements IBaseDao {
	protected Logger logger = LoggerFactory.getLogger(BaseDaoImpl.class);
	
    @Resource
    private JdbcTemplate jdbcTemplate;//jdbc模版
    
    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;//命名对象的模版
   
	@Override
	public <T> T queryForObject(String sql, T t, Class<T> returnType) {		
        EmptyUtil.isEmptyException(sql, "sql为空！");
        EmptyUtil.isEmptyException(t, "参数对象为空!");
        logger.info("查询sql："+sql);
        T f = null;//返回的对象
        List<T> list = null;
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        list = this.namedParameterJdbcTemplate.query(sql, ps, new BeanPropertyRowMapper<T>(returnType));
        if (CollectionUtils.isNotEmpty(list)) {
            f = list.get(0);
            list.clear();
            list = null;            
        }       
		return f;
	}
	
	@Override
	public <T> T queryForObject(String sql, Class<T> requiredType) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        logger.info("查询sql："+sql);
        T f = null;//返回的对象
        List<T> list = null;       
        list = this.jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(requiredType));
        if (CollectionUtils.isNotEmpty(list)) {
            f = list.get(0);
            list.clear();
            list = null;
        }       
		return f;
	}
	
	@Override
	public <T> T queryForObject(String sql, Map<String, String> map,Class<T> returnType) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        EmptyUtil.isEmptyException(map, "参数对象为空!");
        logger.info("查询sql："+sql);
        T f = null;//返回的对象
        List<T> list = null;
        SqlParameterSource ps = new MapSqlParameterSource(map);
        list = this.namedParameterJdbcTemplate.query(sql, ps, new BeanPropertyRowMapper<T>(returnType));
        if (CollectionUtils.isNotEmpty(list)) {
            f = list.get(0);
            list.clear();
            list = null;            
        }       
		return f;
	}
	
	@Override
	public <T> List<T> query(String sql, Object[] args, Class<T> requestType) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        List<T> list = null;
        if (ArrayUtils.isNotEmpty(args)) {
            list = this.jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<T>(requestType));
        } else {
            list = this.jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(requestType));
        }
        return list;
    }


	@Override
	public <T> List<T> query(String sql, T t, Class<T> requestType) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        EmptyUtil.isEmptyException(t, "参数对象为空!");
        logger.info("查询sql："+sql);
        List<T> list = null;
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        list = this.namedParameterJdbcTemplate.query(sql, ps, new BeanPropertyRowMapper<T>(requestType));
		return list;
	}
	
	@Override
	public <T> List<T> query(String sql, Map<String, String> map,Class<T> requestType) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        EmptyUtil.isEmptyException(map, "参数对象为空!");
        logger.info("查询sql："+sql);
        List<T> list = null;
        SqlParameterSource ps = new MapSqlParameterSource(map);
        list = this.namedParameterJdbcTemplate.query(sql, ps, new BeanPropertyRowMapper<T>(requestType));
		return list;
	}
	
	@Override
	public String queryForString(String sql, Object[] args, String fieldName) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        String result = "";
        List<Map<String, Object>> list = null;
        if (ArrayUtils.isNotEmpty(args)) {
            list = this.jdbcTemplate.queryForList(sql, args);
        } else {
            list = this.jdbcTemplate.queryForList(sql);
        }
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, Object> map = list.get(0);
            if (map.containsKey(fieldName)) {
                Object obj = map.get(fieldName);
                if (obj != null) {
                    result = obj.toString();
                }
            }
            //清空对象，释放内存
            map.clear();
            map=null;
            list.clear();
            list = null;
        }
        return result;
    }
	
	@Override
	public <T> int queryForInt(String sql, T t) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        EmptyUtil.isEmptyException(t, "参数对象为空!");		
		int result = 0;
		SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
		//result = this.namedParameterJdbcTemplate.queryForInt(sql, ps);
		result = this.jdbcTemplate.queryForObject(sql,Integer.class, ps);
		return result;
	}


	@Override
	public <T> List<Map<String, Object>> queryForList(String sql, T t) {
        EmptyUtil.isEmptyException(sql, "sql为空！");	
        List<Map<String, Object>> list = null;
		SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
		if(EmptyUtil.isObjEmpty(t)) {
			list = this.namedParameterJdbcTemplate.queryForList(sql, ps);
		} else {
			list = this.jdbcTemplate.queryForList(sql);
		}	
		if (CollectionUtils.isNotEmpty(list)) {
			logger.info("查询结果数："+list.size());
		} else {
			logger.info("查询结果数：0");
		}
		return list;
	}
	
	@Override
	public <T> int saveData(String sql, T t) {
        EmptyUtil.isEmptyException(sql, "sql为空！");
        EmptyUtil.isEmptyException(t, "更新的对象是空值!");
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        int result = namedParameterJdbcTemplate.update(sql, ps);
        return result;
    }

	/**
	 * @return jdbcTemplate
	 * sunli
	 */
	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	/**
	 * @param jdbcTemplate 要设置的 jdbcTemplate
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * @return namedParameterJdbcTemplate
	 * sunli
	 */
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	/**
	 * @param namedParameterJdbcTemplate 要设置的 namedParameterJdbcTemplate
	 */
	public void setNamedParameterJdbcTemplate(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
}