/**
 * <p>类名：ICUSTOMSERVICEACCESSORIES.java</p>
 * <p>功能描叙：受理单附件的保存，删除，查询</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-5-26</p> 
 */
package com.timesontransfar.customservice.common.uploadFile;

import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;

/**
 * @author 万荣伟
 * 
 */
public interface IAccessories {
	/**
	 * 查询该服务单的所有附件
	 * 
	 * @param orderId
	 *            服务单号
	 * @return 返回上传文件数量
	 */
	public int queryFileCount(String orderId);

	/**
	 * 查询该服务单的所有附件
	 * 
	 * @param FileRelatingInfo
	 *            上传文件对象
	 * @param orderId
	 *            服务单号
	 * @return 返回上传文件对象数组
	 */
	public FileRelatingInfo[] queryFile(String orderId);

	/**
	 * 保存服务单的上传附件
	 * 
	 * @param map里面存放FILERELATINGINFO对象
	 * @return true保存文件成功，false 保存文件失败
	 */
	public int insertFile(FileRelatingInfo[] fileInfo);

	/**
	 * 保存单个附件
	 * @param ftpGuid
	 * @param orderId
	 * @param sheetId
	 * @param fileName
	 * @return
	 */
	public String saveFile(String ftpGuid,String orderId,String sheetId,String fileName,String regionId);
	/**
	 * 保存服务单的外呼录音记录
	 * 
	 * @param FILERELATINGINFO对象
	 * @param ftpHost
	 *            cc_ftp_serv_info.ftp_host
	 * @param newFileName
	 *            cc_ftp_serv_info.old_file_name,cc_ftp_serv_info.new_file_name
	 * @param ftpFileDir
	 *            cc_ftp_serv_info.ftp_file_dir
	 * @return 1保存文件成功，0保存文件失败
	 */
	public int insertCallFile(FileRelatingInfo fileInfo, String ftpHost, String newFileName, String ftpFileDir);

	/**
	 * 删除服务单的上传附件
	 * 
	 * @param ftpID
	 *            附件ftpId字符数组
	 * @return true保存文件成功，false 保存文件失败
	 */
	public int deleteFile(String[] ftpID);
}