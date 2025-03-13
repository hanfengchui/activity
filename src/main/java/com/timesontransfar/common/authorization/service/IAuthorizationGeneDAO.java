/**
 *
 */
package com.timesontransfar.common.authorization.service;

import java.util.List;

/**
 * 用于操作数据的权限因子数据库
 * @author 罗翔 创建于2005-12-21
 *
 */
@SuppressWarnings("rawtypes")
public interface IAuthorizationGeneDAO {
	public List getAllDataPermitGene(String roleId);

	public List getAllDataPermit(String roleId);

}
