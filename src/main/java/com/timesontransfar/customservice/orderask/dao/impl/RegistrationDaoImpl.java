package com.timesontransfar.customservice.orderask.dao.impl;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.orderask.dao.RegistrationDao;
import com.timesontransfar.customservice.orderask.pojo.*;
import com.transfar.common.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RegistrationDaoImpl implements RegistrationDao {

    private static final Logger log = LoggerFactory.getLogger(RegistrationDaoImpl.class);

    @Autowired
    private JdbcTemplate jt;

	@Autowired
	private IdbgridDataPub dbgridDataPub;

    @Override
	public int createRegistrationExcel(HarassmentScene harassmentScene) {
		String sql = "";
		String registrationType = harassmentScene.getRegistrationType();
		if("1".equals(registrationType)){
			sql = "INSERT INTO cc_harassment_scene(ID, SUBMISSION_TIME ,DOWNLOAD_TIME, DOWNLOAD_STATUS, REGISTRATION_TYPE, REPORT_DATE, COMPLAINT_TIME, "
					+ "CALL_TIME, CONTACT_NUMBER, REPORTER, REPORT_STAFF,COMPLAINT_NUMBER, COMPLAINED_NUMBER, COMPLAINT_CATEGORY, TEAM, TEAM_ID,COMPLAINT_NUMBER_OPERATOR, COMPLAINED_NUMBER_PROVINCE, "
					+ "SERVICE_ORDER_ID, COMPLAINT_NUMBER_PROVINCE, COMPLAINED_NUMBER_CITY, COMPLAINT_DESCRIPTION, COMPLAINT_NUMBER_LOCATION, OPERATOR,FILE_NAME,GROUP_ID) VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?,?,?,?)";
			try {
				return jt.update(sql, IdUtils.fastSimpleUUID(), harassmentScene.getSubmissionTime(), StringUtils.defaultIfBlank(harassmentScene.getDownloadTime(), null),harassmentScene.getDownloadStatus(),
						StringUtils.defaultIfBlank(harassmentScene.getRegistrationType(), null), StringUtils.defaultIfBlank(harassmentScene.getReportDate(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplaintTime(), null), StringUtils.defaultIfBlank(harassmentScene.getCallTime(), null),
						StringUtils.defaultIfBlank(harassmentScene.getContactNumber(), null), StringUtils.defaultIfBlank(harassmentScene.getReporter(), null),
						StringUtils.defaultIfBlank(harassmentScene.getReporterId(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplaintNumber(), null), StringUtils.defaultIfEmpty(harassmentScene.getComplainedNumber(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplaintCategory(), null), StringUtils.defaultIfBlank(harassmentScene.getTeam(), null),
						StringUtils.defaultIfBlank(harassmentScene.getTeamId(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplaintNumberOperator(), null), StringUtils.defaultIfBlank(harassmentScene.getComplainedNumberProvince(), null),
						StringUtils.defaultIfBlank(harassmentScene.getServiceOrderNumber(), null), StringUtils.defaultIfBlank(harassmentScene.getComplaintNumberProvince(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplainedNumberCity(), null), StringUtils.defaultIfBlank(harassmentScene.getComplaintDescription(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplaintNumberLocation(), null), StringUtils.defaultIfBlank(harassmentScene.getOperator(), null),
						StringUtils.defaultIfBlank(harassmentScene.getFileName(), null),StringUtils.defaultIfBlank(harassmentScene.getGroupId(), null));
			}
			catch(Exception e) {
				log.error("createRegistrationExcel mysql异常：{}", e.getMessage(), e);
			}
		}else {
			sql = "INSERT INTO cc_harassment_scene (ID,REPORT_DATE, REPORTER, REPORT_STAFF,TEAM, TEAM_ID,SERVICE_ORDER_ID, COMPLAINT_NUMBER, REPORTER_NUMBER_TYPE, COMPLAINED_NUMBER, " +
					"REPORTED_NUMBER_TYPE, COMPLAINED_NUMBER_PROVINCE, COMPLAINED_NUMBER_CITY, OPERATOR, COMPLAINT_TIME, CALL_TIME, CALL_DURATION, " +
					"CONTENT_CLASSIFICATION, HARASSMENT_TYPE, COMPLAINT_DESCRIPTION, CONTACT_NUMBER,SUBMISSION_TIME,DOWNLOAD_STATUS,REGISTRATION_TYPE,FILE_NAME,GROUP_ID) " +
					"VALUES " +
					"(?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?)";
			try {
				return jt.update(sql, IdUtils.fastSimpleUUID(), StringUtils.defaultIfBlank(harassmentScene.getReportDate(), null),
						StringUtils.defaultIfBlank(harassmentScene.getReporter(), null), StringUtils.defaultIfBlank(harassmentScene.getReporterId(), null),
						StringUtils.defaultIfBlank(harassmentScene.getTeam(), null),StringUtils.defaultIfBlank(harassmentScene.getTeamId(), null),
						StringUtils.defaultIfBlank(harassmentScene.getServiceOrderNumber(), null), StringUtils.defaultIfBlank(harassmentScene.getComplaintNumber(), null),
						StringUtils.defaultIfBlank(harassmentScene.getReporterNumberType(), null), StringUtils.defaultIfBlank(harassmentScene.getComplainedNumber(), null),
						StringUtils.defaultIfBlank(harassmentScene.getReportedNumberType(), null), StringUtils.defaultIfBlank(harassmentScene.getComplainedNumberProvince(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplainedNumberCity(), null), StringUtils.defaultIfBlank(harassmentScene.getOperator(), null),
						StringUtils.defaultIfBlank(harassmentScene.getComplaintTime(), null), StringUtils.defaultIfBlank(harassmentScene.getCallTime(), null),
						StringUtils.defaultIfBlank(harassmentScene.getCallDuration(), null), StringUtils.defaultIfBlank(harassmentScene.getContentClassification(), null),
						StringUtils.defaultIfBlank(harassmentScene.getHarassmentType(), null), StringUtils.defaultIfBlank(harassmentScene.getComplaintDescription(), null),
						StringUtils.defaultIfBlank(harassmentScene.getContactNumber(), null), StringUtils.defaultIfBlank(harassmentScene.getSubmissionTime(), null),
						StringUtils.defaultIfBlank(harassmentScene.getDownloadStatus(), null), StringUtils.defaultIfBlank(harassmentScene.getRegistrationType(), null),
						StringUtils.defaultIfBlank(harassmentScene.getFileName(), null),StringUtils.defaultIfBlank(harassmentScene.getGroupId(), null));
			}
			catch(Exception e) {
				log.error("createRegistrationExcel mysql异常：{}", e.getMessage(), e);
			}
		}
        return 0;
	}

	@Override
	public GridDataInfo getHarassmentScene(int begion,int pageSize,String time, String status) {
		// 分割时间字符串，获取开始和结束时间
		String[] split = time.split(",");
		String startTime = split[0];
		String endTime = split[1];
		String baseSql = "SELECT %PARAM% FROM CC_HARASSMENT_SCENE WHERE SUBMISSION_TIME >= '" + startTime +
				"' AND SUBMISSION_TIME <= '" + endTime + "' AND DOWNLOAD_STATUS = '" + status + "'";
		baseSql += " ORDER BY SUBMISSION_TIME";
		String strSql = baseSql.replace("%PARAM%","ID, DATE_FORMAT(SUBMISSION_TIME, '%Y-%m-%d %H:%i:%s') AS SUBMISSION_TIME, " +
				"DATE_FORMAT(DOWNLOAD_TIME, '%Y-%m-%d %H:%i:%s') AS DOWNLOAD_TIME, DOWNLOAD_STATUS, REGISTRATION_TYPE," +
				"DATE_FORMAT(REPORT_DATE, '%m-%d') AS REPORT_DATE, DATE_FORMAT(COMPLAINT_TIME, '%Y-%m-%d %H:%i:%s') AS COMPLAINT_TIME, " +
				"DATE_FORMAT(CALL_TIME, '%Y-%m-%d %H:%i:%s') AS CALL_TIME, CONTACT_NUMBER, REPORTER,REPORT_STAFF, COMPLAINT_NUMBER," +
				"COMPLAINED_NUMBER, COMPLAINT_CATEGORY, TEAM, TEAM_ID,COMPLAINT_NUMBER_OPERATOR, COMPLAINED_NUMBER_PROVINCE," +
				"SERVICE_ORDER_ID, COMPLAINT_NUMBER_PROVINCE, COMPLAINED_NUMBER_CITY, COMPLAINT_DESCRIPTION," +
				"COMPLAINT_NUMBER_LOCATION, OPERATOR, REPORTER_NUMBER_TYPE, REPORTED_NUMBER_TYPE, CALL_DURATION," +
				"CONTENT_CLASSIFICATION, HARASSMENT_TYPE,FILE_NAME,GROUP_ID ");
		String countSql = baseSql.replace("%PARAM%", "COUNT(1)");
		try{
			return this.dbgridDataPub.getResultNewBySize(countSql, strSql, begion, pageSize, "", "");
		}catch (Exception e){
			log.error("getHarassmentScene mysql异常：{}",e.getMessage(),e);
		}
		return null;
	}

	@Override
	public int setDownloadTime(String downloadTime,String status,String id) {
		String sql = "UPDATE CC_HARASSMENT_SCENE SET DOWNLOAD_TIME = ?, DOWNLOAD_STATUS = ? WHERE GROUP_ID = ?";
		try{
			return jt.update(sql,downloadTime,status,id);
		}catch (Exception e){
			log.error("setDownloadTime mysql异常：{}",e.getMessage());
			return 0;
		}
	}

	public int checkRepeatedSubmission(HarassmentScene harassmentScene){
		String complaintNumber = harassmentScene.getComplaintNumber();
		String complainedNumber = harassmentScene.getComplainedNumber();
		String orderId = harassmentScene.getServiceOrderNumber();
		String sql = "SELECT COUNT(1) FROM CC_HARASSMENT_SCENE WHERE COMPLAINT_NUMBER = ? AND COMPLAINED_NUMBER = ? AND SERVICE_ORDER_ID = ?";
		try{
			return jt.queryForObject(sql,new Object[]{complaintNumber,complainedNumber,orderId},Integer.class);
		}catch (Exception e){
			log.error("checkRepeatedSubmission mysql异常：{}",e.getMessage());
			return 0;
		}
	}

}