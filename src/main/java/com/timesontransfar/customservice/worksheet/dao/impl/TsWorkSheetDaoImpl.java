/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.SheetReadRecordInfo;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetDealType;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyRmp;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TsWorkSheetDaoImpl implements ItsWorkSheetDao {
	private static final Logger log = LoggerFactory.getLogger(TsWorkSheetDaoImpl.class);
	
	@Autowired
	private PubFunc pubFun;
	
	@Autowired
	private JdbcTemplate jt;

	private ISystemAuthorization systemAuthorization;
	
	@Autowired
	private PortalInterfaceFeign portalFeign;
	
	@Autowired
	private OrderRefundService refundService;
	
	private String saveDealTypeSql;//保存处理类型
	private String queryDealTypeSql;
	private String deleOrderDealTyPeSql;//根据定单号，删除当前表处理类型
	
	private String deleteDTBySheetid;
	
	private String saveDealTypeHisSql;//把当前表的数据迁移到历史表中
	
	private String saveRetVisitResultSql;//保存回访结果
	private String deleOrdereRetVisitResultSql;//根据定单号，删除当前表的记录
	
	/**
	 * CC_SHEET_STATU_APPLY<br>
	 * 保存提出申请挂起,释放的工单
	 */
	private String saveSheetApplySql;
	private String saveSheetApplyHisSql;//保存提出申请挂起,释放的工单
	private String getSheetApplySql;//得到工单号所有申请信息
	private String getSheetApplyHisSql;//得到工单号历史所有申请信息
	private String updateSheetApplySql;//更新审批状态
	private String saveSheetReadSql;//保存工单阅读记录

	/**
	 * 投诉保存处理类型
	 * @param bean 
	 * @return 保存数
	 */
	public int saveSheetDealType(TsSheetDealType bean) {
		int size = 0;
		String strSql = this.saveDealTypeSql;
		size = this.jt.update(strSql,
				StringUtils.defaultIfEmpty(bean.getDealTypeId(),null),
				StringUtils.defaultIfEmpty(bean.getOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getWorkSheetId(),null),
				StringUtils.defaultIfEmpty(bean.getDealType(),null),
				StringUtils.defaultIfEmpty(bean.getDealTypeDesc(),null),
				bean.getDealId(),
				StringUtils.defaultIfEmpty(bean.getDealDesc(),null),
				StringUtils.defaultIfEmpty(bean.getDealContent(),null),
				bean.getMonth(),
                bean.getUpgradeIncline()
		);
		if(log.isDebugEnabled()) {
			log.debug("新保存投诉处理类型条数为：-->"+size);
		}
		return size;
	}
	
	public int querySheetDealType(TsSheetDealType bean) {
		int size = 0;
		String strSql = this.queryDealTypeSql;
		size = this.jt.queryForObject(strSql,new Object[]{
				bean.getWorkSheetId(),
				bean.getDealType(),
				bean.getDealTypeDesc()
		},Integer.class);
		if(log.isDebugEnabled()) {
			log.debug("新保存投诉处理类型条数为：-->{}", size);
		}
		return size;
	}
	
	/**
	 * 根据定单号删除当前表中处理类型
	 * @param orderId 定单号
	 * @param month 月分区
	 * @return 删除的数目
	 */
	public int deleteOrderDealType(String orderId,Integer month) {
		int size = 0;
		String strSql = this.deleOrderDealTyPeSql;
		size = this.jt.update(strSql, orderId);
		if(log.isDebugEnabled()) {
			log.debug("根据定单号："+orderId+"删除了"+size+"条处理类型记录。");
		}
		return size;
	}
	
	public int deleteDealTypeBySheetid(String sheetId){
		return jt.update(deleteDTBySheetid, sheetId);
	}
	/**
	 * 根据定号，月分区，把当前表中的数据迁移到历史中
	 * @param orderId 定单号
	 * @param month 月分区
	 * @return 保存数
	 */
	public int saveSheetDealTypeHis(String orderId,int regionid) {
		int size = 0;
		String strSql = this.saveDealTypeHisSql;
		size = this.jt.update(strSql, orderId);
		if(log.isDebugEnabled()) {
			log.debug("根据定单号："+orderId+"保存了"+size+"条处理类型记录到历史表中。");
		}		
		return size;
	}
	/**
	 * 保存投诉回访客户内容
	 * @param retVisitResult
	 * @return 
	 */
	public int saveResVisitResult(RetVisitResult retVisitResult) {
		int coutn = jt.update(this.saveRetVisitResultSql,
				StringUtils.defaultIfEmpty(retVisitResult.getWordSheetId(),null),
				StringUtils.defaultIfEmpty(retVisitResult.getDealOrgName(),null),
				StringUtils.defaultIfEmpty(retVisitResult.getDealOrgId(),null),
				retVisitResult.getSatisfyDegree(),
				StringUtils.defaultIfEmpty(retVisitResult.getSatisfyDegreeDesc(),null),
				retVisitResult.getAppealType(),
				StringUtils.defaultIfEmpty(retVisitResult.getAppealTypeDesc(),null),
				StringUtils.defaultIfEmpty(retVisitResult.getRetVisitContent(),null),
				StringUtils.defaultIfEmpty(retVisitResult.getConclusionDesc(),null),
				retVisitResult.getConclusionId(),
				retVisitResult.getContactFailNum(),
				retVisitResult.getRegionId(),
				StringUtils.defaultIfEmpty(retVisitResult.getServOrderId(),null),
				retVisitResult.getMonth(),
				retVisitResult.getAskDate()				
		);
		if(log.isDebugEnabled()) {
			log.debug("新保存投诉处理类型条数为：-->"+coutn);
		}		
		return coutn;
	}

	/**
	 * 定单竣工的时候，从当前表中删除客户回访
	 * @param orderId 定单号
	 * @param month 月分区
	 * @return 删除的记录数
	 */
	public int deleOrderResVisitResult(String orderId,Integer month) {
		int size = 0;
		size = jt.update(this.deleOrdereRetVisitResultSql,
				orderId,
				month
		);
		if(log.isDebugEnabled()) {
			log.debug("根据定单号："+orderId+"删除了"+size+"条客户回访记录。");
		}
		return size;
	}
	/**
	 * 工单挂起,释放提出申请
	 * @param sheetApply 申请工单对象
	 * @return
	 */
	public int saveSheetApply(WorkSheetStatuApplyInfo sheetApply,boolean boo) {
		int size = 0;
		if(boo) {
			size = this.jt.update(this.saveSheetApplySql,
					StringUtils.defaultIfEmpty(sheetApply.getApplyGuid(),null),
					StringUtils.defaultIfEmpty(sheetApply.getOrderId(),null),
					StringUtils.defaultIfEmpty(sheetApply.getSheetId(),null),
					StringUtils.defaultIfEmpty(sheetApply.getApplyOrg(),null),
					StringUtils.defaultIfEmpty(sheetApply.getApplyOrgName(),null),
					sheetApply.getApplyStaff(),
					StringUtils.defaultIfEmpty(sheetApply.getApplyStaffName(),null),
					StringUtils.defaultIfEmpty(sheetApply.getApplyReason(),null),
					sheetApply.getApplyStatu(),
					StringUtils.defaultIfEmpty(sheetApply.getAudResult(),null),
					StringUtils.defaultIfEmpty(sheetApply.getAudOrg(),null),
					StringUtils.defaultIfEmpty(sheetApply.getAudOrgName(),null),
					sheetApply.getAudStaff(),
					StringUtils.defaultIfEmpty(sheetApply.getAudStaffName(),null),
					sheetApply.getMonth(),
					sheetApply.getApplyType()
			);
		} else {
			size = this.jt.update(this.saveSheetApplyHisSql,
					sheetApply.getOrderId(),
					sheetApply.getMonth()
			);
		}
		if(log.isDebugEnabled()) {
			log.debug("保存工单申请记录:"+size+"条");
		}
		return size;
	}
	/**
	 * 根据定单号,删除与该定单关联的挂起,释放申请信息
	 * @param orderId
	 * @return
	 */
	public int deleteSheetApply(String orderId) {
		String strSql = "DELETE FROM CC_SHEET_STATU_APPLY WHERE SERVICE_ORDER_ID=?";
		int size = this.jt.update(strSql, orderId);
		if(log.isDebugEnabled()) {
			log.debug("根据定单好"+orderId+"删除挂起,释放申请记录有"+size+"条");
		}
		return size;
	}
	/**
	 * 根据工单号得到该工单所有的申请信息
	 * @param sheetId 工单号
	 * @boo true 查询当前
	 * @return
	 */
	public WorkSheetStatuApplyInfo[] getsheetApplyObj(String condition, boolean boo) {
		String strSql = "";
		if(boo) {
			strSql = this.getSheetApplySql;
		} else {
			strSql = this.getSheetApplyHisSql;
		}
		strSql = strSql + condition;

		List tmp = this.jt.query(strSql, new WorkSheetStatuApplyRmp());
		int size = tmp.size();
		if(size > 0) {
			WorkSheetStatuApplyInfo[] sheetApply = new WorkSheetStatuApplyInfo[size];
			for(int i = 0; i < size; i++) {
				sheetApply[i] = (WorkSheetStatuApplyInfo)tmp.get(i);
			}
			tmp.clear();
			tmp = null;
			return sheetApply;
		}
		return new WorkSheetStatuApplyInfo[0];
	}
	
	/**
	 * 更新申请审批状态
	 * @param sheetApply
	 * @return
	 */
	public int updateSheetApply(WorkSheetStatuApplyInfo sheetApply) {
/*		UPDATE CC_SHEET_STATU_APPLY T SET
		T.APPLY_AUD_STATU=?,T.AUD_RESULT=?,T.AUD_ORG=?,
		T.AUD_ORG_NAME=?,T.AUD_STAFF=?,T.AUD_STAFF_NAME=?,T.AUD_DATE=SYSDATE
		WHERE T.APPLY_GUID=?*/
		int size = 0;
		size = this.jt.update(this.updateSheetApplySql,
				sheetApply.getApplyStatu(),
				StringUtils.defaultIfEmpty(sheetApply.getAudResult(),null),
				StringUtils.defaultIfEmpty(sheetApply.getAudOrg(),null),
				StringUtils.defaultIfEmpty(sheetApply.getAudOrgName(),null),
				sheetApply.getAudStaff(),
				StringUtils.defaultIfEmpty(sheetApply.getAudStaffName(),null),
				StringUtils.defaultIfEmpty(sheetApply.getApplyGuid(),null)
		);
		return size;
	}
	
	public int updateSheetApplyStatu(String worksheetId, int oldStatu, int newStatu){
	    String sql="update CC_SHEET_STATU_APPLY s set s.apply_aud_statu = ? where s.apply_aud_statu= ? and s.worksheet_id = ?";
	    return jt.update(sql, newStatu, oldStatu, worksheetId);
	}
	/**
	 * @param orderId 定单号
	 * @param regionId 业务归属地
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public int getTsSheetDealCount(String orderId,int regionId,boolean boo) {		
		String strSql = "";
		if(boo) {			
			strSql = 
                "SELECT COUNT(1)\n" +
                "  FROM CC_WORKSHEET_DEAL_TYPE A, CC_WORK_SHEET W RIGHT JOIN CC_SHEET_TODISPATCH T ON T.WORK_SHEET_ID = W.WORK_SHEET_ID \n" +
                " WHERE W.SERVICE_ORDER_ID = ?\n" + 
                "   AND W.REGION_ID = ?\n" + 
                "   AND A.WORKSHEET_ID = W.WORK_SHEET_ID\n" +
                "   AND A.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID\n" + 
                "   AND W.SHEET_STATU IN (700000047, 720130036)\n" + 
                " ORDER BY W.RESPOND_DATE ASC";
		} else {
			strSql = 
                "SELECT COUNT(1)\n" +
                "  FROM CC_WORKSHEET_DEAL_TYPE_HIS A, CC_WORK_SHEET_HIS W RIGHT JOIN CC_SHEET_TODISPATCH_HIS T ON T.WORK_SHEET_ID = W.WORK_SHEET_ID \n" +
                " WHERE W.SERVICE_ORDER_ID = ?\n" + 
                "   AND W.REGION_ID = ?\n" + 
                "   AND A.WORKSHEET_ID = W.WORK_SHEET_ID\n" +
                "   AND A.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID\n" + 
                "   AND W.SHEET_STATU IN (700000047, 720130036)\n" + 
                " ORDER BY W.RESPOND_DATE ASC";
		}
		return this.jt.queryForObject(strSql, new Object[]{orderId, regionId}, Integer.class);
	}
	/**
	 * 查询投诉处理类型
	 * @param orderId 定单号
	 * @param regionId 业务归属地
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public List getTsSheetDealObj(String orderId,int regionId,boolean boo) {		
		String strSql = "";
		if(boo) {			
			strSql = 
                "SELECT A.*,\n" +
                "       W.TACHE_ID,\n" + 
                "       W.SHEET_TYPE,\n" + 
                "       W.SAVE_DEALCONTEN,\n" +
                "       W.WORK_SHEET_ID,\n" +
                "       W.SHEET_STATU,W.RECEIVE_ORG_NAME," +
                "       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
                "       DATE_FORMAT(W.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE,\n" +
                "       W.RETURN_STAFF_NAME,\n" + 
                "       W.RETURN_ORG_NAME,\n" + 
                "       W.DEAL_STAFF_NAME,\n" + 
                "       W.DEAL_ORG_NAME,\n" + 
                "       CONCAT(W.DEAL_REQUIRE , IF( T.REASON IS NULL, '', CONCAT('    转派原因：',T.REASON))) DEAL_REQUIRE,\n" +
                "       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE,\n" +
                "       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME,\n" +
                "       W.HANGUP_TIME_COUNT,\n" + 
                "       W.DEAL_LIMIT_TIME,\n" + 
                "       W.TACHE_DESC,\n" + 
                "       W.SOURCE_SHEET_ID,\n" +
                "       A.UPGRADE_INCLINE,"+
                "       (SELECT S.RELAPHONE from TSM_STAFF s WHERE  W.RETURN_STAFF = S.STAFF_ID) RELAPHONE," + 
                "       (SELECT 1 FROM cc_work_sheet o WHERE o.service_order_id = w.service_order_id AND o.source_sheet_id = w.work_sheet_id AND o.tache_id = 720130025 AND o.auto_visit_flag = 2 AND o.report_num = 1) AUTO_VISIT_FLAG\n" +
                "  FROM CC_WORKSHEET_DEAL_TYPE A, CC_WORK_SHEET W RIGHT JOIN CC_SHEET_TODISPATCH T ON T.WORK_SHEET_ID = W.WORK_SHEET_ID" +
                " WHERE W.SERVICE_ORDER_ID = ?\n" + 
                "   AND W.REGION_ID = ?\n" + 
                "   AND A.WORKSHEET_ID = W.WORK_SHEET_ID\n" +
                "   AND A.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID\n" + 
                "   AND (W.SHEET_STATU IN (700000047, 720130036) OR W.SHEET_TYPE IN (720130028,720130029)) " + 
                " ORDER BY W.CREAT_DATE ASC,a.deal_desc";
		} else {
			strSql = 
                "SELECT A.*,\n" +
                "       W.TACHE_ID,\n" + 
                "       W.SHEET_TYPE,\n" +
                "       W.WORK_SHEET_ID,\n" +
                "       W.SAVE_DEALCONTEN,\n" + 
                "       W.RETURN_STAFF_NAME,\n" + 
                "       W.RETURN_ORG_NAME,\n" + 
                "       W.DEAL_STAFF_NAME,\n" + 
                "       W.DEAL_ORG_NAME,\n" + 
                "       CONCAT(W.DEAL_REQUIRE , IF( T.REASON IS NULL, '', CONCAT('    转派原因：',T.REASON))) DEAL_REQUIRE,\n" +
                "       W.SHEET_STATU,W.RECEIVE_ORG_NAME," + 
                "       W.TACHE_DESC,\n" + 
                "       W.SOURCE_SHEET_ID,\n" + 
                "       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE,\n" +
                "       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME,\n" +
                "       W.HANGUP_TIME_COUNT,\n" + 
                "       W.DEAL_LIMIT_TIME,\n" + 
                "       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
                "       DATE_FORMAT(W.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE,\n" +
                "       A.UPGRADE_INCLINE,\n" + 
                "       (SELECT S.RELAPHONE from TSM_STAFF s WHERE  W.RETURN_STAFF = S.STAFF_ID) RELAPHONE," + 
                "       (SELECT 1 FROM cc_work_sheet_his o WHERE o.service_order_id = w.service_order_id AND o.source_sheet_id = w.work_sheet_id AND o.tache_id = 720130025 AND o.auto_visit_flag = 2 AND o.report_num = 1) AUTO_VISIT_FLAG\n" +
                "  FROM CC_WORKSHEET_DEAL_TYPE_HIS A, CC_WORK_SHEET_HIS W RIGHT JOIN CC_SHEET_TODISPATCH_HIS T ON T.WORK_SHEET_ID = W.WORK_SHEET_ID " +
                " WHERE W.SERVICE_ORDER_ID = ?\n" + 
                "   AND W.REGION_ID = ?\n" + 
                "   AND A.WORKSHEET_ID = W.WORK_SHEET_ID\n" +
                "   AND A.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID\n" + 
                "   AND (W.SHEET_STATU IN (700000047, 720130036) OR W.SHEET_TYPE IN (720130028,720130029)) "+
                " ORDER BY W.CREAT_DATE ASC,a.deal_desc";
		}
		return this.jt.queryForList(strSql, orderId, regionId);
	}
	/**
	 * 查询协查工单
	 * @param orderId 定单号
	 * @param regionId 业务归属地
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public List getXcSheetDealObj(String orderId,int regionId,boolean boo) {		
		String strSql = "";
		if(boo) {			
			strSql = 
                "SELECT W.WORK_SHEET_ID,\n" +
                "       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
                "       DATE_FORMAT(W.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE,\n" +
                "       W.DEAL_STAFF_NAME,\n" + 
                "       W.DEAL_ORG_NAME,\n" + 
                "       W.DEAL_REQUIRE,\n" + 
                "       W.DEAL_LIMIT_TIME,\n" + 
                "       W.RECEIVE_ORG_NAME,\n" + 
                "       W.DEAL_CONTENT,\n" + 
                "       W.SHEET_TYPE_DESC\n" + 
                "  FROM CC_WORK_SHEET W\n" + 
                " WHERE W.SERVICE_ORDER_ID = ?\n" + 
                "   AND W.REGION_ID = ?\n" + 
                "   AND W.SHEET_TYPE IN (720130028, 720130029)\n" + 
                " ORDER BY W.CREAT_DATE ASC";
		} else {
			strSql = 
                "SELECT W.WORK_SHEET_ID,\n" +
                "       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
                "       DATE_FORMAT(W.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE,\n" +
                "       W.DEAL_STAFF_NAME,\n" + 
                "       W.DEAL_ORG_NAME,\n" + 
                "       W.DEAL_REQUIRE,\n" + 
                "       W.DEAL_LIMIT_TIME,\n" + 
                "       W.RECEIVE_ORG_NAME,\n" + 
                "       W.DEAL_CONTENT,\n" + 
                "       W.SHEET_TYPE_DESC\n" + 
                "  FROM CC_WORK_SHEET_HIS W\n" + 
                " WHERE W.SERVICE_ORDER_ID = ?\n" + 
                "   AND W.REGION_ID = ?\n" + 
                "   AND W.SHEET_TYPE IN (720130028, 720130029)\n" + 
                " ORDER BY W.CREAT_DATE ASC";
		}
		return this.jt.queryForList(strSql, orderId, regionId);
	}
	/**
	 * 得到跟录音文件有关系的定单信息
	 * @param orderId 定单号
	 * @param regionId 地域
	 * @param boo true 得到当前 false 得到历史
	 * @return
	 */
	public List getRecordOrderInfo(String orderId,int regionId,boolean boo) {
		String orderSql = "";
		if(boo) {
			orderSql =	"SELECT A.CALL_SERIAL_NO,\n" +
			"       A.SOURCE_NUM,\n" + 
			"       A.ACCEPT_ORG_ID" + 
			"  FROM CC_SERVICE_ORDER_ASK A" + 
			" WHERE A.SERVICE_ORDER_ID = ?\n" + 
			"   AND A.REGION_ID = ?";
		} else {
			orderSql =	"SELECT A.CALL_SERIAL_NO,\n" +
			"       A.SOURCE_NUM,\n" + 
			"       A.ACCEPT_ORG_ID" + 
			"  FROM CC_SERVICE_ORDER_ASK_HIS A" + 
			" WHERE A.SERVICE_ORDER_ID = ?\n" + 
			"   AND A.REGION_ID = ?";
		}
		List tmp = this.jt.queryForList(orderSql,orderId,regionId);
		if(tmp.isEmpty()) {
			log.debug("没有查询到地域为：{} 定单号为：{}相关信息", regionId, orderId);
		}
		return tmp;
	}
	
	/**
	 * 得到录音FTP信息
	 * @param flowId 录音流水号
	 * @return
	 */
	public List getFtpFilesByFlowId(String flowId) {
		List tmp = portalFeign.getVoiceRecordFileByFlowid(flowId);
		if (tmp.isEmpty()) {
			log.info("jscsc_ct_pub t_qc_voice_record表中没有查询到flowId为：{}的记录", flowId);
		}
		return tmp;
	}
	
	public String getAreaId(String orgId) {
		String tmpId = pubFun.getAreaOrgId(orgId);//获取二三级部门ID用于区分省投、区域中心、分公司
		log.info("{} 所属大部门ID为：{}", orgId, tmpId);
		return this.getAreaCenter(tmpId);
	}
	
	private String getAreaCenter(String orgId) {
		String areaCenter = "";
		switch (orgId) {
			case "182":
		    case "285":
		    case "286":
		    case "287":
		    	areaCenter = "5";//南京
		    	break;
		    case "97":
			case "284":
			case "289":
			case "290":
			case "291":
			case "292":
			case "293":
			case "294":
				areaCenter = "4";//扬州
		    	break;
			case "288":
				areaCenter = "7";//苏州分公司
				break;
			default:
				areaCenter = "";//其他
				break;
		}
		log.info("getAreaCenter：{}", areaCenter);
		return areaCenter;
	}
	
	/**
	 * 得到录音文件的FTP服务器
	 * @param regionId FTP服务器唯一ID
	 * @return
	 */
	public List getRecordFtp(int regionId,String wasHost) {
		String strSql = "SELECT * FROM PUB_FTP_CONFIG R WHERE R.REGION_ID=? AND R.WAS_HOST=?";
		List tmp = this.jt.queryForList(strSql, regionId, wasHost);
		if(tmp.isEmpty()) {
			log.debug("没有找到ID号为：{}FTP服务器", regionId);
		}
		return tmp;
	}
	
	/**
	 * 根据客户号码和地域
	 * @param regionId 地域
	 * @param custNum 客户号码
	 * @return
	 */
	public List getTsEspeciallyCustInfo(int regionId,String custNum) {
		String strSql = "SELECT * FROM CC_ESPECIALLY_CUST A WHERE A.REGION_ID=? AND A.CUST_NUM=? AND A.STATU=1";
		List tmp = this.jt.queryForList(strSql, regionId, custNum);
		return tmp;
		
	}
	
	/**
	 * 得到特殊客户审核定性记录
	 * @param regionId 地域
	 * @param custNum 客户号码
	 * @return
	 */
	public List getAudQualitative(int regionId,String custNum) {
		String strSql = " SELECT A.SERVICE_ORDER_ID,\n" +
						" C.WORK_SHEET_ID,\n" + 
						" DATE_FORMAT(C.CREAT_DATA, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE ,\n" +
						" C.PIGEONHOLE_SORT_NAME,\n" + 
						" C.TS_REASON_NAME\n" + 
						"  FROM CC_SERVICE_ORDER_ASK_HIS A, CC_SHEET_QUALITATIVE_HIS C\n" + 
						" WHERE A.REGION_ID = C.REGION_ID\n" + 
						"   AND A.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n" + 
						"   AND A.ORDER_STATU in( 700000103,720130010)\n" + 
						"   AND A.REGION_ID = ? AND A.PROD_NUM = ?";
		return this.jt.queryForList(strSql, regionId, custNum);

	}
	/**
	 * 根据审核工单或审批工单得相关联的完成工单
	 * @param sheetId
	 * @param regionId
	 * @return
	 */
	public List getRelatingSheet(String sheetId,int regionId) {		 
		String strSql =	
			"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,W.RECEIVE_ORG_NAME," +
			" W.DEAL_STAFF,W.DEAL_STAFF_NAME,W.DEAL_ORG_ID,W.DEAL_ORG_NAME,W.MAIN_SHEET_FLAG,\n" +
			" W.RECEIVE_ORG_ID,W.RECEIVE_STAFF,W.RECEIVE_STAFF_NAME,W.MONTH_FLAG,W.REGION_ID,W.DEAL_LIMIT_TIME\n"+			
			" FROM CC_WORK_SHEET W,CC_WORKSHEET_ALLOT_RELA A\n" + 
			" WHERE W.WORK_SHEET_ID = A.DEAL_WORKSHEET_ID AND W.SHEET_STATU in(700000047,720130036)  AND W.TACHE_ID in(700000086,720130023) \n" + 
			" AND W.REGION_ID=? AND A.CHECK_WORKSHEET_ID=?";
		List tmp = this.jt.queryForList(strSql, regionId, sheetId);
		return tmp;

	}
	/**
	 * 得到该定单下是否有定性或归档的工单
	 * @param orderId
	 * @return
	 */
	public int getOrderDealData(String orderId) {
		String strSql = "SELECT COUNT(*) FROM CC_WORK_SHEET W WHERE W.TACHE_ID IN ("+StaticData.TACHE_PIGEONHOLE+", "+
		StaticData.TACHE_TSQUALITATIVE +") AND W.SERVICE_ORDER_ID=? ";
		return this.jt.queryForObject(strSql,new Object[]{orderId},Integer.class);
	}
	/**
	 * 得到工单状态审批数据
	 * @param sheetId 工单号
	 */
	public List getSheetStatuAud(String sheetId) {
		String strSql="SELECT * FROM CC_SHEET_STATU_APPLY A WHERE A.WORKSHEET_ID=?";
		List tmp = this.jt.queryForList(strSql,sheetId);
		return tmp;
	}
	
	/**
	 * 保存工单阅读记录
	 * @param bean 阅读记录对象
	 * @return
	 */
	public int saveSheetReadRecord(SheetReadRecordInfo bean) {
		return this.jt.update(this.saveSheetReadSql,
				bean.getSheetId(),
				bean.getReadStaffId(),
				bean.getReadStaffName(),
				bean.getReadDate(),
				bean.getReadDate()
		);
	}
	
	//===========================================================
	/**
	 * @return jt
	 */
	public JdbcTemplate getJt() {
		return jt;
	}
	/**
	 * @param jt 要设置的 jt
	 */
	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}
	/**
	 * @return systemAuthorization
	 */
	public ISystemAuthorization getSystemAuthorization() {
		return systemAuthorization;
	}
	/**
	 * @param systemAuthorization 要设置的 systemAuthorization
	 */
	public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}

	/**
	 * @return saveDealTypeSql
	 */
	public String getSaveDealTypeSql() {
		return saveDealTypeSql;
	}

	/**
	 * @param saveDealTypeSql 要设置的 saveDealTypeSql
	 */
	public void setSaveDealTypeSql(String saveDealTypeSql) {
		this.saveDealTypeSql = saveDealTypeSql;
	}

	/**
	 * @return deleOrderDealTyPeSql
	 */
	public String getDeleOrderDealTyPeSql() {
		return deleOrderDealTyPeSql;
	}

	/**
	 * @param deleOrderDealTyPeSql 要设置的 deleOrderDealTyPeSql
	 */
	public void setDeleOrderDealTyPeSql(String deleOrderDealTyPeSql) {
		this.deleOrderDealTyPeSql = deleOrderDealTyPeSql;
	}
	/**
	 * @return saveDealTypeHisSql
	 */
	public String getSaveDealTypeHisSql() {
		return saveDealTypeHisSql;
	}
	/**
	 * @param saveDealTypeHisSql 要设置的 saveDealTypeHisSql
	 */
	public void setSaveDealTypeHisSql(String saveDealTypeHisSql) {
		this.saveDealTypeHisSql = saveDealTypeHisSql;
	}
	/**
	 * @return saveRetVisitResultSql
	 */
	public String getSaveRetVisitResultSql() {
		return saveRetVisitResultSql;
	}
	/**
	 * @param saveRetVisitResultSql 要设置的 saveRetVisitResultSql
	 */
	public void setSaveRetVisitResultSql(String saveRetVisitResultSql) {
		this.saveRetVisitResultSql = saveRetVisitResultSql;
	}
	/**
	 * @return deleOrdereRetVisitResultSql
	 */
	public String getDeleOrdereRetVisitResultSql() {
		return deleOrdereRetVisitResultSql;
	}
	/**
	 * @param deleOrdereRetVisitResultSql 要设置的 deleOrdereRetVisitResultSql
	 */
	public void setDeleOrdereRetVisitResultSql(String deleOrdereRetVisitResultSql) {
		this.deleOrdereRetVisitResultSql = deleOrdereRetVisitResultSql;
	}
	/**
	 * @return saveSheetApplySql
	 */
	public String getSaveSheetApplySql() {
		return saveSheetApplySql;
	}
	/**
	 * @param saveSheetApplySql 要设置的 saveSheetApplySql
	 */
	public void setSaveSheetApplySql(String saveSheetApplySql) {
		this.saveSheetApplySql = saveSheetApplySql;
	}
	/**
	 * @return saveSheetApplyHisSql
	 */
	public String getSaveSheetApplyHisSql() {
		return saveSheetApplyHisSql;
	}
	/**
	 * @param saveSheetApplyHisSql 要设置的 saveSheetApplyHisSql
	 */
	public void setSaveSheetApplyHisSql(String saveSheetApplyHisSql) {
		this.saveSheetApplyHisSql = saveSheetApplyHisSql;
	}
	/**
	 * @return getSheetApplySql
	 */
	public String getGetSheetApplySql() {
		return getSheetApplySql;
	}
	/**
	 * @param getSheetApplySql 要设置的 getSheetApplySql
	 */
	public void setGetSheetApplySql(String getSheetApplySql) {
		this.getSheetApplySql = getSheetApplySql;
	}
	/**
	 * @return updateSheetApplySql
	 */
	public String getUpdateSheetApplySql() {
		return updateSheetApplySql;
	}
	/**
	 * @param updateSheetApplySql 要设置的 updateSheetApplySql
	 */
	public void setUpdateSheetApplySql(String updateSheetApplySql) {
		this.updateSheetApplySql = updateSheetApplySql;
	}
	/**
	 * @return getSheetApplyHisSql
	 */
	public String getGetSheetApplyHisSql() {
		return getSheetApplyHisSql;
	}
	/**
	 * @param getSheetApplyHisSql 要设置的 getSheetApplyHisSql
	 */
	public void setGetSheetApplyHisSql(String getSheetApplyHisSql) {
		this.getSheetApplyHisSql = getSheetApplyHisSql;
	}
	/**
	 * @return saveSheetReadSql
	 */
	public String getSaveSheetReadSql() {
		return saveSheetReadSql;
	}
	/**
	 * @param saveSheetReadSql 要设置的 saveSheetReadSql
	 */
	public void setSaveSheetReadSql(String saveSheetReadSql) {
		this.saveSheetReadSql = saveSheetReadSql;
	}

	public void setDeleteDTBySheetid(String deleteDTBySheetid) {
		this.deleteDTBySheetid = deleteDTBySheetid;
	}

	public String getQueryDealTypeSql() {
		return queryDealTypeSql;
	}

	public void setQueryDealTypeSql(String queryDealTypeSql) {
		this.queryDealTypeSql = queryDealTypeSql;
	}

	@Override
	public List getTsSheetDealObjNew(String orderId,boolean boo) {
		String strSql ="";
		if(boo) {
			strSql = 
"SELECT*FROM(SELECT DATE_FORMAT(A.LOCK_DATE,'%Y-%m-%d %H:%i:%s')TOP_DATE,A.WORK_SHEET_ID IN_SHEET_ID,A.TACHE_DESC,DATE_FORMAT(A.DISTILL_DATE,"
+ "'%Y-%m-%d %H:%i:%s')IN_DISTILL_DATE,IFNULL(A.DEAL_ORG_ID,A.RECEIVE_ORG_ID)IN_DEAL_ORG,IFNULL(A.DEAL_STAFF_NAME,A.RECEIVE_STAFF_NAME)IN_DEAL_STAFF,"
+ "DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')IN_DATE,IF(A.LOCK_FLAG=9,A.SHEET_STATU_DESC,A.DEAL_DESC)DEAL_DESC,CONCAT_WS('',A.DEAL_LIMIT_TIME,'小时')"
+ "DEAL_LIMIT_TIME,0 OVER_DATE_FLAG,DATE_FORMAT(IFNULL(A.SHEET_RECEIVE_DATE,IF(A.SHEET_TYPE IN(700001002,720130015),A.CREAT_DATE,A.LOCK_DATE)),"
+ "'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(B.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(IF(A.SHEET_STATU IN(700000046,720130035),"
+ "A.HANGUP_START_TIME,A.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,A.HANGUP_TIME_COUNT,B.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,"
+ "A.DEAL_LIMIT_TIME WLT,A.RECEIVE_ORG_DISPLAY,A.DEAL_CONTENT IN_CONTENT,''OUT_SHEET_ID,''SHEET_TYPE_DESC,''OUT_DISTILL_DATE,''OUT_DEAL_ORG,''"
+ "OUT_DEAL_STAFF,''OUT_DATE,''OUT_CONTENT,IF(A.DEAL_DESC IN('终定性重新派发工单','审核派单环节派单','审核环节重新派发工单','分派工单','部门转派'),1,2)SHEET_FLAG,IFNULL(A.RESPOND_DATE,"
+ "A.LOCK_DATE)ORDER_BY FROM CC_WORK_SHEET A,CC_SERVICE_ORDER_ASK B WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND(A.TACHE_ID IN(700000084,700000085,"
+ "700000086,720130021,720130023)OR(A.TACHE_ID=700000088 AND A.DEAL_DESC IN('审核环节重新派发工单','审核环节退单','审核环节重新派发工单'))OR(A.TACHE_ID=720130025 AND "
+ "A.DEAL_DESC='终定性重新派发工单'))AND A.LOCK_FLAG IN(2,9)AND A.SHEET_TYPE NOT IN(720130028,720130029)AND A.SERVICE_ORDER_ID=? UNION ALL SELECT DATE_FORMAT("
+ "A.CREAT_DATE,'%Y-%m-%d %H:%i:%s'),C.WORK_SHEET_ID,C.TACHE_DESC,DATE_FORMAT(C.DISTILL_DATE,'%Y-%m-%d %H:%i:%s'),A.RETURN_ORG_ID,A.RETURN_STAFF_NAME,"
+ "DATE_FORMAT(A.CREAT_DATE,'%Y-%m-%d %H:%i:%s'),IF(A.SHEET_TYPE=720130028,'省内协查派单','集团协查派单'),CONCAT_WS('',A.DEAL_LIMIT_TIME,'小时'),IF(A.RESPOND_DATE IS NULL AND "
+ "C.RESPOND_DATE IS NOT NULL,1,IF(A.RESPOND_DATE IS NOT NULL AND C.RESPOND_DATE IS NOT NULL AND A.RESPOND_DATE>C.RESPOND_DATE,1,0)),DATE_FORMAT("
+ "IFNULL(A.SHEET_RECEIVE_DATE,IF(A.SHEET_TYPE IN(700001002,720130015),A.CREAT_DATE,A.LOCK_DATE)),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(B.ACCEPT_DATE,"
+ "'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(IF(A.SHEET_STATU IN(700000046,720130035),A.HANGUP_START_TIME,A.RESPOND_DATE),'%Y-%m-%d %H:%i:%s'),"
+ "A.HANGUP_TIME_COUNT,B.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),A.DEAL_LIMIT_TIME,'',A.DEAL_REQUIRE,A.WORK_SHEET_ID,A.SHEET_TYPE_DESC,"
+ "DATE_FORMAT(A.DISTILL_DATE,'%Y-%m-%d %H:%i:%s'),IFNULL(A.DEAL_ORG_ID,A.RECEIVE_ORG_ID),IFNULL(A.DEAL_STAFF_NAME,A.RECEIVE_STAFF_NAME),DATE_FORMAT("
+ "A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s'),A.DEAL_CONTENT,3 PD_FLAG,A.CREAT_DATE FROM CC_WORK_SHEET A,CC_SERVICE_ORDER_ASK B,CC_WORK_SHEET C WHERE "
+ "A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND A.SOURCE_SHEET_ID=C.WORK_SHEET_ID AND A.SHEET_TYPE IN"
+ "(720130028,720130029)AND A.SERVICE_ORDER_ID=?)X ORDER BY ORDER_BY";
		}else {
			strSql = 
"SELECT*FROM(SELECT DATE_FORMAT(A.LOCK_DATE,'%Y-%m-%d %H:%i:%s')TOP_DATE,A.WORK_SHEET_ID IN_SHEET_ID,A.TACHE_DESC,DATE_FORMAT(A.DISTILL_DATE,"
+ "'%Y-%m-%d %H:%i:%s')IN_DISTILL_DATE,IFNULL(A.DEAL_ORG_ID,A.RECEIVE_ORG_ID)IN_DEAL_ORG,IFNULL(A.DEAL_STAFF_NAME,A.RECEIVE_STAFF_NAME)IN_DEAL_STAFF,"
+ "DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')IN_DATE,IF(A.LOCK_FLAG=9,A.SHEET_STATU_DESC,A.DEAL_DESC)DEAL_DESC,CONCAT_WS('',A.DEAL_LIMIT_TIME,'小时')"
+ "DEAL_LIMIT_TIME,0 OVER_DATE_FLAG,DATE_FORMAT(IFNULL(A.SHEET_RECEIVE_DATE,IF(A.SHEET_TYPE IN(700001002,720130015),A.CREAT_DATE,A.LOCK_DATE)),"
+ "'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(B.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(IF(A.SHEET_STATU IN(700000046,720130035),"
+ "A.HANGUP_START_TIME,A.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,A.HANGUP_TIME_COUNT,B.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,"
+ "A.DEAL_LIMIT_TIME WLT,A.RECEIVE_ORG_DISPLAY,A.DEAL_CONTENT IN_CONTENT,''OUT_SHEET_ID,''SHEET_TYPE_DESC,''OUT_DISTILL_DATE,''OUT_DEAL_ORG,''"
+ "OUT_DEAL_STAFF,''OUT_DATE,''OUT_CONTENT,IF(A.DEAL_DESC IN('终定性重新派发工单','审核派单环节派单','审核环节重新派发工单','分派工单','部门转派'),1,2)SHEET_FLAG,IFNULL(A.RESPOND_DATE,"
+ "A.LOCK_DATE)ORDER_BY FROM CC_WORK_SHEET_HIS A,CC_SERVICE_ORDER_ASK_HIS B WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND B.ORDER_STATU IN(700000103,"
+ "720130010)AND(A.TACHE_ID IN(700000084,700000085,700000086,720130021,720130023)OR(A.TACHE_ID=700000088 AND A.DEAL_DESC IN('审核环节重新派发工单','审核环节退单',"
+ "'审核环节重新派发工单'))OR(A.TACHE_ID=720130025 AND A.DEAL_DESC='终定性重新派发工单'))AND A.LOCK_FLAG IN(2,9)AND A.SHEET_TYPE NOT IN(720130028,720130029)"
+ "AND A.SERVICE_ORDER_ID=? UNION ALL SELECT DATE_FORMAT(A.CREAT_DATE,'%Y-%m-%d %H:%i:%s'),C.WORK_SHEET_ID,C.TACHE_DESC,DATE_FORMAT(C.DISTILL_DATE,"
+ "'%Y-%m-%d %H:%i:%s'),A.RETURN_ORG_ID,A.RETURN_STAFF_NAME,DATE_FORMAT(A.CREAT_DATE,'%Y-%m-%d %H:%i:%s'),IF(A.SHEET_TYPE=720130028,'省内协查派单',"
+ "'集团协查派单'),CONCAT_WS('',A.DEAL_LIMIT_TIME,'小时'),IF(A.RESPOND_DATE IS NULL AND C.RESPOND_DATE IS NOT NULL,1,IF(A.RESPOND_DATE IS NOT NULL AND C.RESPOND_DATE "
+ "IS NOT NULL AND A.RESPOND_DATE>C.RESPOND_DATE,1,0)),DATE_FORMAT(IFNULL(A.SHEET_RECEIVE_DATE,IF(A.SHEET_TYPE IN(700001002,720130015),A.CREAT_DATE,"
+ "A.LOCK_DATE)),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(B.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(IF(A.SHEET_STATU IN(700000046,720130035),"
+ "A.HANGUP_START_TIME,A.RESPOND_DATE),'%Y-%m-%d %H:%i:%s'),A.HANGUP_TIME_COUNT,B.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),"
+ "A.DEAL_LIMIT_TIME,'',A.DEAL_REQUIRE,A.WORK_SHEET_ID,A.SHEET_TYPE_DESC,DATE_FORMAT(A.DISTILL_DATE,'%Y-%m-%d %H:%i:%s'),IFNULL(A.DEAL_ORG_ID,"
+ "A.RECEIVE_ORG_ID),IFNULL(A.DEAL_STAFF_NAME,A.RECEIVE_STAFF_NAME),DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s'),A.DEAL_CONTENT,3 PD_FLAG,"
+ "A.CREAT_DATE FROM CC_WORK_SHEET_HIS A,CC_SERVICE_ORDER_ASK_HIS B,CC_WORK_SHEET_HIS C WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND "
+ "A.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND A.SOURCE_SHEET_ID=C.WORK_SHEET_ID AND B.ORDER_STATU IN(700000103,720130010)AND A.SHEET_TYPE IN"
+ "(720130028,720130029)AND A.SERVICE_ORDER_ID=?)X ORDER BY ORDER_BY";
		}
		List list = this.jt.queryForList(strSql, orderId, orderId);
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				String ido = "IN_DEAL_ORG";
				String inDealOrg = this.getDealOrg(map.get(ido));
				map.put(ido, inDealOrg);
				String odo = "OUT_DEAL_ORG";
				String outDealOrg = this.getDealOrg(map.get(odo));
				map.put(odo, outDealOrg);
				int isOverTime = 0;
				String beginDate = this.getMapValueStr(map, "BEGIN_DATE");
				String orderDate = this.getMapValueStr(map, "ACCEPT_DATE");
				String endDate = this.getMapValueStr(map, "END_DATE");
				int hangupTimeCount = this.getMapValueInt(map, "HANGUP_TIME_COUNT");
				int serviceType = this.getMapValueInt(map, "SERVICE_TYPE");
				String sysDate = this.getMapValueStr(map, "SYS_DATE");
				int workTime = pubFun.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate);
				int wlt = this.getMapValueInt(map, "WLT");
				if (workTime > wlt * 3600 && wlt > 0) {
					isOverTime = 1;
				}
				map.put("IS_OVER_TIME", isOverTime);
				//调账审批协查单，派单内容加入调账详情
				this.setRefundDetail(map);
				list.set(i, map);
			}
		}
		return list;
	}
	
	private void setRefundDetail(Map map) {
		String dealDesc = this.getMapValueStr(map, "DEAL_DESC");
		String inContent = this.getMapValueStr(map, "IN_CONTENT");
		if("省内协查派单".equals(dealDesc) && StringUtils.contains(inContent, "协查原因：")) {
			String sheetId = this.getMapValueStr(map, "OUT_SHEET_ID");
			List<Map<String, Object>> list = refundService.getRefundApproveInfo(sheetId);
			if(!list.isEmpty()) {
				Map<String, Object> info = list.get(0);
				String refundDetail = info.get("REFUND_DETAIL").toString();
				map.put("IN_CONTENT", inContent + "\n" + refundDetail);
			}
		}
	}
	
	public int insertSheetOperation(SheetOperation operation) {
		int num = 0;
		try {
    		String sql = "INSERT INTO cc_sheet_operation(SERVICE_ORDER_ID, WORK_SHEET_ID, OPER_DATE, DEAL_STAFF, DEAL_STAFF_ID, DEAL_STAFF_NAME, "
    				+ "DEAL_ORG_ID, DEAL_ORG_NAME, DISPATCH_ORG, DISPATCH_ORG_NAME, DISPATCH_STAFF, DISPATCH_STAFF_NAME, REMARK, MATCH_GUID) "
    				+ "VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			num = jt.update(sql, operation.getServiceOrderId(), operation.getWorkSheetId(), operation.getDealStaff(), operation.getDealStaffId(), 
					operation.getDealStaffName(), operation.getDealOrgId(), operation.getDealOrgName(), operation.getDispatchOrg(), 
					operation.getDispatchOrgName(), operation.getDispatchStaff(), operation.getDispatchStaffName(), operation.getRemark(), operation.getMatchGuid());
    	} catch(Exception e) {
    		log.error("insertSheetOperation error: {}", e.getMessage(), e);
    	}
		log.info("insertSheetOperation result: {}", num);
		return num;
	}

	public Map getDispatchOrgMap(String orderId, String remark) {
		try {
			String sql = "SELECT dispatch_org,dispatch_staff,deal_staff_id FROM cc_sheet_operation WHERE service_order_id=? AND remark=?";
			List tmpList = this.jt.queryForList(sql, orderId, remark);
			if (!tmpList.isEmpty()) {
				return (Map) tmpList.get(0);
			}
		} catch (Exception e) {
			log.error("getDispatchOrgMap error: {}", e.getMessage(), e);
		}
		return Collections.emptyMap();
	}

	private String getDealOrg(Object obj) {
		return obj == null ? "" : pubFun.getOrgWater(obj.toString());
	}
	
	private String getMapValueStr(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	private int getMapValueInt(Map map, String key) {
		return map.get(key) == null ? 0 : Integer.parseInt(map.get(key).toString());
	}
	
}