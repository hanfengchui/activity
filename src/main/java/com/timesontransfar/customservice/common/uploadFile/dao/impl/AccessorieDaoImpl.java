/**
 * <p>类名：CUSTOMSERVICEACCESSORIESDAOImpl.java</p>
 * <p>功能描叙：附件DAO实现类</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-5-26</p> 
 */
package com.timesontransfar.customservice.common.uploadFile.dao.impl;

import java.util.List;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.timesontransfar.customservice.common.uploadFile.dao.IAccessorieDao;
import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;
import com.timesontransfar.customservice.common.uploadFile.pojo.FilerelTiongRmp;

/**
 * @author 万荣伟
 */
@SuppressWarnings("rawtypes")
public class AccessorieDaoImpl implements IAccessorieDao {
	protected Logger log = LoggerFactory.getLogger(AccessorieDaoImpl.class);
	
	@Resource
    private JdbcTemplate jdbcTemplate;

    private String saveFileSql;

	private String saveCallFileSql;

    private String deleteFileSql;

    private String quryFileSql;

	private String quryFileNotInJTSql;

    private String quryFileCountSql;

	private String updateRegionSql;

    /**
     * 删除服务单的附件记录
     * @param ftpId 服务单号
     * @return true 删除成功 false 删除失败
     */
    public int deleteFile(String ftpId) {
        return jdbcTemplate.update(this.deleteFileSql, ftpId);
        
    }

    /**
     * 保存服务单的附件的基本信息
     * 
     * @param FileRelatingInfo
     *            上传文件对象
     * @param orderId
     *            服务单号
     * @return 返回上传文件对象数组
     */
    public int saveFile(FileRelatingInfo fileinfo) {
    	return jdbcTemplate.update(
                this.saveFileSql,
                fileinfo.getFtpId(), fileinfo.getOrderId(),
                        fileinfo.getRegionId(), StringUtils.defaultIfEmpty(fileinfo.getSheetId(),null),
                        fileinfo.getFileName(), fileinfo.getUpStaffId(),
                        fileinfo.getUpStaffName(), fileinfo.getMonth());
    }

	/**
	 * 保存服务单的外呼录音记录
	 * 
	 * @param FileRelatingInfo
	 *            上传外呼录音记录
	 * @return 1保存文件成功，0保存文件失败
	 */
	public int saveCallFile(FileRelatingInfo fileinfo) {
		return jdbcTemplate.update(this.saveCallFileSql, fileinfo.getFtpId(), fileinfo.getOrderId(),
				StringUtils.defaultIfEmpty(fileinfo.getSheetId(),null), fileinfo.getFileName(),
				fileinfo.getUpStaffId(), fileinfo.getUpStaffName(), fileinfo.getFtpDate());
	}

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
	public int saveCallFtp(String ftpId, String ftpHost, String newFileName, String ftpFileDir) {
		String sql = "INSERT INTO CC_FTP_SERV_INFO VALUES (?, ?, ?, ?, ?, 0, NOW())";
		return jdbcTemplate.update(sql, ftpId, ftpHost, newFileName, newFileName, ftpFileDir);
	}

	/**
	 * 查询服务单的附件数
	 * 
	 * @param orderId
	 *            服务单号
	 * @return 返回count
	 */
	public int quryFileCount(String orderId) {
		
		return this.jdbcTemplate.queryForObject(quryFileCountSql,new java.lang.Object[]{ orderId },Integer.class);
	}

