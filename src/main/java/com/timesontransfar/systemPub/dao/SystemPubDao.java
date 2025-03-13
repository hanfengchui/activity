package com.timesontransfar.systemPub.dao;

import java.util.List;
import java.util.Map;

import com.timesontransfar.systemPub.entity.CapchaInfo;
import com.timesontransfar.systemPub.entity.PubColumn;

import net.sf.json.JSONArray;

@SuppressWarnings("rawtypes")
public interface SystemPubDao {
	/**
	 * 查询树行清单
	 * Description: <br> 
	 *  
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年6月4日 下午2:36:11 <br>
	 */
	public List queryOrgInfo(String flag,String parm);
	/**
	 * Description: 根据部门id 查询对应部门得员工信息<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param orgId
	 * @return <br>
	 * @CreateDate 2020年7月6日 下午3:10:41 <br>
	 */
	public String queryStaffByOrgId(int orgId,String staffName,String logName,String flag,String parm);
	
	public List queryAskInfoNew(List list);
	
	public List<PubColumn> queryReasonInfo();
	
	public List<PubColumn> queryPubColumnNew(List<PubColumn> list, String referId);

	public List auxiliaryToolMuen(String logName);

	public List specificOrgInfo(String orgId);
	/**
	 * 
	 * Description: <br> 
	 *  
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param type
	 * @return <br>
	 * @CreateDate 2020年10月15日 下午4:29:16 <br>
	 */
	public JSONArray queryOrgTypeInfo(int type);
	/**
	 * Description: 执行验证码数据保存<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param capchaInfo
	 * @return <br>
	 * @CreateDate 2020年11月24日 下午5:47:08 <br>
	 */
	public int saveCaptchaInfo(CapchaInfo capchaInfo);
	
	public CapchaInfo captChaByGuid(String guid);
	/**
	 * Description: 查询四级目录所有层级数据<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年12月16日 下午6:00:01 <br>
	 */
	public List<PubColumn> queryFourSixMulu();
	
	public List<PubColumn> loadColumnsByEntity(String table,String colCode,String entity);

	public List<PubColumn> getAllColumnsNew(String table, String colValueHandling);

	public List skillOrgTreelist(String strWhere);
	
	/**
	 * 带权限实体查询树行清单
	 * Description: <br> 
	 *  
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年6月4日 下午2:36:11 <br>
	 */
	public List queryOrgInfoAuth(String flag,String parm,String staffId,String param);
	
	public List<PubColumn> loadColumnsByEntityNew(String table,String colCode,String entity);

	public int addColumnsReference(Map<String, String> newColumnData);

	public int updateColumnsReference(Map<String, String> updateColumnData);

	public int delColumnsReference(String referId);

	public List<PubColumn> getAllColumnsNew(String table);
	
}
