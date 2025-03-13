/**
 * <p>类名：CUSTOMSERVICEACCESSORIESImpl.java</p>
 * <p>功能描叙：附件查询，删除，保存实现类</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-5-26</p> 
 */
package com.timesontransfar.customservice.common.uploadFile.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.uploadFile.IAccessories;
import com.timesontransfar.customservice.common.uploadFile.dao.IAccessorieDao;
import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;

/**
 * @author 万荣伟
 *
 */
@Service
public class AccessoriesImpl implements IAccessories {
	
	@Autowired
	private IAccessorieDao AccessorieDaoImpl;
	
	@Autowired 
	private PubFunc pubFunc;
	
	/**
	 * 删除服务单的上传附件
	 * @param  ftpID 附件ftpId字符数组
	 * @return true保存文件成功，false 保存文件失败
	 */	
	public int deleteFile(String[] ftpID) {
		int size  = ftpID.length;
		int count = 0;
		for(int i = 0; i < size;i++) {
			count += AccessorieDaoImpl.deleteFile(ftpID[i]);
		}
		return count;
	}

	/**
	 * 保存服务单的上传附件
	 * @param  map里面存放FILERELATINGINFO对象
	 * @return true保存文件成功，false 保存文件失败
	 */
	public int insertFile(FileRelatingInfo[] fileInfo) {
	    String staffId = "";
        String staffName = "";
	    if(pubFunc.getSystemAuthorization().getHttpSession()!=null){
	        TsmStaff staff = this.pubFunc.getLogonStaff();
	        staffId = staff.getId();
	        staffName = staff.getName();
	    }
		
		int size = fileInfo.length;
		int count = 0;
		
		Integer month =  pubFunc.getIntMonth();
		for(int i=0;i < size;i++) {
			FileRelatingInfo fileInfos = fileInfo[i];
			//加集团附件判断
			if(fileInfos.getUpStaffId()==null || fileInfos.getUpStaffId().length()==0){
			    fileInfos.setUpStaffId(staffId);
	            fileInfos.setUpStaffName(staffName);
			}
			
			if(null == fileInfos.getMonth() ){
				fileInfos.setMonth(month);
			}
			count += AccessorieDaoImpl.saveFile(fileInfos);
		}
		return count;
	}

	public String saveFile(String ftpGuid,String orderId,String sheetId,String fileName,String regionId) {
		FileRelatingInfo fileInfo =  new FileRelatingInfo();
		fileInfo.setFileName(fileName);
		fileInfo.setFtpId(ftpGuid);
		fileInfo.setOrderId(orderId);
		fileInfo.setSheetId(sheetId);
		fileInfo.setRegionId(regionId);
		FileRelatingInfo[] ls = new FileRelatingInfo[1];
		ls[0] = fileInfo;
		int ctn = insertFile(ls);
		return ctn > 0 ? "success" : "fail";
	}

	/**
	 * 保存服务单的外呼录音记录
	 * @param FILERELATINGINFO对象
	 * @param ftpHost cc_ftp_serv_info.ftp_host
	 * @param newFileName
	 *            cc_ftp_serv_info.old_file_name,cc_ftp_serv_info.new_file_name
	 * @param ftpFileDir
	 *            cc_ftp_serv_info.ftp_file_dir
	 * @return 1保存文件成功，0保存文件失败
	 */
	public int insertCallFile(FileRelatingInfo fileInfo, String ftpHost, String newFileName, String ftpFileDir) {
		int count = 0;
		String ftpId = pubFunc.crtGuid();
		fileInfo.setFtpId(ftpId);
		if (AccessorieDaoImpl.saveCallFile(fileInfo) == 1) {
			count = AccessorieDaoImpl.saveCallFtp(ftpId, ftpHost, newFileName, ftpFileDir);
		}
		return count;
	}
	
	/**
	 * 查询该服务单的所有附件量
	 * @param orderId	服务单号
	 * @return 返回上传文件数量
	 */	
	public int queryFileCount(String orderId) {
		return AccessorieDaoImpl.quryFileCount(orderId);
	}

	/**
	 * 查询该服务单的所有附件
	 * @param FileRelatingInfo 上传文件对象
	 * @param orderId	服务单号
	 * @return 返回上传文件对象数组
	 */	
	public FileRelatingInfo[] queryFile(String orderId) {
		FileRelatingInfo[] relating = AccessorieDaoImpl.quryFileInfo(orderId);
		return relating;
	}

}