    /**
     * 查询服务单的附件记录
     * 
     * @param orderId
     *            服务单号
     * @return 返回FileRelatingInfo
     */
    @SuppressWarnings("unchecked")
	public FileRelatingInfo[] quryFileInfo(String orderId) {
//        String strSql = this.quryFileSql;
		String strSql = "SELECT A.FTP_GUID,A.SERVICE_ORDER_ID,A.REGION_ID,A.WORK_SHEET_ID,A.OLD_FILE_NAME,A.UPLOAD_STAFF_ID,\n" +
				"DATE_FORMAT( A.FTP_DATE, '%Y-%m-%d %H:%i:%s' ) AS FTP_DATE,A.MONTH_FLAG,\n" +
				"(SELECT STAFFNAME FROM tsm_staff WHERE LOGONNAME = B.LOGIN_NAME ) AS LOGIN_NAME,\n" +
				"DATE_FORMAT( B.SHORT_LINK_TIME, '%Y-%m-%d %H:%i:%s' ) AS SHORT_LINK_TIME,B.PROD_NUM,\n" +
				"CASE WHEN C.USER_NAME IS NOT NULL AND C.USER_NAME != '' THEN C.USER_NAME ELSE A.UPLOAD_STAFF_NAME \n" +
				"END AS UPLOAD_STAFF_NAME FROM CC_FILE_RELATING A LEFT JOIN CC_UPLOAD_RECORD_FILE C ON A.FTP_GUID = C.FTP_GUID\n" +
				"LEFT JOIN CC_UPLOAD_RECORD_INFO B ON C.REQUEST_ID = B.REQUEST_ID WHERE A.SERVICE_ORDER_ID = ? AND A.REGION_ID NOT IN (92448)";
        List list = this.jdbcTemplate.query(strSql, new Object[] {orderId}, new FilerelTiongRmp());
        int size = list.size();
        if (size == 0) {
            return new FileRelatingInfo[0];
        }

        FileRelatingInfo[] fileInfo = new FileRelatingInfo[size];
        for (int i = 0; i < size; i++) {
            fileInfo[i] = (FileRelatingInfo) list.get(i);
        }
        list.clear();
        list = null;
        return fileInfo;
    }

	/**
	 * 查询服务单的本地附件记录，不包括集团来的附件
	 * 
	 * @param orderId 服务单号
	 * @return 返回FileRelatingInfo
	 */
	@SuppressWarnings("unchecked")
	public FileRelatingInfo[] quryFileInfoNotInJT(String orderId) {
		String strSql = this.quryFileNotInJTSql;
		List list = this.jdbcTemplate.query(strSql, new Object[] { orderId, orderId }, new FilerelTiongRmp());
		int size = list.size();
		if (size == 0) {
			return new FileRelatingInfo[0];
		}
		FileRelatingInfo[] fileInfo = new FileRelatingInfo[size];
		for (int i = 0; i < size; i++) {
			fileInfo[i] = (FileRelatingInfo) list.get(i);
		}
		list.clear();
		list = null;
		return fileInfo;
	}

	public int updateRegion(String serviceOrderId, int newRegionId, int oldRegionId){
		return jdbcTemplate.update(updateRegionSql, newRegionId, serviceOrderId, oldRegionId);
	}

    /**
     * @return deleteFileSql
     */
    public String getDeleteFileSql() {
        return deleteFileSql;
    }

    /**
     * @param deleteFileSql
     *            要设置的 deleteFileSql
     */
    public void setDeleteFileSql(String deleteFileSql) {
        this.deleteFileSql = deleteFileSql;
    }

    /**
     * @return saveFileSql
     */
    public String getSaveFileSql() {
        return saveFileSql;
    }

    /**
     * @param saveFileSql
     *            要设置的 saveFileSql
     */
    public void setSaveFileSql(String saveFileSql) {
        this.saveFileSql = saveFileSql;
    }

    public String getSaveCallFileSql() {
		return saveCallFileSql;
	}

	public void setSaveCallFileSql(String saveCallFileSql) {
		this.saveCallFileSql = saveCallFileSql;
	}

    /**
     * @return quryFileSql
     */
    public String getQuryFileSql() {
        return quryFileSql;
    }

    /**
     * @param quryFileSql
     *            要设置的 quryFileSql
     */
    public void setQuryFileSql(String quryFileSql) {
        this.quryFileSql = quryFileSql;
    }

	public String getQuryFileNotInJTSql() {
		return quryFileNotInJTSql;
	}

	public void setQuryFileNotInJTSql(String quryFileNotInJTSql) {
		this.quryFileNotInJTSql = quryFileNotInJTSql;
	}

	public String getQuryFileCountSql() {
		return quryFileCountSql;
	}

	public void setQuryFileCountSql(String quryFileCountSql) {
		this.quryFileCountSql = quryFileCountSql;
	}

	public void setUpdateRegionSql(String updateRegionSql) {
		this.updateRegionSql = updateRegionSql;
	}

}