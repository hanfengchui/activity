/*
 * 说明：执行标签增删改查操作的实现类
 * 时间： 2012-5-2
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.customservice.labelmanage.dao.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabelRmp;

/**
 * @author LiJiahui
 * 
 */
@SuppressWarnings("rawtypes")
public class LabelManageDAOImpl implements ILabelManageDAO {
	private static final Logger log = LoggerFactory.getLogger(LabelManageDAOImpl.class);
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private PubFunc pubFunc;

    /**
     * SQL语句，用于记录订单的处理完成时间
     */
    private String saveOrderFinishDate;

    /**
     * SQL语句，用于记录订单的定性结果
     */
    private String saveQualitative;

    /**
     * SQL语句，用于插入一条空记录
     */
    private String insertNew;

    /**
     * SQL语句，用于记录首次回复时间
     */
    private String saveFirstRespondDate;
    
    /**
     * SQL语句，用于记录部门正式回复时间
     */
    private String saveFormalAnswerDate;
    
    /**
     * SQL语句，用于更新最后一次部门处理提交时间
     */
    private String updateLastAnswerDate;

    /**
     * SQL语句，用于记录重复关联数据
     */
    private String insertServiceConnectionWithDateSql;
    private String insertServiceConnectionSql;

    /**
     * SQL语句，用于标记订单是否超时
     */
    private String updateOverTimeLabel;

    /**
     * SQL语句，用于查询当前订单的标签集信息
     */
    private String queryLabelById;

    /**
     * SQL语句，用于查询历史订单的标签集信息
     */
    private String queryLabelHisById;

    /**
     * SQL语句，用于保存工单三强终判值
     */
    private String updateForceCfmFlag;

    /**
     * SQL语句，用于保存工单三强终判值
     */
    private String insertLabelHisById;

    /**
     * SQL语句，用于保存工单三强终判值
     */
    private String deleteLabelById;

    /**
     * SQL语句，更新申诉是否有效
     */
    private String updateValidFlag;

    /**
     * SQL语句，更新升级倾向
     */
    private String updateUpgradeIncline;
    
    /**
     * SQL语句，更新有效催单的次数值
     */
    private String updateValidHastenNum;

    /**
     * SQL语句，更新省市总热线标识
     */
    private String updateHotlineFlag;
    private String updateAutoVisitFlagSql;
    private String selectAutoVisitFlagSql;
    private String updateDealResultSql;
    private String updateSecFlag;

    /**
     * SQL语句，剔除投诉工单处理内容中关键字“互联网卡”、“屏蔽外呼”标识：1、剔除
     */
    private String updateUnusualFlagSql;

    private String updateFirstAuditDateSql;
    private String selectFirstAuditDateSql;
    private String selectRepeatFlagSql;
    private String updateDealHoursSql;
    private String selectDealHoursSql;
    private String updateAuditHoursSql;
    private String selectAuditHoursSql;
    private String updateIsUnifiedSql;
    private String selectIsUnifiedSql;
    private String updateZdxCpDateSql;
    private String selectZdxCpDateSql;
    private String updateLastAuditDateSql;
    private String selectLastAuditDateSql;

    public String getUpdateUpgradeIncline() {
        return updateUpgradeIncline;
    }

    public void setUpdateUpgradeIncline(String updateUpgradeIncline) {
        this.updateUpgradeIncline = updateUpgradeIncline;
    }

    public int updateUpgradeIncline(String serviceId,int upgradeIncline){
        return this.jdbcTemplate.update(updateUpgradeIncline, upgradeIncline, serviceId);
    }

