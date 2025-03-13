/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfoRmp;
import com.timesontransfar.customservice.worksheet.service.ItsSheetDealPub;

@SuppressWarnings("rawtypes")
public class TsSheetDealPubImpl implements ItsSheetDealPub {
   
    private static final Logger logger = LoggerFactory.getLogger(TsSheetDealPubImpl.class);
    
    @Autowired
    private JdbcTemplate jt;
	
	@Autowired
    private ISystemAuthorization systemAuthorization;

	@Autowired
    private WorkSheetAllot workSheetAllot;
	
	@Autowired
	private PubFunc pubFunc;

    class KeyRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int arg1) throws SQLException {
            return rs.getString(1);
        }
    }

    /**
     * 查询在南京分公司的工单池的 后台派单工单、部门处理工单、预定性工单
     */
    private String allotDDQSql;
    /**
     * 查询在南京分公司的工单池的审批工单
     */
    private String allotApvSql;
    /**
     * 查询在南京分公司的工单池的审核工单
     */
    private String allotFinApvSql;
    /**
     * 将待处理的工单放进个人任务池
     */
    private String distillSql;
    
    /*
     * (non-Javadoc)
     * 
     * @see com.timesontransfar.customservice.worksheet.service.ItsSheetDealPub#
     * autoAllotWorkSheet(String str)
     */
    @SuppressWarnings("unchecked")
	public String autoAllotWorkSheet(String str, List<String> paramList) {
    	if ("2".equals(pubFunc.querySysContolFlag("batchAllotWorkSheet.flag"))){
            return "当前正在派发，请等待.";
    	}
        pubFunc.updateSysContolFlag("batchAllotWorkSheet.flag", "2", 3600);
        final List all = new ArrayList();
        
        Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "A");
		tableMap.put("CC_SERVICE_ORDER_ASK", "S");
        tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
        tableMap.put("CC_ORDER_CUST_INFO", "C");
        
		String s1 = this.systemAuthorization.getAuthedSql(tableMap, allotDDQSql, "900018300");
		String s2 = this.systemAuthorization.getAuthedSql(tableMap, allotApvSql, "900018300");
		String s3 = this.systemAuthorization.getAuthedSql(tableMap, allotFinApvSql, "900018300");

        this.allot1(s1+str, paramList, all);
        this.allot2(s2+str, paramList, all);
        this.allot3(s3+str, paramList, all);

        if(!all.isEmpty()){
            jt.batchUpdate(distillSql, new BatchPreparedStatementSetter() {
                SheetPubInfo info = null;
                public void setValues(PreparedStatement ps, int x) throws SQLException {
                    info = (SheetPubInfo) all.get(x);
                    ps.setInt(1, info.getDealStaffId());
                    ps.setString(2, StringUtils.defaultIfEmpty(info.getDealStaffName(),null));
                    ps.setInt(3, info.getRcvStaffId());
                    ps.setString(4, StringUtils.defaultIfEmpty(info.getRcvStaffName(),null));
                    ps.setString(5, StringUtils.defaultIfEmpty(info.getDealOrgId(),null));
                    ps.setString(6, StringUtils.defaultIfEmpty(info.getDealOrgName(),null));
                    ps.setInt(7, info.getSheetStatu());
                    ps.setString(8, StringUtils.defaultIfEmpty(info.getSheetSatuDesc(),null));
                    ps.setInt(9, info.getLockFlag());
                    ps.setString(10, StringUtils.defaultIfEmpty(info.getWorkSheetId(),null));
                }
                public int getBatchSize() {
                    return all.size();
                }
            });
            pubFunc.updateSysContolFlag("batchAllotWorkSheet.flag", "1", 3600);
            return "系统成功派发的工单数目为：" + all.size();
        }
        pubFunc.updateSysContolFlag("batchAllotWorkSheet.flag", "1", 3600);
        return "系统成功派发的工单数目为 零.";
    }
    
    @SuppressWarnings("unchecked")
	private void allot1(String sql, List<String> paramList, List all) {
        List tmp = null;
        SheetPubInfo sheetPubInfo = null;
        String result = "NONE";
        
    	// 后台派单、部门处理
        try {
        	Object[] array = paramList.toArray();
        	logger.info("allot1 sql: {} \n args: {}", sql, JSON.toJSON(array));
            tmp = jt.query(sql, array, new SheetPubInfoRmp());//CodeSec未验证的SQL注入；CodeSec误报：2
            if (null != tmp && !tmp.isEmpty()) {
                for (int i = tmp.size()-1; i >= 0; i--) {
                    sheetPubInfo = (SheetPubInfo) tmp.get(i);
                    if(sheetPubInfo.getTacheId()==StaticData.TACHE_ASSIGN || sheetPubInfo.getTacheId()==StaticData.TACHE_ASSIGN_NEW){
                    	//后台派单
                    	result = workSheetAllot.allotToAllot(sheetPubInfo);
                    }
                    else if(sheetPubInfo.getTacheId()==StaticData.TACHE_DEAL || sheetPubInfo.getTacheId()==StaticData.TACHE_DEAL_NEW){
                    	//部门处理
                    	result = workSheetAllot.allotToDeal(sheetPubInfo);
                    }
                    if (WorkSheetAllot.RST_NONE.equals(result)) {
                        tmp.remove(i);
                    }
                }
                all.addAll(tmp);
            }
        } catch (Exception e) {
        	logger.error("allot1 error: {}", e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
	private void allot2(String sql, List<String> paramList, List all) {
        List tmp = null;
        SheetPubInfo sheetPubInfo = null;
        String result = "NONE";
        
    	// 待审批
        try {
        	Object[] array = paramList.toArray();
        	logger.info("allot2 sql: {} \n args: {}", sql, JSON.toJSON(array));
            tmp = jt.query(sql, array, new SheetPubInfoRmp());//CodeSec未验证的SQL注入；CodeSec误报：2
            if (!tmp.isEmpty()) {
            	for (int i = tmp.size()-1; i >= 0; i--) {
                    sheetPubInfo = (SheetPubInfo) tmp.get(i);
                    result = workSheetAllot.allotToApprove(sheetPubInfo);
                    if (WorkSheetAllot.RST_NONE.equals(result)) {
                        tmp.remove(i);
                    }
                }
                all.addAll(tmp);
            }
        } catch (Exception e) {
        	logger.error("allot2 error: {}", e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
	private void allot3(String sql, List<String> paramList, List all) {
    	List tmp = null;
        SheetPubInfo sheetPubInfo = null;
        String result = "NONE";
        
        // 后台审核 待审核
        try {
        	Object[] array = paramList.toArray();
        	logger.info("allot3 sql: {} \n args: {}", sql, JSON.toJSON(array));
            tmp = jt.query(sql, array, new SheetPubInfoRmp());//CodeSec未验证的SQL注入；CodeSec误报：2
            if (!tmp.isEmpty()) {
            	for (int i = tmp.size()-1; i >= 0; i--) {
                    sheetPubInfo = (SheetPubInfo) tmp.get(i);
                    result = workSheetAllot.allotToVerify(sheetPubInfo);
                    if (WorkSheetAllot.RST_NONE.equals(result)) {
                        tmp.remove(i);
                    }
                }
                all.addAll(tmp);
            }
        } catch (Exception e) {
        	logger.error("allot3 error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 设置allotDDQSql
     * @param allotDDQSql 要设置的allotDDQSql。
     */
    public void setAllotDDQSql(String allotDDQSql) {
        this.allotDDQSql = allotDDQSql;
    }

    /**
     * 设置allotApvSql
     * @param allotApvSql 要设置的allotApvSql。
     */
    public void setAllotApvSql(String allotApvSql) {
        this.allotApvSql = allotApvSql;
    }

    /**
     * 设置allotFinApvSql
     * @param allotFinApvSql 要设置的allotFinApvSql。
     */
    public void setAllotFinApvSql(String allotFinApvSql) {
        this.allotFinApvSql = allotFinApvSql;
    }

    /**
     * 设置distillSql
     * @param distillSql 要设置的distillSql。
     */
    public void setDistillSql(String distillSql) {
        this.distillSql = distillSql;
    }

	public String getAllotDDQSql() {
		return allotDDQSql;
	}

	public String getAllotApvSql() {
		return allotApvSql;
	}

	public String getAllotFinApvSql() {
		return allotFinApvSql;
	}

	public String getDistillSql() {
		return distillSql;
	}
	
}
