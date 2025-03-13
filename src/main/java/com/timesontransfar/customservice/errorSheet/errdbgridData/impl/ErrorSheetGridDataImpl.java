package com.timesontransfar.customservice.errorSheet.errdbgridData.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.errorSheet.errdbgridData.IerrorSheetGridData;
/**
 * @author 孙力
 */
@Component("ErrorSheetGridDataImpl")
@SuppressWarnings("rawtypes")
public class ErrorSheetGridDataImpl implements IerrorSheetGridData {

	@Autowired
	private ISystemAuthorization systemAuthorization;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private PubFunc pubFunc;
	
    /**
	 * 根据员工ID获取该员工未阅读消息
	 * @param staffId
	 * @return 
	 */
	public GridDataInfo getNotReadedList(int begion,String strWhere){		
		String sql =" SELECT GUID,STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,TYPE_ID,TYPE_NAME," +
		"MESSAGE_CONTENT,decode(READED,0,'否','是') readed, TO_CHAR(CREAT_DATE,'YYYY-MM-DD HH24:MI:SS')CREAT_DATE," +
		" TO_CHAR(READED_DATE,'YYYY-MM-DD HH24:MI:SS')READED_DATE,URL_ADDR " +
		"FROM CC_MESSAGE_PROMPT where 1 = 1  ";
		if(!"".equals(strWhere)){
			sql=sql+strWhere;
		}	
		
		return this.dbgridDataPub.getResult(sql, begion,
				" ORDER BY CREAT_DATE ASC",
				DbgridStatic.GRID_FUNID_MSG_PROMPT_LIST);
	}	
	/**
	 * 取得错单当前列表数据
	 * 
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrSheetList(int begion,String strWhere) {//取消权限
		String strSql = 
			" SELECT W.SERVICE_ORDER_ID,\n" +
			"       W.WORK_SHEET_ID,\n" + 
			"       W.DEAL_DESC,\n" +                // 产品号码
			"       date_format(W.STATU_DATE,'%Y-%m-%d %H:%i:%s') AS STATU_DATE,\n" +  // 受理时间
			"       (case W.SHEET_STATU\n" +
			"       when 3000037 then date_format( W.CREAT_DATE,'%Y-%m-%d %H:%i:%s') \n" +
			"       else ( SELECT date_format( WW.CREAT_DATE,'%Y-%m-%d %H:%i:%s') FROM CC_WORK_SHEET WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID ) end) CREAT_DATE,\n" + // 错单判定时间
			"       W.DEAL_CONTENT,\n" +             // 差错项
			"       (case W.SHEET_STATU\n"	+
			"       when 3000037 then W.SAVE_DEALCONTEN else (SELECT WW.SAVE_DEALCONTEN FROM CC_WORK_SHEET WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID ) end) SAVE_DEALCONTEN,\n" + // 改进建议
			"       (case W.SHEET_STATU when 3000037 then '' else   W.SAVE_DEALCONTEN end) DEAL_REQUIRE,\n" +             // 申诉理由
			"       W.MONTH_FLAG,\n" + 			     // 月分区
			"       W.RECEIVE_ORG_ID,\n" +           // 受理部门
			"       W.RECEIVE_ORG_NAME,\n" +         // 受理部门
			"       W.RECEIVE_STAFF,\n" +            // 受理员工
			"       W.RECEIVE_STAFF_NAME,\n" +       // 受理员工
			"       W.RETURN_ORG_ID,\n" +            // 判定部门
			"       W.RETURN_ORG_NAME,\n" +          // 判定部门
			"       W.RETURN_STAFF,\n" +             // 判定员工
			"       W.RETURN_STAFF_NAME,\n" +        // 判定员工
			"       W.REGION_ID,\n" +                // 受理地域
			"       W.REGION_NAME,\n" +              // 受理地域
			"       W.DISTILL_DATE,\n" +             // 错单确认时间
			"       W.RESPOND_DATE,\n" +             // 申诉处理时间
			"       W.SHEET_STATU,\n" +              // 工单状态
			"       W.SHEET_STATU_DESC,\n" +         // 工单状态
			"       W.SHEET_TYPE,\n" +               // 工单类型
			"       W.SHEET_TYPE_DESC \n," +         // 工单类型
			"       W.SHEET_PRI_VALUE " +            // 受理单版本号  
			"  FROM CC_WORK_SHEET W \n" + 
			" WHERE 1 = 1 \n" + 
			"   AND W.SHEET_STATU IN (3000038, 3000037) ";        // 错单
		
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}		
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ALLERROR_SHEET );	
	}
	
	/**
	 * 取得错单历史列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrSheetHisList(int begion,String strWhere){	
		String strSql = 
		    " SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,\n" +
		    "       W.DEAL_DESC,\n" +                // 产品号码
		    "       DATE_FORMAT(W.STATU_DATE, '%Y-%m-%d %H:%i:%s') AS STATU_DATE,\n" +  // 受理时间
		    "       DATE_FORMAT (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN W.CREAT_DATE ELSE (SELECT WW.CREAT_DATE FROM CC_WORK_SHEET_HIS WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID) END) ELSE W.CREAT_DATE END,'%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +  // 错单判定时间
		    "       W.DEAL_CONTENT,\n" +             // 差错项
		    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN W.SAVE_DEALCONTEN ELSE (SELECT WW.SAVE_DEALCONTEN FROM CC_WORK_SHEET_HIS WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID) END ) ELSE W.SAVE_DEALCONTEN END)SAVE_DEALCONTEN,\n" +          // 改进建议
		    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN '' ELSE W.SAVE_DEALCONTEN END) ELSE W.DEAL_REQUIRE END)DEAL_REQUIRE,\n" +             // 申诉理由
		    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN '' ELSE W.DEAL_REQUIRE END ) ELSE '' END)SUREDMSG,\n" +             // 申诉内容的审批意见
		    "       W.MONTH_FLAG,\n" +               // 月分区
		    "       W.RECEIVE_ORG_ID,\n" +           // 受理部门
		    "       W.RECEIVE_ORG_NAME,\n" +         // 受理部门
		    "       W.RECEIVE_STAFF,\n" +            // 受理员工
		    "       W.RECEIVE_STAFF_NAME,\n" +       // 受理员工
		    "       W.RETURN_ORG_ID,\n" +            // 判定部门
		    "       W.RETURN_ORG_NAME,\n" +          // 判定部门
		    "       W.RETURN_STAFF,\n" +             // 判定员工
		    "       W.RETURN_STAFF_NAME,\n" +        // 判定员工
		    "       W.REGION_ID,\n" +                // 受理地域
		    "       W.REGION_NAME,\n" +              // 受理地域
		    "       DATE_FORMAT((CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN W.RESPOND_DATE ELSE W.CREAT_DATE END) ELSE  W.DISTILL_DATE END),'%Y-%m-%d %H:%i:%s') DISTILL_DATE,\n" +  // 错单确认时间
		    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN '' ELSE (DATE_FORMAT(W.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')) END) ELSE (DATE_FORMAT(W.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')) END) RESPOND_DATE,\n" + // 申诉处理时间
		    "       W.SHEET_STATU,\n" +              // 工单状态
		    "       W.SHEET_STATU_DESC,\n" +         // 工单状态
		    "       W.SHEET_TYPE,\n" +               // 工单类型
		    "       W.SHEET_TYPE_DESC, \n" +          // 工单类型
		    "       W.SHEET_PRI_VALUE " +            // 受理单版本号
			"  FROM CC_WORK_SHEET_HIS W \n" + 
			" WHERE W.REPORT_NUM = 0 \n" +
			"   AND W.SHEET_TYPE IN (3000035,3000036) AND W.SHEET_STATU = 700000047 ";
	
		if(!"".equals(strWhere)){
			strSql = strSql + strWhere;
		}		
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ALLERROR_SHEET_FINISH);	
	}
	
	/**
	 * 取得错单确认列表数据
	 * 
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrAcceptSheet(int begion,String strWhere) {	
	    TsmStaff staff = pubFunc.getLogonStaff();
		String strSql = "SELECT W.SERVICE_ORDER_ID,\n"+
		                "       W.WORK_SHEET_ID,\n"+
		                "       W.DEAL_DESC,\n"+
		                "       W.REGION_ID,\n"+
		                "       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n"+
		                "       DATE_FORMAT(W.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE,\n"+
		                "       DATE_FORMAT(W.STATU_DATE, '%Y-%m-%d %H:%i:%s') AS STATU_DATE,\n"+
		                "       DATE_FORMAT(W.DISTILL_DATE, '%Y-%m-%d %H:%i:%s') AS DISTILL_DATE,\n"+
		                "       W.DEAL_REQUIRE,\n"+
		                "       W.DEAL_CONTENT,\n"+
		                "       W.SAVE_DEALCONTEN,\n"+
		                "       W.RECEIVE_STAFF,\n"+
		                "       W.RECEIVE_STAFF_NAME,\n"+
		                "       W.RECEIVE_ORG_ID,\n"+
		                "       W.RECEIVE_ORG_NAME,\n"+
		                "       W.DEAL_STAFF,\n"+
		                "       W.DEAL_STAFF_NAME,\n"+
		                "       W.DEAL_ORG_ID,\n"+
		                "       W.DEAL_ORG_NAME,\n"+
		                "       W.RETURN_STAFF,\n"+
		                "       W.RETURN_STAFF_NAME,\n"+
		                "       W.RETURN_ORG_ID,\n"+
		                "       W.RETURN_ORG_NAME,\n" +
		                "       W.MONTH_FLAG \n"+
		                "  FROM CC_WORK_SHEET W \n"+
		                " WHERE W.SHEET_TYPE = " + StaticData.SHEET_TYPE_ERROR + " \n" +
		                "   AND W.RECEIVE_STAFF = " + staff.getId() + " \n" +
		                "   AND W.SHEET_STATU = " + StaticData.WKST_VERIFY + " \n";
		
		if(!"".equals(strWhere)) {
			strSql = strSql + strWhere;
		}
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ERROR_SHEET_ACCEPT);
	}
	
	/**
	 * 取得错单申诉处理列表数据
	 * 
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GridDataInfo getErrAuditSheet(int begion,String strWhere) {
		String strSql = "SELECT W.SERVICE_ORDER_ID,\n"+
                "       W.WORK_SHEET_ID,\n"+
                "       W.DEAL_DESC,\n"+
                "       W.REGION_ID,\n"+
                "       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n"+
                "       DATE_FORMAT(W.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE,\n"+
                "       DATE_FORMAT(W.STATU_DATE, '%Y-%m-%d %H:%i:%s') AS STATU_DATE,\n"+
                "       DATE_FORMAT(W.DISTILL_DATE, '%Y-%m-%d %H:%i:%s') AS DISTILL_DATE,\n"+
                "       (SELECT WW.SAVE_DEALCONTEN FROM CC_WORK_SHEET WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID) DEAL_REQUIRE,\n"+ //改进建议
                "       W.DEAL_CONTENT,\n"+ //差错项
                "       W.SAVE_DEALCONTEN,\n"+ //申诉理由
                "       W.RECEIVE_STAFF,\n"+
                "       W.RECEIVE_STAFF_NAME,\n"+
                "       W.RECEIVE_ORG_ID,\n"+
                "       W.RECEIVE_ORG_NAME,\n"+
                "       W.DEAL_STAFF,\n"+
                "       W.DEAL_STAFF_NAME,\n"+
                "       W.DEAL_ORG_ID,\n"+
                "       W.DEAL_ORG_NAME,\n"+
                "       W.RETURN_STAFF,\n"+
                "       W.RETURN_STAFF_NAME,\n"+
                "       W.RETURN_ORG_ID,\n"+
                "       W.RETURN_ORG_NAME,\n" +
                "       W.MONTH_FLAG \n"+
                "  FROM CC_WORK_SHEET W \n"+
                " WHERE W.SHEET_TYPE = " + StaticData.SHEET_TYPE_ERROR + " \n" +
                "   AND W.SHEET_STATU = " + StaticData.WKST_APPEAL + " \n";
		
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018312");//错单实体
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ERROR_SHEET_AUDIT);
	}

	public IdbgridDataPub getDbgridDataPub() {
		return dbgridDataPub;
	}

	public void setDbgridDataPub(IdbgridDataPub dbgridDataPub) {
		this.dbgridDataPub = dbgridDataPub;
	}

	public ISystemAuthorization getSystemAuthorization() {
		return systemAuthorization;
	}

	public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}

	public PubFunc getPubFunc() {
        return pubFunc;
    }
    public void setPubFunc(PubFunc pubFunc) {
        this.pubFunc = pubFunc;
    }
	@Override
	public GridDataInfo getErrAllSheetList(int begion, String strWhere, String flag) {
		String strSql = 
				" SELECT W.SERVICE_ORDER_ID,\n" +
				"       W.WORK_SHEET_ID,\n" + 
				"       W.DEAL_DESC,\n" +                // 产品号码
				"       DATE_FORMAT(W.STATU_DATE, '%Y-%m-%d %H:%i:%s') AS STATU_DATE,\n" +  // 受理时间
				"       (CASE W.SHEET_STATU WHEN 3000037 THEN DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') ELSE ( SELECT DATE_FORMAT ( WW.CREAT_DATE, '%Y-%m-%d %H:%i:%s' ) FROM CC_WORK_SHEET WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID ) END) CREAT_DATE,\n" + // 错单判定时间
				"       W.DEAL_CONTENT,\n" +             // 差错项
				"       (CASE W.SHEET_STATU WHEN 3000037 THEN W.SAVE_DEALCONTEN ELSE ( SELECT WW.SAVE_DEALCONTEN FROM CC_WORK_SHEET WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID ) END) SAVE_DEALCONTEN,\n"+// 改进建议
				"       (CASE  W.SHEET_STATU WHEN 3000037 THEN '' ELSE W.SAVE_DEALCONTEN END) DEAL_REQUIRE,\n" +             // 申诉理由
				"       W.MONTH_FLAG,\n" + 			     // 月分区
				"       W.RECEIVE_ORG_ID,\n" +           // 受理部门
				"       W.RECEIVE_ORG_NAME,\n" +         // 受理部门
				"       W.RECEIVE_STAFF,\n" +            // 受理员工
				"       W.RECEIVE_STAFF_NAME,\n" +       // 受理员工
				"       W.RETURN_ORG_ID,\n" +            // 判定部门
				"       W.RETURN_ORG_NAME,\n" +          // 判定部门
				"       W.RETURN_STAFF,\n" +             // 判定员工
				"       W.RETURN_STAFF_NAME,\n" +        // 判定员工
				"       W.REGION_ID,\n" +                // 受理地域
				"       W.REGION_NAME,\n" +              // 受理地域
				"       W.DISTILL_DATE,\n" +             // 错单确认时间
				"       W.RESPOND_DATE,\n" +             // 申诉处理时间
				"       W.SHEET_STATU,\n" +              // 工单状态
				"       W.SHEET_STATU_DESC,\n" +         // 工单状态
				"       W.SHEET_TYPE,\n" +               // 工单类型
				"       W.SHEET_TYPE_DESC \n," +         // 工单类型
				"       W.SHEET_PRI_VALUE " +            // 受理单版本号  
				"  FROM CC_WORK_SHEET W \n" + 
				" WHERE 1 = 1 \n" + 
				"   AND W.SHEET_STATU IN (3000038, 3000037) ";        // 错单
			
			if(!"".equals(strWhere)) {
				strSql=strSql+strWhere;
			}
			if("all".equals(flag)) {
				return this.dbgridDataPub.getAllResult(strSql, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ALLERROR_SHEET );	
			}else {
				return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ALLERROR_SHEET );	
			}
	}
	
	@Override
	public GridDataInfo getErrAllSheetHisList(int begion, String strWhere, String flag) {
		String strSql = 
			    " SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,\n" +
			    "       W.DEAL_DESC,\n" +                // 产品号码
			    "       DATE_FORMAT(W.STATU_DATE, '%Y-%m-%d %H:%i:%s') AS STATU_DATE,\n" +  // 受理时间
			    "       DATE_FORMAT(CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN W.CREAT_DATE ELSE (SELECT WW.CREAT_DATE FROM CC_WORK_SHEET_HIS WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID) END) ELSE W.CREAT_DATE END,'%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +  // 错单判定时间
			    "       W.DEAL_CONTENT,\n" +             // 差错项
			    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN W.SAVE_DEALCONTEN ELSE (SELECT WW.SAVE_DEALCONTEN FROM CC_WORK_SHEET_HIS WW WHERE WW.WORK_SHEET_ID = W.SOURCE_SHEET_ID) END) ELSE W.SAVE_DEALCONTEN END)SAVE_DEALCONTEN,\n" +          // 改进建议
			    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN '' ELSE W.SAVE_DEALCONTEN END) ELSE W.DEAL_REQUIRE END)DEAL_REQUIRE,\n" +             // 申诉理由
			    "       (CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN '' ELSE W.DEAL_REQUIRE END ) ELSE '' END )SUREDMSG,\n" +             // 申诉内容的审批意见
			    "       W.MONTH_FLAG,\n" +               // 月分区
			    "       W.RECEIVE_ORG_ID,\n" +           // 受理部门
			    "       W.RECEIVE_ORG_NAME,\n" +         // 受理部门
			    "       W.RECEIVE_STAFF,\n" +            // 受理员工
			    "       W.RECEIVE_STAFF_NAME,\n" +       // 受理员工
			    "       W.RETURN_ORG_ID,\n" +            // 判定部门
			    "       W.RETURN_ORG_NAME,\n" +          // 判定部门
			    "       W.RETURN_STAFF,\n" +             // 判定员工
			    "       W.RETURN_STAFF_NAME,\n" +        // 判定员工
			    "       W.REGION_ID,\n" +                // 受理地域
			    "       W.REGION_NAME,\n" +              // 受理地域
			    "       DATE_FORMAT((CASE W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN W.RESPOND_DATE ELSE W.CREAT_DATE END) ELSE W.DISTILL_DATE END),'%Y-%m-%d %H:%i:%s') DISTILL_DATE,\n" +  // 错单确认时间
			    "       (CASE  W.HOME_SHEET WHEN 3 THEN (CASE W.DEAL_STAFF_NAME WHEN NULL THEN '' ELSE (DATE_FORMAT(W.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')) END) ELSE ( DATE_FORMAT(W.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')) END) RESPOND_DATE,\n" + // 申诉处理时间
			    "       W.SHEET_STATU,\n" +              // 工单状态
			    "       W.SHEET_STATU_DESC,\n" +         // 工单状态
			    "       W.SHEET_TYPE,\n" +               // 工单类型
			    "       W.SHEET_TYPE_DESC, \n" +          // 工单类型
			    "       W.SHEET_PRI_VALUE " +            // 受理单版本号
				"  FROM CC_WORK_SHEET_HIS W \n" + 
				" WHERE W.SHEET_STATU = 700000047 \n" +
				"   AND W.SHEET_TYPE IN (3000035,3000036) AND W.REPORT_NUM = 0 ";
		
			if(!"".equals(strWhere)){
				strSql = strSql + strWhere;
			}		
			if("all".equals(flag)) {
				return this.dbgridDataPub.getAllResult(strSql, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ALLERROR_SHEET );	
			}else {
				return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_ALLERROR_SHEET );	
			}	
	}
}
