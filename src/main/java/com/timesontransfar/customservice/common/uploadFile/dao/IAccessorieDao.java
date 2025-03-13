/**
 * <p>类名：ICUSTOMSERVICEACCESSORIES.java</p>
 * <p>功能描叙：受理单附件的DAO</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-5-26</p> 
 */
package com.timesontransfar.customservice.common.uploadFile.dao;

import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;

public interface IAccessorieDao {

	/**
	 * 保存服务单的附件的基本信息
	 * 
	 * @param fileinfo
	 *            附件信息
	 * @return 保存的记录数
	 */
	public int saveFile(FileRelatingInfo fileinfo);

	/**
	 * 保存服务单的外呼录音记录
	 * 
	 * @param FileRelatingInfo
	 *            上传外呼录音记录
	 * @return 1保存文件成功，0保存文件失败
	 */
	public int saveCallFile(FileRelatingInfo fileinfo);

	/**
	 * 保存服务单的外呼录音记录
	 * 
	 * @param ftpId
	 *            cc_ftp_serv_info.ftp_guid
	 * @param ftpHost
	 *            cc_ftp_serv_info.ftp_host
	 * @param newFileName
	 *            cc_ftp_serv_info.old_file_name,cc_ftp_serv_info.new_file_name
	 * @param ftpFileDir
	 *            cc_ftp_serv_info.ftp_file_dir
	 * @return 1保存文件成功，0保存文件失败
	 */
	public int saveCallFtp(String ftpId, String ftpHost, String newFileName, String ftpFileDir);

	/**
	 * 删除服务单的附件记录
	 * 
	 * @param ftpId
	 *            服务单号
	 * @return true 删除成功 false 删除失败
	 */
	public int deleteFile(String ftpId);

	/**
	 * 查询服务单的附件数
	 * 
	 * @param orderId
	 *            服务单号
	 * @return 返回count
	 */
	public int quryFileCount(String orderId);

	/**
	 * 查询服务单的附件记录
	 * 
	 * @param orderId
	 *            服务单号
	 * @return 返回FileRelatingInfo
	 */
	public FileRelatingInfo[] quryFileInfo(String orderId);

	/**
	 * 查询服务单的本地附件记录，不包括集团来的附件
	 * 
	 * @param orderId 服务单号
	 * @return 返回FileRelatingInfo
	 */
	public FileRelatingInfo[] quryFileInfoNotInJT(String orderId);

	/**
	 * 根据受理单号、原地域ID，更新记录的地域信息
	 * 
	 * @param serviceOrderId
	 *            受理单号
	 * @param newRegionId
	 *            新地域ID
	 * @param oldRegionId
	 *            旧地域ID
	 * @return
	 */
	public int updateRegion(String serviceOrderId, int newRegionId, int oldRegionId);
}