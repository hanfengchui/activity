package com.timesontransfar.baseDao;
/**
 * Copyright @ 2013 transfar
 * All right reserved
 */
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>Class Name: IPubDao</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-12</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public interface IBaseDao {
	/**
	 * 
	 * 描述: 带参数对象根据查询sql，返回相对应javaben对象
	 * @param sql 查询sql
	 * @param t 条件参数bean，比如 where user_id = :userId;其中userId为t对象的属性
	 * @param returnType 需要返回javaben 对象，传入的对象的属性跟查询sql返回列一样
	 * @return
	 * @author     sunli
	 * date        2013-12-12
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-12       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> T queryForObject(String sql, T t,Class<T> returnType);
	/**
	 * 
	 * 描述: 以map方式进行查询，其中map的key等于查询的参数 where user_id= :userId,map的key为userId
	 * @param sql
	 * @param map
	 * @param returnType
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> T queryForObject(String sql, Map<String,String> map, Class<T> returnType);	
	/**
	 * 
	 * 描述: 不带参数对象查询sql，返回相对应javaben对象
	 * @param sql
	 * @param requiredType 需要返回javaben 对象，传入的对象的属性跟查询sql返回列一样
	 * @return
	 * @author     sunli
	 * date        2013-12-12
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-12       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> T queryForObject(String sql, Class<T> requiredType);
	
	/**
	 * 
	 * 描述: 返回list列表，list带javabean对象，javabean对象的属性等于查询sql列
	 * @deprecated
	 * @param sql
	 * @param args 允许为空，
	 * @param requestType
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	@Deprecated
	<T> List<T> query(String sql, Object[] args, Class<T> requestType);
	/**
	 * 
	 * 描述: 返回list列表，list带javabean对象,查询参数为bean对象
	 * @param sql
	 * @param t
	 * @param requestType
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> List<T> query(String sql,T t, Class<T> requestType);

	/**
	 * 
	 * 描述: 查询参数以map方式传入
	 * @param sql
	 * @param map
	 * @param requestType
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> List<T> query(String sql,Map<String,String> map, Class<T> requestType);
	/**
	 * 
	 * 描述: 返回字符串
	 * @param sql
	 * @param args
	 * @param fieldName
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	String queryForString(String sql, Object[] args, String fieldName);

	/**
	 * 
	 * 描述: 查询记录数,参数以javabean对象带入
	 * @param sql
	 * @param t
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> int queryForInt(String sql,T t);
	/**
	 * 
	 * 描述: 返回list对象，包含map
	 * @param sql
	 * @param t
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> List<Map<String,Object>> queryForList(String sql,T t);

	/**
	 * 
	 * 描述: 以javabean对象进行保存insert table (user_id) value(:userId)
	 * @param sql
	 * @param t
	 * @return
	 * @author     sunli
	 * date        2013-12-13
	 * --------------------------------------------------
	 * 修改人    	      修改日期       修改描述
	 * sunli        2013-12-13       创建
	 * --------------------------------------------------
	 * @Version  Ver1.0
	 */
	<T> int saveData(String sql,T t);
	
	JdbcTemplate getJdbcTemplate();
}
