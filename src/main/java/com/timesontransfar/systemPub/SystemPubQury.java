/**
 * @author 万荣伟
 */
package com.timesontransfar.systemPub;

import java.util.List;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface SystemPubQury {
	
	/**
	 * 根据部门ID获取员工
	 * @return
	 */
	public List getStaffByOrgId(String orgId);
	
	/**
	 * 取得根接点
	 *
	 */
	public List getSystemTreeRoot(String flag,String prama);
	
	/**
	 * @author 李佳慧
	 * @date 2012-1-13
	 * @param entityId 受理来源ID
	 * @return 得到受理渠道ID
	 */
	public List getAskChannel(String entityId);
	
}
