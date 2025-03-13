package com.timesontransfar.systemPub;



public interface ISystemPubService {
	/**
	 * 查询组织机构树结构
	 * Description: <br> 
	 *  
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年6月4日 下午2:26:22 <br>
	 */
	public String getSysttemOrgTree(String flag,String parm);
	/**
	 * 层级关系之后得组织机构
	 * Description: <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年7月6日 上午11:16:34 <br>
	 */
	public String getOrgTreeLevel();
	/**
	 * Description: 查询当前部门得员工信息<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param orgId
	 * @return <br>
	 * @CreateDate 2020年7月6日 下午3:09:08 <br>
	 */
	public String getStaffByOrganId(int orgId,String staffName,String loginName,String sheetId);
	/**
	 * Description: 获得对应员工的受理渠道展现树 <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年8月22日 上午11:32:28 <br>
	 */
	public String channlAskInfoTree();
	
	public String systemOrgTreeNew(String sheetId);
	/**
	 * Description: 办结原因树<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年8月25日 上午9:14:23 <br>
	 */
	public String reaSonTree();
	
	public String getPubColomndefaultTree(int sixGread);

	/**
	 * 辅助工具 菜单列表
	 * @return
	 */
	public String auxiliaryToolMuen();

	/**
	 * 通过 收单部门id 遍历二级菜单及下级节点
	 * @param orgId
	 * @return
	 */
	public String specificInfoService(String orgId);

	/**
	 * 判断责任部门 是否显示对应得具体部门
	 * @param referid
	 * @param revOrgId
	 * @return
	 */
	public boolean zerenOrgFlag(String referid,String revOrgId);
	/**
	 *  下发用户密码修改 验证码
	 * Description: <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param loginName
	 * @return <br>
	 * @CreateDate 2020年11月24日 下午4:47:56 <br>
	 */
	public String sendRestUserCaptcha(String loginName,int captchaType);
	/**
	 * Description: 验证短信验证码是否正常<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param guid
	 * @param loginName
	 * @param cahtcha
	 * @return <br>
	 * @CreateDate 2020年11月24日 下午6:17:57 <br>
	 */
	public String validationUserCaptcha(String guid,String loginName,String cahtcha);
	/**
	 * Description: 生成业务类型四级目录树<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年12月16日 下午5:49:57 <br>
	 */
	public String createFourMuLuTree();
	
	public String loadColumnsByEntity(String table,String colCode,String entity);

	public String getColumnsByCode(String table,String colCode,String entity);

	public String skillOrgTree(String flag,String parm);
	
	/**
	 * 带权限查询组织机构树
	 * @param flag
	 * @param parm
	 * @return
	 */
	public String getSysttemOrgTreeAuth(String flag,String parm,String staffId,String param);
	
	/**
	 * 获取协查单全渠道转派树根节点
	 * @param sheetId
	 * @return
	 */
	public String getXcDispatchOrg(String sheetId);
	
	public String loadColumnsByEntityNew(String table,String colCode,String entity);

	public int addColumnsReference( String newColumnData);

	public int updateColumnsReference(String newColumnData);

	public int delColumnsReference(String referId);

	public String getColumnsByCodeBuop(String table,String colCode,String entity);
	
}