    public int updateValidFlag(String serviceId,int valiFlag){
        return this.jdbcTemplate.update(updateValidFlag, valiFlag, serviceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.flagmanage.dao.IFlagManageDAO#
     * updateFinishDate(java.lang.String)
     */
    public int saveFinishDate(String serviceId) {
    	return this.jdbcTemplate.update(saveOrderFinishDate, serviceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#
     * saveQualitative(java.lang.Integer, java.lang.String, java.lang.Long,
     * java.lang.String)
     */
    public int saveQualitative(Integer controlAreaSec, String dutyOrgSec, Long lastYY, String serviceId) {
    	return this.jdbcTemplate.update(saveQualitative, controlAreaSec,
                controlAreaSec, controlAreaSec, controlAreaSec, dutyOrgSec, dutyOrgSec, dutyOrgSec,
                dutyOrgSec, lastYY, lastYY, serviceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#insertNew
     * (java.lang.String)
     */
    public int insertNew(String serviceId) {
    	int size = 0;
    	int i = jdbcTemplate.queryForObject("select count(1) as ctn  from CC_SERVICE_LABEL a where a.service_order_id = ?" , new Object[] {serviceId},Integer.class);
    	if(i == 0) {
    		size = this.jdbcTemplate.update(insertNew, serviceId);
    	}
        return size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#
     * saveFirstRespondDate(java.lang.String)
     */
    public int saveFirstRevertDate(String serviceId) {
    	return this.jdbcTemplate.update(saveFirstRespondDate, serviceId);
    }
    
    /**
     * {@inheritDoc}
     */
    public int saveFormalAnswerDate(String serviceId) {
    	return this.jdbcTemplate.update(saveFormalAnswerDate, serviceId);
    }

    /* (non-Javadoc)
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#saveLastAnswerDate(java.lang.String)
     */
    @Override
	public int updateLastAnswerDate(String serviceId) {
    	return this.jdbcTemplate.update(updateLastAnswerDate, serviceId);
	}
    
    public int updateAdjustAccountFlag(String serviceId) {
    	String sqlStr = "UPDATE CC_SERVICE_LABEL L SET L.ADJUST_ACCOUNT_FLAG = 1 WHERE L.SERVICE_ORDER_ID = ?";
    	return this.jdbcTemplate.update(sqlStr, serviceId);
	}
    
    public int updateDirectDispatchFlag(String serviceId) {
    	String sqlStr = "UPDATE CC_SERVICE_LABEL L SET L.DIRECT_DISPATCH_FLAG = 1 WHERE L.SERVICE_ORDER_ID = ?";
    	return this.jdbcTemplate.update(sqlStr, serviceId);
	}
    
    public int updateUpTendencyFlag(String serviceId,int upTendencyFlag) {
    	String sqlStr = "update CC_SERVICE_LABEL c set c.up_tendency_flag=? where c.service_order_id=?";
    	return this.jdbcTemplate.update(sqlStr, upTendencyFlag, serviceId);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#
     * queryServiceLabelById(java.lang.String, boolean)
     */
    @SuppressWarnings( "unchecked" )
	public ServiceLabel queryServiceLabelById(String serviceId, boolean hisFlag) {
        List list = null;
        if (hisFlag) {// false当前；true历史
            list = this.jdbcTemplate.query(queryLabelHisById, new Object[] {serviceId},
                    new ServiceLabelRmp());
        } else {
            list = this.jdbcTemplate.query(queryLabelById, new Object[] {serviceId},
                    new ServiceLabelRmp());
        }
        if (list.isEmpty()) {
            return null;
        }
        return ((ServiceLabel) list.get(0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#
     * updateOverTimeLabel(java.lang.String)
     */
    public int updateOverTimeLabel(String serviceId) {
    	int overTimeFlag = pubFunc.judgeOrderOvertime(serviceId);
        return this.jdbcTemplate.update(updateOverTimeLabel, overTimeFlag, serviceId);
    }

	public int insertServiceConnection(String connectionGuid, String serviceOrderId, int connectionState, String connectionType, String acceptDate) {
		if ("".equals(acceptDate)) {
			return this.jdbcTemplate.update(insertServiceConnectionSql, connectionGuid, serviceOrderId, connectionState, connectionType);
		} else {
			return this.jdbcTemplate.update(insertServiceConnectionWithDateSql, connectionGuid, serviceOrderId, connectionState, connectionType, acceptDate);
		}
	}
	
	public List getRepeatGuidList(String serviceOrderId) {
		String strSql = "select c.CONNECTION_GUID,c.CONNECTION_TYPE from cc_service_connection c where c.SERVICE_ORDER_ID=? and c.CONNECTION_STATE='1'";
		return jdbcTemplate.queryForList(strSql, serviceOrderId);
	}
	
	public int deleteServiceConnection(String connectionGuid) {
		String strSql = "delete from cc_service_connection where connection_guid=?";
		return jdbcTemplate.update(strSql, connectionGuid);
	}
	
	public int saveOldConnection(String guid, String orderId, String type) {
		String strSql = "insert into cc_old_connection(CONNECTION_GUID, SERVICE_ORDER_ID, CONNECTION_TYPE) values (?, ?, ?)";
		return jdbcTemplate.update(strSql, guid, orderId, type);
	}

    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#
     * saveLabelHisById(java.lang.String)
     */
    public int saveLabelHisById(String serviceId) {
        int res = this.jdbcTemplate.update(insertLabelHisById, serviceId);
        if (res > 0) {
            return this.jdbcTemplate.update(deleteLabelById, serviceId);
        }
        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO#
     * updateForceCfmFlag(java.lang.String, java.lang.String)
     */
    public int updateForceCfmFlag(String serviceId, String forceId) {
        return this.jdbcTemplate.update(updateForceCfmFlag, forceId, serviceId);
    }
    
    public int updateValidHastenNum(String serviceId, int num){
    	return this.jdbcTemplate.update(updateValidHastenNum, num, serviceId);
    }

    /**
     * 更新省市总热线标识
     */
	public int updateHotlineFlag(String serviceOrderId, int staffId, int hotlineFlag) {
		return this.jdbcTemplate.update(updateHotlineFlag, hotlineFlag, staffId, serviceOrderId);
	}

    /**
     * 更新终定性环节中“回访记录”区域的“投诉处理结果”值
     */	
	public int updateDealResult(String serviceOrderId, int dealResult, String dealResultName) {
		return this.jdbcTemplate.update(updateDealResultSql, dealResult, dealResultName, serviceOrderId);
	}

    public String queryFinishDate(String serviceId){
        String sql = "SELECT DATE_FORMAT(L.DX_FINISH_DATE,'%Y-%m-%d %H:%i:%s') AS DX_FINISH_DATE FROM CC_SERVICE_LABEL L WHERE L.SERVICE_ORDER_ID=?";
        List<String> tmp = jdbcTemplate.queryForList(sql, new Object[] { serviceId }, String.class);
        if(tmp.isEmpty()) {
        	return null;
        }
        return tmp.get(0);
    }
	
	public int saveSecFlag(String serviceId) {
		return this.jdbcTemplate.update(updateSecFlag, serviceId);
	}

	public int updateUnusualFlag(String serviceId) {
		return this.jdbcTemplate.update(updateUnusualFlagSql, serviceId);
	}

	public int updateFirstAuditDate(String serviceOrderId) {
		return this.jdbcTemplate.update(updateFirstAuditDateSql, serviceOrderId);
	}

	public String selectFirstAuditDate(String serviceOrderId) {
		List tmp = jdbcTemplate.queryForList(selectFirstAuditDateSql, new Object[] { serviceOrderId }, String.class);
		if(tmp.isEmpty()) {
			return null;
		}
		return tmp.get(0).toString();
	}

	public int selectRepeatFlag(String serviceOrderId) {
		int repeatFlag = 0;
		try {
			List<Integer> tmp = this.jdbcTemplate.queryForList(selectRepeatFlagSql, new Object[] { serviceOrderId },Integer.class);
			if(!tmp.isEmpty()) {
				repeatFlag = tmp.get(0);
			}
		} catch (Exception e) {
			repeatFlag = 0;
		}
		return repeatFlag;
	}

	public int updateDealHours(int dealHours, String serviceOrderId) {
		return this.jdbcTemplate.update(updateDealHoursSql, dealHours, serviceOrderId);
	}

	public int selectDealHours(String serviceOrderId) {
		int dealHours = 30;
		try {
			List<Integer> tmp = this.jdbcTemplate.queryForList(selectDealHoursSql, new Object[] { serviceOrderId },Integer.class);
			if(!tmp.isEmpty()) {
				dealHours = tmp.get(0);
			}
		} catch (Exception e) {
			dealHours = 30;
		}
		return dealHours;
	}

	public int updateAuditHours(int auditHours, String serviceOrderId) {
		return this.jdbcTemplate.update(updateAuditHoursSql, auditHours, serviceOrderId);
	}

	public int selectAuditHours(String serviceOrderId) {
		int auditHours = 6;
		try {
			List<Integer> tmp = this.jdbcTemplate.queryForList(selectAuditHoursSql, new Object[] { serviceOrderId },Integer.class);
			if(!tmp.isEmpty()) {
				auditHours = tmp.get(0);
			}
		} catch (Exception e) {
			auditHours = 6;
		}
		return auditHours;
	}

	public int updateIsUnified(int isUnified, String serviceOrderId) {
		return this.jdbcTemplate.update(updateIsUnifiedSql, isUnified, serviceOrderId);
	}

	public int selectIsUnified(String serviceOrderId) {
		int isUnified = 0;
		try {
			List<Integer> tmp = this.jdbcTemplate.queryForList(selectIsUnifiedSql, new Object[] { serviceOrderId },Integer.class);
			if(!tmp.isEmpty()) {
				isUnified = tmp.get(0);
			}
		} catch (Exception e) {
			isUnified = 0;
		}
		return isUnified;
	}

	public int updateAutoVisitFlag(int autoVisitFlag, String serviceOrderId) {
		return this.jdbcTemplate.update(updateAutoVisitFlagSql, autoVisitFlag, serviceOrderId);
	}

	public int selectAutoVisitFlag(String serviceOrderId) {
		int autoVisitFlag = 0;
		try {
			List<Integer> tmp = this.jdbcTemplate.queryForList(selectAutoVisitFlagSql, new Object[] { serviceOrderId },Integer.class);
			if(!tmp.isEmpty()) {
				autoVisitFlag = tmp.get(0);
			}
		} catch (Exception e) {
			autoVisitFlag = 0;
		}
		return autoVisitFlag;
	}

	public int updateZdxCpDate(String serviceOrderId) {
		return this.jdbcTemplate.update(updateZdxCpDateSql, serviceOrderId);
	}

	public String selectZdxCpDate(String serviceOrderId) {
		List<String> tmp = jdbcTemplate.queryForList(selectZdxCpDateSql, new Object[] { serviceOrderId }, String.class);
		if(tmp.isEmpty()){
			return null;
		}
		return tmp.get(0);
	}

	public int updateLastAuditDate(String serviceOrderId) {
		return this.jdbcTemplate.update(updateLastAuditDateSql, serviceOrderId);
	}

	public String selectLastAuditDate(String serviceOrderId) {
		List tmp = jdbcTemplate.queryForList(selectLastAuditDateSql, new Object[] { serviceOrderId }, String.class);
		if(tmp.isEmpty()) {
			return null;
		}
		return tmp.get(0).toString();
	}
	
    public int saveFinalOptionLabel(String orderId, String sheetId) {
    	String sqlStr = "UPDATE CC_SERVICE_LABEL L SET L.final_option_flag = ? WHERE L.SERVICE_ORDER_ID = ?";
    	return this.jdbcTemplate.update(sqlStr, sheetId, orderId);
	}

	public int updatePassiveRepeatFlag(String orderId, String accNum, int region) {
		if (accNum.length() <= 4) {
			return 0;
		}
		String querySql = 
"SELECT service_order_id SOI,0 HISFLAG FROM cc_service_order_ask WHERE service_type IN(700006312,720130000) AND order_statu NOT IN(700000099,700000100,"
+ "720130001,720130003) AND accept_date>date_sub(now(),interval 30 day) AND region_id=? AND prod_num=? AND service_order_id<>? UNION ALL SELECT service_order_id,1 FROM "
+ "cc_service_order_ask_his WHERE service_type IN(700006312,720130000) AND order_statu IN(700000103,720130010) AND accept_date>date_sub(now(),interval 30 day) AND region_id"
+ "=? AND prod_num=? AND service_order_id<>?";
		List soiList = jdbcTemplate.queryForList(querySql, region, accNum, orderId, region, accNum, orderId);
		if (soiList.isEmpty()) {
			return 0;
		}
		String cncSql = "INSERT INTO cc_passive_connection(passive_guid,service_order_id,passive_type,modify_date)VALUES(REPLACE(UUID(),'-',''),?,1,NOW())";
		String curSql = "UPDATE cc_service_label SET passive_repeat_flag=1 WHERE service_order_id=?";
		String hisSql = "UPDATE cc_service_label_his SET passive_repeat_flag=1 WHERE service_order_id=?";
		for (int i = 0; i < soiList.size(); i++) {
			Map soiMap = (Map) soiList.get(i);
			String soi = soiMap.get("SOI").toString();
			String hisFlag = soiMap.get("HISFLAG").toString();
			if ("0".equals(hisFlag)) {
				jdbcTemplate.update(curSql, soi);
			} else {
				//jdbcTemplate.update(rptSql, soi);
				jdbcTemplate.update(cncSql, soi);
				jdbcTemplate.update(hisSql, soi);
			}
		}
		String sql = "UPDATE cc_service_label SET passive_repeat_flag=2 WHERE service_order_id=?";
		return jdbcTemplate.update(sql, orderId);
	}

	public int updatePassiveUpgradeFlag(String orderId, String accNum, int region) {
		if (accNum.length() <= 4) {
			return 0;
		}
		String querySql = 
"SELECT service_order_id SOI,0 HISFLAG FROM cc_service_order_ask WHERE service_type IN(700006312,720130000) AND order_statu NOT IN(700000099,700000100,"
+ "720130001,720130003) AND accept_date>date_sub(now(),interval 60 day) AND region_id=? AND prod_num=? AND service_order_id<>? UNION ALL SELECT service_order_id,1 FROM "
+ "cc_service_order_ask_his WHERE service_type IN(700006312,720130000) AND order_statu IN(700000103,720130010) AND accept_date>date_sub(now(),interval 60 day) AND region_id"
+ "=? AND prod_num=? AND service_order_id<>?";
		List soiList = jdbcTemplate.queryForList(querySql, region, accNum, orderId, region, accNum, orderId);
		if (soiList.isEmpty()) {
			return 0;
		}
		String cncSql = "INSERT INTO cc_passive_connection(passive_guid,service_order_id,passive_type,modify_date)VALUES(REPLACE(UUID(),'-',''),?,2,NOW())";
		String curSql = "UPDATE cc_service_label SET passive_upgrade_flag=1 WHERE service_order_id=?";
		String hisSql = "UPDATE cc_service_label_his SET passive_upgrade_flag=1 WHERE service_order_id=?";
		for (int i = 0; i < soiList.size(); i++) {
			Map soiMap = (Map) soiList.get(i);
			String soi = soiMap.get("SOI").toString();
			String hisFlag = soiMap.get("HISFLAG").toString();
			if ("0".equals(hisFlag)) {
				jdbcTemplate.update(curSql, soi);
			} else {
				//jdbcTemplate.update(rptSql, soi);
				jdbcTemplate.update(cncSql, soi);
				jdbcTemplate.update(hisSql, soi);
			}
		}
		String sql = "UPDATE cc_service_label SET passive_upgrade_flag=2 WHERE service_order_id=?";
		return jdbcTemplate.update(sql, orderId);
    }
	
	public void updateServiceLabel(ServiceLabel label) {
		String sql = "UPDATE cc_service_label\n"
				+ "SET is_unified = ?,\n"
				+ "repeat_flag = ifnull(?, 0),\n"
				+ "repeat_new_flag = ifnull(?, 0),\n"
				+ "deal_hours = ifnull(?, 0),\n"
				+ "audit_hours = ifnull(?, 0),\n" 
				+ "formal_answer_date = if(?=null, null, NOW()),\n"
				+ "last_answer_date = if(?=null, null, NOW()),\n"
				+ "adjust_account_flag = ifnull(?, 0),\n"
				+ "direct_dispatch_flag = ifnull(?, 0),\n"
				+ "up_tendency_flag = ifnull(?, 0),\n"
				+ "first_revert_date = if(?=null, null, NOW()),\n"
				+ "sys_modi_date = NOW(),\n"
				+ "c_repeat_flag = ifnull(?, 0),\n"
				+ "c_repeat_best_flag = ifnull(?, 0),\n"
				+ "order_type = ifnull(?, 0),\n"
				+ "sensitive_num = ifnull(?, 0)\n"
				+ "WHERE service_order_id = ?";
		jdbcTemplate.update(sql, 
				label.getIsUnified(),
				label.getRepeatFlag(),
				label.getRepeatNewFlag(),
				label.getDealHours(),
				label.getAuditHours(),
				label.getFormalAnswerDate(),
				label.getLastAnswerDate(),
				label.getAdjustAccountFlag(),
				label.getDirectDispatchFlag(),
				label.getUpTendencyFlag(),
				label.getFirstRevertDate(),
				label.getCptRepeatFlag(),
				label.getCptRepeatBestFlag(),
				label.getOrderType(),
				label.getSensitiveNum(),
				label.getServiceOrderId());
	}
	
	public void updateCallFlag(String orderId, int callFlag) {
		try {
			String sql = "UPDATE cc_service_label SET c_call_flag = ?, sys_modi_date = now() WHERE service_order_id = ?";
			jdbcTemplate.update(sql, callFlag, orderId);
		} catch (Exception e) {
			log.error("updateCallFlag mysql error: {}", e.getMessage(), e);
		}
	}
	
	public void updateCustType(String orderId, int flag) {
		try {
			String sql = "UPDATE cc_service_label SET cust_type = ?, sys_modi_date = now() WHERE service_order_id = ?";
			jdbcTemplate.update(sql, flag, orderId);
		} catch (Exception e) {
			log.error("updateCustType mysql error: {}", e.getMessage(), e);
		}
	}

	public int insertRuyiLabel(String orderId) {
		String sql = "INSERT INTO cc_ruyi_label(service_order_id,is_ruyi,create_date)values(?,1,NOW())";
		try {
			return jdbcTemplate.update(sql, orderId);
		} catch (Exception e) {
			log.error("insertRuyiLabel error: {}", e.getMessage());
		}
		return 0;
	}

	public int insertRuyiLabelHis(String orderId) {
		String sql = "INSERT INTO cc_ruyi_label_his(service_order_id,is_ruyi,create_date,his_date)"
				+ "SELECT service_order_id,is_ruyi,create_date,NOW() FROM cc_ruyi_label WHERE service_order_id=?";
		try {
			if (jdbcTemplate.update(sql, orderId) > 0) {
				return jdbcTemplate.update("DELETE FROM cc_ruyi_label WHERE service_order_id=?", orderId);
			}
		} catch (Exception e) {
			log.error("insertRuyiLabelHis error: {}", e.getMessage());
		}
		return 0;
	}

	public void updateRepeatLabel(ServiceLabel label) {
		log.info("updateRepeatLabel: {}", label);
		String sql = "UPDATE cc_service_label SET\n"
				+ "repeat_flag = ifnull(?, 0),\n"
				+ "repeat_new_flag = ifnull(?, 0),\n"
				+ "sys_modi_date = NOW(),\n"
				+ "c_repeat_flag = ifnull(?, 0),\n"
				+ "c_repeat_best_flag = ifnull(?, 0)\n"
				+ "WHERE service_order_id = ?";
		jdbcTemplate.update(sql, 
				label.getRepeatFlag(),
				label.getRepeatNewFlag(),
				label.getCptRepeatFlag(),
				label.getCptRepeatBestFlag(),
				label.getServiceOrderId());
	}
	
	public void updateRepeatBestFlag(String orderId) {
		String sql = "UPDATE cc_service_label SET repeat_flag = 0, sys_modi_date = NOW() WHERE service_order_id = ?";
		int num = jdbcTemplate.update(sql, orderId);
		log.info("updateRepeatBestFlag: {} result: {}", orderId, num);
	}
	
	public void updateRefundFlag(String orderId, int flag) {
		int num = 0;
		try {
			String sql = "UPDATE cc_service_label SET REFUND_FLAG = ?, sys_modi_date = now() WHERE service_order_id = ?";
			num = jdbcTemplate.update(sql, flag, orderId);
		} catch (Exception e) {
			log.error("updateRefundFlag mysql error: {}", e.getMessage(), e);
		}
		log.info("updateRefundFlag: {} flag: {} result: {}", orderId, flag, num);
	}

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setSaveOrderFinishDate(String saveOrderFinishDate) {
        this.saveOrderFinishDate = saveOrderFinishDate;
    }

    public void setSaveQualitative(String saveQualitative) {
        this.saveQualitative = saveQualitative;
    }

	public void setInsertNew(String insertNew) {
        this.insertNew = insertNew;
    }

    public void setSaveFirstRespondDate(String saveFirstRespondDate) {
        this.saveFirstRespondDate = saveFirstRespondDate;
    }

    public void setQueryLabelById(String queryLabelById) {
        this.queryLabelById = queryLabelById;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public String getSaveOrderFinishDate() {
        return saveOrderFinishDate;
    }

    public String getSaveQualitative() {
        return saveQualitative;
    }

    public String getInsertNew() {
        return insertNew;
    }

    public String getSaveFirstRespondDate() {
        return saveFirstRespondDate;
    }

    public String getQueryLabelById() {
        return queryLabelById;
    }

    public String getUpdateOverTimeLabel() {
        return updateOverTimeLabel;
    }

    public void setUpdateOverTimeLabel(String updateOverTimeLabel) {
        this.updateOverTimeLabel = updateOverTimeLabel;
    }

    public String getUpdateForceCfmFlag() {
        return updateForceCfmFlag;
    }

    public void setUpdateForceCfmFlag(String updateForceCfmFlag) {
        this.updateForceCfmFlag = updateForceCfmFlag;
    }

    public String getQueryLabelHisById() {
        return queryLabelHisById;
    }

    public void setQueryLabelHisById(String queryLabelHisById) {
        this.queryLabelHisById = queryLabelHisById;
    }



    public String getInsertServiceConnectionWithDateSql() {
		return insertServiceConnectionWithDateSql;
	}

	public void setInsertServiceConnectionWithDateSql(String insertServiceConnectionWithDateSql) {
		this.insertServiceConnectionWithDateSql = insertServiceConnectionWithDateSql;
	}

	public String getInsertServiceConnectionSql() {
		return insertServiceConnectionSql;
	}

	public void setInsertServiceConnectionSql(String insertServiceConnectionSql) {
		this.insertServiceConnectionSql = insertServiceConnectionSql;
	}

    public String getDeleteLabelById() {
        return deleteLabelById;
    }

    public void setDeleteLabelById(String deleteLabelById) {
        this.deleteLabelById = deleteLabelById;
    }

    public String getInsertLabelHisById() {
        return insertLabelHisById;
    }

    public void setInsertLabelHisById(String insertLabelHisById) {
        this.insertLabelHisById = insertLabelHisById;
    }

    public String getUpdateValidFlag() {
        return updateValidFlag;
    }

    public void setUpdateValidFlag(String updateValidFlag) {
        this.updateValidFlag = updateValidFlag;
    }

    public void setSaveFormalAnswerDate(String saveFormalAnswerDate) {
        this.saveFormalAnswerDate = saveFormalAnswerDate;
    }

	public void setUpdateValidHastenNum(String updateValidHastenNum) {
		this.updateValidHastenNum = updateValidHastenNum;
	}

	public void setUpdateLastAnswerDate(String updateLastAnswerDate) {
		this.updateLastAnswerDate = updateLastAnswerDate;
	}

	public String getUpdateHotlineFlag() {
		return updateHotlineFlag;
	}

	public void setUpdateHotlineFlag(String updateHotlineFlag) {
		this.updateHotlineFlag = updateHotlineFlag;
	}

	public String getUpdateDealResultSql() {
		return updateDealResultSql;
	}

	public void setUpdateDealResultSql(String updateDealResultSql) {
		this.updateDealResultSql = updateDealResultSql;
	}

	public String getUpdateSecFlag() {
		return updateSecFlag;
	}

	public void setUpdateSecFlag(String updateSecFlag) {
		this.updateSecFlag = updateSecFlag;
	}

	public String getUpdateUnusualFlagSql() {
		return updateUnusualFlagSql;
	}

	public void setUpdateUnusualFlagSql(String updateUnusualFlagSql) {
		this.updateUnusualFlagSql = updateUnusualFlagSql;
	}

	public String getUpdateFirstAuditDateSql() {
		return updateFirstAuditDateSql;
	}

	public void setUpdateFirstAuditDateSql(String updateFirstAuditDateSql) {
		this.updateFirstAuditDateSql = updateFirstAuditDateSql;
	}

	public String getSelectFirstAuditDateSql() {
		return selectFirstAuditDateSql;
	}

	public void setSelectFirstAuditDateSql(String selectFirstAuditDateSql) {
		this.selectFirstAuditDateSql = selectFirstAuditDateSql;
	}

	public String getSelectRepeatFlagSql() {
		return selectRepeatFlagSql;
	}

	public void setSelectRepeatFlagSql(String selectRepeatFlagSql) {
		this.selectRepeatFlagSql = selectRepeatFlagSql;
	}

	public String getUpdateDealHoursSql() {
		return updateDealHoursSql;
	}

	public void setUpdateDealHoursSql(String updateDealHoursSql) {
		this.updateDealHoursSql = updateDealHoursSql;
	}

	public String getSelectDealHoursSql() {
		return selectDealHoursSql;
	}

	public void setSelectDealHoursSql(String selectDealHoursSql) {
		this.selectDealHoursSql = selectDealHoursSql;
	}

	public String getUpdateAuditHoursSql() {
		return updateAuditHoursSql;
	}

	public void setUpdateAuditHoursSql(String updateAuditHoursSql) {
		this.updateAuditHoursSql = updateAuditHoursSql;
	}

	public String getSelectAuditHoursSql() {
		return selectAuditHoursSql;
	}

	public void setSelectAuditHoursSql(String selectAuditHoursSql) {
		this.selectAuditHoursSql = selectAuditHoursSql;
	}

	public String getUpdateIsUnifiedSql() {
		return updateIsUnifiedSql;
	}

	public void setUpdateIsUnifiedSql(String updateIsUnifiedSql) {
		this.updateIsUnifiedSql = updateIsUnifiedSql;
	}

	public String getSelectIsUnifiedSql() {
		return selectIsUnifiedSql;
	}

	public void setSelectIsUnifiedSql(String selectIsUnifiedSql) {
		this.selectIsUnifiedSql = selectIsUnifiedSql;
	}

	public String getUpdateAutoVisitFlagSql() {
		return updateAutoVisitFlagSql;
	}

	public void setUpdateAutoVisitFlagSql(String updateAutoVisitFlagSql) {
		this.updateAutoVisitFlagSql = updateAutoVisitFlagSql;
	}

	public String getSelectAutoVisitFlagSql() {
		return selectAutoVisitFlagSql;
	}

	public void setSelectAutoVisitFlagSql(String selectAutoVisitFlagSql) {
		this.selectAutoVisitFlagSql = selectAutoVisitFlagSql;
	}

	public String getUpdateZdxCpDateSql() {
		return updateZdxCpDateSql;
	}

	public void setUpdateZdxCpDateSql(String updateZdxCpDateSql) {
		this.updateZdxCpDateSql = updateZdxCpDateSql;
	}

	public String getSelectZdxCpDateSql() {
		return selectZdxCpDateSql;
	}

	public void setSelectZdxCpDateSql(String selectZdxCpDateSql) {
		this.selectZdxCpDateSql = selectZdxCpDateSql;
	}

	public String getUpdateLastAuditDateSql() {
		return updateLastAuditDateSql;
	}

	public void setUpdateLastAuditDateSql(String updateLastAuditDateSql) {
		this.updateLastAuditDateSql = updateLastAuditDateSql;
	}

	public String getSelectLastAuditDateSql() {
		return selectLastAuditDateSql;
	}

	public void setSelectLastAuditDateSql(String selectLastAuditDateSql) {
		this.selectLastAuditDateSql = selectLastAuditDateSql;
	}
}