/*
 * 文 件 名：DbgridCmpWorksheetImpl.java
 * 版    权：
 * 描    述：
 * 修 改 人：Administrator
 * 修改时间：2013-9-9
 * 修改内容：新增
 */
package com.timesontransfar.customservice.dbgridData.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridCmpWorksheet;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;

/**
 *  添加类的描述
 * 
 * @author Administrator
 * @version
 * @since
 */
@Component(value="cmpWorksheetImpl")
public class DbgridCmpWorksheetImpl implements IdbgridCmpWorksheet {
	@Autowired
    private IdbgridDataPub dbgridDataPub;

    private static String sqlQueryCmpWorkSheet =
            "SELECT r.RELA_GUID, r.COMPLAINT_WORKSHEET_ID, r.SERVICE_ORDER_ID, r.STATU, r.ASSIGN_TYPE,\n" +
                    "       t.TRANSACTIONID, w.COMPLAINT_PHONE, w.ASK_SOURCE_SRL,w.ASK_SOURCE_SRL_DESC,\n" +
                    "       a.ASSIGN_CODE, a.ASSIGN_NAME, a.RECEIVER_CODE, a.RECEIVER_NAME,r.SEC_FLAG\n" +
                    "   FROM CC_CMP_RELATION r,CC_CMP_TCPCONT t,CC_CMP_ASSIGN a,CC_CMP_WORKSHEET w\n" +
                    "  where t.rela_guid = r.rela_guid and t.oper_type = 'ASSIGN'\n" + 
                    "        and t.transactionid = a.transactionid and t.transactionid = w.transactionid";
 
            /*"SELECT r.RELA_GUID, r.complaint_worksheet_id,\n"
            + "       r.service_order_id, r.statu, r.assign_type,\n"
            + "       t.transactionid, w.complaint_phone,\n"
            + "       w.ask_source_srl,w.ask_source_srl_desc,\n"
            + "       a.assign_code, a.assign_name, a.receiver_code, a.receiver_name,\n"
            + "       s.order_statu,s.order_statu_desc,s.month_flag,s.cust_guid,s.region_id,s.region_name"
            + "  FROM cc_cmp_relation  r, cc_cmp_tcpcont   t, cc_cmp_assign    a,\n"
            + "       cc_cmp_worksheet w, cc_service_order_ask s where 1 = 1\n"
            + "   and t.rela_guid = r.rela_guid and t.oper_type = 'ASSIGN'"
            + "   and t.transactionid = a.transactionid and t.transactionid = w.transactionid"
            + "   and r.service_order_id = s.service_order_id \n";*/

    public GridDataInfo getCmpWorksheet(int begin, String strWhere) {
        String sql = sqlQueryCmpWorkSheet + strWhere;
        GridDataInfo gridData = dbgridDataPub.getResult(sql, begin, "",
                DbgridStatic.GRID_FUNID_CMP_CWORKSHEET);
        return gridData;
    }

    /**
     * 投诉单&部门内&处理中<br>
     * (咨询|建议|表扬|增值退订)&(派单|处理|审批|审核)&处理中
     */
     static String sqlQueryWorkSheet = "SELECT S.SERVICE_ORDER_ID, S.SERVICE_TYPE, S.SERVICE_TYPE_DESC, S.REGION_ID, S.REGION_NAME,\n"
            + "       W.WORK_SHEET_ID, S.PROD_NUM, S.ACCEPT_CHANNEL_ID, S.ACCEPT_CHANNEL_DESC, S.ORDER_STATU,\n"
            + "       S.MONTH_FLAG, S.CUST_GUID, A.APPLY_STAFF, A.APPLY_STAFF_NAME, A.APPLY_ORG, A.APPLY_ORG_NAME,\n"
            + "       DATE_FORMAT(A.AUD_DATE, '%Y-%m-%d %H:%i:%s') AUD_DATE, A.APPLY_GUID,W.SHEET_TYPE,A.APPLY_AUD_STATU\n"
            + "  FROM CC_SERVICE_ORDER_ASK S, CC_WORK_SHEET W, CC_SHEET_STATU_APPLY A\n"
            + " WHERE A.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID\n"
            + "   AND A.WORKSHEET_ID = W.WORK_SHEET_ID\n"
            + "   AND A.APPLY_AUD_STATU IN (0,1) AND A.APPLY_TYPE = 2";

    public GridDataInfo getWorksheet(int begin, String strWhere) {
        String sql = sqlQueryWorkSheet + strWhere;
        GridDataInfo gridData = dbgridDataPub.getResult(sql, begin, "",
                DbgridStatic.GRID_FUNID_CMP_PWORKSHEET);
        return gridData;
    }

    /**
     * 设置dbgridDataPub
     * 
     * @param dbgridDataPub
     *            要设置的dbgridDataPub。
     */
    public void setDbgridDataPub(IdbgridDataPub dbgridDataPub) {
        this.dbgridDataPub = dbgridDataPub;
    }
}
