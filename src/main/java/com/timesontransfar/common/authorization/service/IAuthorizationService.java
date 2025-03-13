package com.timesontransfar.common.authorization.service;

import java.util.List;
import java.util.Map;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.exception.BusinessException;

@SuppressWarnings("rawtypes")
public interface IAuthorizationService {

    /***
     * 取员工基本信息
     * @param staffId
     * @return
     */
	public TsmStaff getStaff(String staffId);

	public List getStaffRoleList(Map inParams);

	/**
	 * 保存员工
	 * @param inParams
	 * @return
	 * @throws BusinessException
	 */
	public String saveStaff(Map inParams) throws BusinessException;

	public String saveStaffRole(String staffId,List lstRole) throws BusinessException;

}
