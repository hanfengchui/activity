/**
 * <p>类名：HastenSheetInfoDaoImpl.java</p>
 * <p>功能描叙:</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-6-13</p> 
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.service.IMessageManager;
import com.timesontransfar.customservice.worksheet.dao.IHastenSheetInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetRmp;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class HastenSheetInfoDaoImpl implements IHastenSheetInfoDao{
	
	@Autowired
	private JdbcTemplate jt;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private ISheetActionInfoDao sheetActionInfoDao;
	@Autowired
	private IMessageManager messageManager;
	
	private String saveHastenSql;//生成催单SQL
	private String quryHastenSql;//根据工单号查询当前催单工单
	private String quryHastenHis;//根据工单号查询历史催单工单
	private String deleHastenSql;//根据服务单号删除催单
	private String saveHatenHis;//想历史表插入数据
	private String orderHastenSql;//根据服务单号查询当前表
	private String orderHastenSqlHis;//根据服务单号历史表
	private String workSheetSql;//查询有催单的工单
	private String updateRegion;
	private String tyxrwSql;
	private String cdtxSql;
	

	/**
	 * 根据工单号删除对应的催单
	 * @param orderId 服务单号
	 * @return
	 */	
	public int delHastenSheet(String orderId,Integer month) {
		return jt.update(this.deleHastenSql, orderId, month);
	}

	/**
	 * 根据工单号查询相应的催单信息
	 * @param sheetId	工单号
	 *  @param boo	为true的时候 查询历史单
	 * @return	催单信息
	 */		
	public HastenSheetInfo[] getHastenSheetInfo(String sheetId,boolean boo) {
		//  SELECT * FROM CC_HASTEN_SHEET H WHERE H.WORK_SHEET_ID=?
		String strSql = "";
		if(boo) {
			strSql = this.quryHastenHis;
		} else {
			strSql = this.quryHastenSql;
		}
		
		List list = this.jt.query(strSql, new Object[] { sheetId },
				new HastenSheetRmp());	
		int listLong = list.size();
		if(listLong == 0) {
			return new HastenSheetInfo[0];
		}
		HastenSheetInfo[] hastenList = new HastenSheetInfo[listLong];
		for(int i = 0;i < listLong;i++) {
			hastenList[i] = (HastenSheetInfo)list.get(i);
		}
		list.clear();
		list = null;
		return hastenList;
	}

	/**
	 * 根据服务单号查询催单信息
	 * @param orderId
	 * @param boo TRUE查询历史 FALSE查询当前
	 * @return
	 */
	public HastenSheetInfo[] getOrderHatenInfo(String orderId,boolean boo){
		//  SELECT * FROM CC_HASTEN_SHEET H WHERE H.SERVICE_ORDER_ID=?
		String strSql = "";
		if(boo) {
			strSql = this.orderHastenSqlHis;
		} else {
			strSql = this.orderHastenSql;
		}
		
		List list = this.jt.query(strSql, new Object[] { orderId },
				new HastenSheetRmp());	
		int listLong = list.size();
		if(listLong == 0) {
			return new HastenSheetInfo[0];
		}
		HastenSheetInfo[] hastenList = new HastenSheetInfo[listLong];
		for(int i = 0;i < listLong;i++) {
			hastenList[i] = (HastenSheetInfo)list.get(i);
		}
		list.clear();
		list = null;
		return hastenList;		
	}
	
	/**
	 * 根据不同的查询条件取得催单信息
	 * @param strWhere 带如的WHERE 条件
	 * @param boo 为TRUE 查询历史
	 * @return HastenSheetInfo
	 */
	public HastenSheetInfo[] getListHastenInfo(String strWhere,boolean boo){
		String strSql = " ";
		if(boo) {
			strSql +="SELECT CC_HASTEN_SHEET_HIS.* FROM CC_HASTEN_SHEET_HIS ,CC_SERVICE_ORDER_ASK_HIS ,CC_WORK_SHEET_HIS  "+
						" WHERE CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID = CC_HASTEN_SHEET_HIS.SERVICE_ORDER_ID "+
						" AND CC_WORK_SHEET_HIS.SERVICE_ORDER_ID = CC_HASTEN_SHEET_HIS.SERVICE_ORDER_ID "+
						" AND CC_WORK_SHEET_HIS.WORK_SHEET_ID = CC_HASTEN_SHEET_HIS.WORK_SHEET_ID" + strWhere;
		} else {
				strSql +="SELECT CC_HASTEN_SHEET.* FROM CC_HASTEN_SHEET ,CC_SERVICE_ORDER_ASK ,CC_WORK_SHEET  "+
				" WHERE CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID = CC_HASTEN_SHEET.SERVICE_ORDER_ID "+
				" AND CC_WORK_SHEET.SERVICE_ORDER_ID = CC_HASTEN_SHEET.SERVICE_ORDER_ID "+
				" AND CC_WORK_SHEET.WORK_SHEET_ID = CC_HASTEN_SHEET.WORK_SHEET_ID" + strWhere;
			}
		List list = jt.query(strSql, new Object[] {},new HastenSheetRmp());	
		int size = list.size();
		if(size == 0) {
			return new HastenSheetInfo[0];
		}
		HastenSheetInfo[] listHasten = new  HastenSheetInfo[size];
		for(int i=0;i < size;i++) {
			listHasten[i] = (HastenSheetInfo)list.get(i);
		}
		list.clear();
		list = null;
		return listHasten;
	}
	
	/**
	 * 生成催单工单
	 * @param hasten 催单信息
	 * @return
	 */	
	public int saveHastenSheet(HastenSheetInfo hasten) {
		int count = this.jt.update(saveHastenSql, 
				hasten.getHastenGuid(),
				hasten.getOrderId(),
				hasten.getWorkSheetId(),
				hasten.getRegionId(),
				hasten.getRegionName(),
				hasten.getSendOrgName(),
				hasten.getSendOrgId(),
				hasten.getSendStaffName(),
				hasten.getSendStaffId(),
				hasten.getHastenReasonId(),
				hasten.getHastenReasonDesc(),
				hasten.getHastenInfo(),
				hasten.getMonth(),
				hasten.getValidFlag()
		);
		if (count < 1) {
			return 0;
		}
		
		List list = sheetActionInfoDao.queryHiddenListByOpraStaff(" AND a.service_order_id = '" + hasten.getOrderId() + "'");
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				MessagePrompt mp = new MessagePrompt();
				String opraStaff = map.get("OPRA_STAFF").toString();
				mp.setStaffId(Integer.parseInt(opraStaff));
				mp.setTypeId(StaticData.MESSAGE_PROMPT_HIDDEN);
				mp.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_HIDDEN));
				mp.setMsgContent("服务单：" + hasten.getOrderId() + "，" + pubFunc.getSysDate() + "，被催单！");
				messageManager.createMsgPrompt(mp);
			}
		}
		// 2020-4 时荣菊 天翼新入网
		List tyxrw = this.jt.queryForList(tyxrwSql + " AND a.service_order_id = '" + hasten.getOrderId() + "'");
		if (!tyxrw.isEmpty()) {
			for (int i = 0; i < tyxrw.size(); i++) {
				Map map = (Map) tyxrw.get(i);
				MessagePrompt mp = new MessagePrompt();
				String dealStaff = map.get("DEAL_STAFF").toString();
				mp.setStaffId(Integer.parseInt(dealStaff));
				mp.setTypeId(StaticData.MESSAGE_PROMPT_TYXRW);
				mp.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_TYXRW));
				mp.setMsgContent("天翼新入网预受理单：" + hasten.getOrderId() + "，" + pubFunc.getSysDate() + "，被催单！");
				messageManager.createMsgPrompt(mp);
			}
		}
		// 2020-11 袁萍要求所有投诉大类单催单被弹窗提醒
		List cdtx = this.jt.queryForList(cdtxSql + " AND a.service_order_id = '" + hasten.getOrderId() + "'");
		if (!cdtx.isEmpty()) {
			for (int i = 0; i < cdtx.size(); i++) {
				Map map = (Map) cdtx.get(i);
				MessagePrompt mp = new MessagePrompt();
				String dealStaff = map.get("DEAL_STAFF").toString();
				mp.setStaffId(Integer.parseInt(dealStaff));
				mp.setTypeId(StaticData.MESSAGE_PROMPT_CDTZ);
				mp.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_CDTZ));
				mp.setMsgContent("催单提醒：" + hasten.getOrderId() + "，" + pubFunc.getSysDate() + "，被催单！");
				messageManager.createMsgPrompt(mp);
			}
		}
		return count;
	}
	
	/**
	 * 更新工单催单数量
	 * @param sheetId
	 * @param month
	 * @return 更新数量
	 */
	public int updateSheetHastentNum(String sheetId,Integer month) {
		String strSql = "UPDATE CC_WORK_SHEET W SET W.HASTENT_NUM=(W.HASTENT_NUM+1) "+
						"WHERE W.WORK_SHEET_ID=? AND W.MONTH_FLAG=?";
		return this.jt.update(strSql,sheetId,month);
	}
	
	/**
	 * 向催单历史表中插入数据
	 * @param hasten 催单信息
	 * @return
	 */
	public int savHastenSheetInfoHis(String orderId,Integer month){
		/*		INSERT INTO CC_HASTEN_SHEET_HIS (HASTEN_SHEET_GUID,
				SERVICE_ORDER_ID, WORK_SHEET_ID, REGION_ID, REGION_NAME,
				CREAT_DATE, SEND_ORG_NAME, SEND_ORG_ID, SEND_STAFF_NAME,
				SEND_STAFF_ID, HASTEN_REASON_ID, HASTEN_REASON_DESC,
				HASTEN_INFO) SELECT * FROM CC_HASTEN_SHEET H WHERE H.SERVICE_ORDER_ID=?	*/	
		String strSql = this.saveHatenHis;
		return this.jt.update(strSql, orderId, month);	
	}

	public int checkIsValid(String serviceOrderId){
		String validSql = 
"SELECT IF(TIMESTAMPDIFF(SECOND,A.ACCEPT_DATE,NOW())>=4*3600,1,0)OVER4H,CASE WHEN NOW()<DATE_ADD(CURDATE(),INTERVAL 8 HOUR)THEN'DAWN'WHEN NOW()>DATE_ADD("
+ "CURDATE(),INTERVAL 20 HOUR)THEN'NIGHT'ELSE'DAY'END DAYTIME,IFNULL(L.VALID_HASTEN_NUM,0)VALID_HASTEN_NUM FROM CC_SERVICE_ORDER_ASK A,CC_SERVICE_LABEL"
+ " L WHERE L.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.COME_CATEGORY=707907001 AND IFNULL(L.VALID_HASTEN_NUM,0)<5 AND A.SERVICE_ORDER_ID='"
+ serviceOrderId + "'";
		List valids = jt.queryForList(validSql);
		if (valids.isEmpty()) {
			return 0;
		}
		int isValid = 0;
		String over4h = ((Map) valids.get(0)).get("OVER4H").toString();
		String dayTime = ((Map) valids.get(0)).get("DAYTIME").toString();
		String validHastenNum = ((Map) valids.get(0)).get("VALID_HASTEN_NUM").toString();
		valids.clear();
		if ("day".equals(dayTime)) {// 上班时间8：00-20：00
			isValid = checkDayValid(serviceOrderId, validHastenNum, over4h);
		} else { // 非工作时间（晚上20：00-次日8：00）
			isValid = checkNightValid(serviceOrderId, dayTime);
		}
		return isValid;
	}

	private int checkDayValid(String serviceOrderId, String validHastenNum, String over4h) {
		int isValid = 0;
		if ("0".equals(validHastenNum)) { // 第一次有效催单
			if ("1".equals(over4h)) {// 服务单受理4小时后的催单计为第一次有效催单
				isValid = 1;
			}
		} else { // 第一次有效催单时间开始每间隔两小时算一次有效催单
			String daySql =
"SELECT IF(TIMESTAMPDIFF(SECOND,CREAT_DATE,NOW())>=2*3600,1,0)OVER2H FROM CC_HASTEN_SHEET WHERE SERVICE_ORDER_ID='" +serviceOrderId+ "'AND VALID_FLAG=1 "
+ "ORDER BY CREAT_DATE DESC LIMIT 1";
			List days = jt.queryForList(daySql);
			if (days.isEmpty()) {
				return 1;
			}
			String over2h = ((Map) days.get(0)).get("OVER2H").toString();
			days.clear();
			if ("1".equals(over2h)) {
				isValid = 1;
			}
		}
		return isValid;
	}

	private int checkNightValid(String serviceOrderId, String dayTime) {
		int isValid = 0;
		String nightSql = 
"SELECT IF(COUNT(1)=4,1,0)FOURTIMES FROM CC_HASTEN_SHEET WHERE SERVICE_ORDER_ID='" + serviceOrderId + "'";
		if ("night".equals(dayTime)) {
			nightSql += " AND CREAT_DATE>DATE_ADD(CURDATE(),INTERVAL 20 HOUR)";
		} else {
			nightSql += " AND CREAT_DATE>DATE_ADD(DATE_SUB(CURDATE(),INTERVAL 1 DAY),INTERVAL 20 HOUR)";
		}
		List nights = jt.queryForList(nightSql);
		if (nights.isEmpty()) {
			return 0;
		}
		String fourTimes = ((Map) nights.get(0)).get("FOURTIMES").toString();
		nights.clear();
		if ("1".equals(fourTimes)) { // 非工作时间（晚上20：00-次日8：00），累计5次及以上算一次有效催单
			isValid = 1;
		}
		return isValid;
	}
	
	public int updateRegion(String serviceOrderId, int oldRegion, int newRegion, String newRegionName){
		//UPDATE CC_HASTEN_SHEET S SET S.REGION_ID = ? ,S.REGION_NAME = ? WHERE S.SERVICE_ORDER_ID = ? AND S.REGION_ID = ?
		return jt.update(updateRegion, newRegion, newRegionName, serviceOrderId, oldRegion);
	}

	/**
	 * 工单状态为挂起/自动回访、受理渠道为申诉-政府监管渠道-工信部-部立案/部并案、受理渠道为申诉且渠道细项非部立案/部并案且工单状态为终定性
	 */
	@Override
	public int getHastenCondition(String orderId) {
		String sql = "SELECT COUNT(1) FROM cc_service_order_ask c LEFT JOIN cc_work_sheet w ON c.SERVICE_ORDER_ID = w.SERVICE_ORDER_ID "
				+ "WHERE c.SERVICE_ORDER_ID = ? "
				+ "AND ( c.CHANNEL_DETAIL_ID IN (707907029 , 707907098) "
				+ "OR ( c.COME_CATEGORY = 707907003 AND c.CHANNEL_DETAIL_ID NOT IN (707907029 , 707907098) AND c.ORDER_STATU = 720130008 ) "
				+ "OR w.SHEET_STATU IN (720130035 , 700000046) "
				+ "OR c.ORDER_STATU = 720130007 )";
		return this.jt.queryForObject(sql, new Object[]{orderId}, Integer.class);
	}

	@Override
	public String getRefunded(String orderId) {
		String newOrderId = "";
		String sql = "select count(1) from cc_service_content_ask c where c.APPEAL_CHILD = 2020501 AND c.SERVICE_TYPE = 720200003 AND c.SERVICE_ORDER_ID = ?";
		int refunded = this.jt.queryForObject(sql, new Object[]{orderId}, Integer.class);
		if (refunded != 0){
			String querySql = "select t.NEW_ORDER_ID from cc_service_track_tz t where t.OLD_ORDER_ID = ? order by t.CREATE_DATE desc limit 1";
			List list = this.jt.queryForList(querySql, orderId);
			if (!list.isEmpty()) {
				Map map = (Map) list.get(0);
				newOrderId = map.get("NEW_ORDER_ID").toString();
			}
		}
		return newOrderId;
	}

	/**
	 * @return workSheetSql
	 */
	public String getWorkSheetSql() {
		return workSheetSql;
	}

	/**
	 * @param workSheetSql 要设置的 workSheetSql
	 */
	public void setWorkSheetSql(String workSheetSql) {
		this.workSheetSql = workSheetSql;
	}

	/**
	 * @return orderHastenSql
	 */
	public String getOrderHastenSql() {
		return orderHastenSql;
	}

	/**
	 * @param orderHastenSql 要设置的 orderHastenSql
	 */
	public void setOrderHastenSql(String orderHastenSql) {
		this.orderHastenSql = orderHastenSql;
	}

	/**
	 * @return orderHastenSqlHis
	 */
	public String getOrderHastenSqlHis() {
		return orderHastenSqlHis;
	}

	/**
	 * @param orderHastenSqlHis 要设置的 orderHastenSqlHis
	 */
	public void setOrderHastenSqlHis(String orderHastenSqlHis) {
		this.orderHastenSqlHis = orderHastenSqlHis;
	}

	/**
	 * @return deleHastenSql
	 */
	public String getDeleHastenSql() {
		return deleHastenSql;
	}

	/**
	 * @param deleHastenSql 要设置的 deleHastenSql
	 */
	public void setDeleHastenSql(String deleHastenSql) {
		this.deleHastenSql = deleHastenSql;
	}

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
	 * @return quryHastenSql
	 */
	public String getQuryHastenSql() {
		return quryHastenSql;
	}

	/**
	 * @param quryHastenSql 要设置的 quryHastenSql
	 */
	public void setQuryHastenSql(String quryHastenSql) {
		this.quryHastenSql = quryHastenSql;
	}

	/**
	 * @return saveHastenSql
	 */
	public String getSaveHastenSql() {
		return saveHastenSql;
	}

	/**
	 * @param saveHastenSql 要设置的 saveHastenSql
	 */
	public void setSaveHastenSql(String saveHastenSql) {
		this.saveHastenSql = saveHastenSql;
	}

	/**
	 * @return quryHastenHis
	 */
	public String getQuryHastenHis() {
		return quryHastenHis;
	}

	/**
	 * @param quryHastenHis 要设置的 quryHastenHis
	 */
	public void setQuryHastenHis(String quryHastenHis) {
		this.quryHastenHis = quryHastenHis;
	}

	/**
	 * @return saveHatenHis
	 */
	public String getSaveHatenHis() {
		return saveHatenHis;
	}

	/**
	 * @param saveHatenHis 要设置的 saveHatenHis
	 */
	public void setSaveHatenHis(String saveHatenHis) {
		this.saveHatenHis = saveHatenHis;
	}

	public void setUpdateRegion(String updateRegion) {
		this.updateRegion = updateRegion;
	}

	public ISheetActionInfoDao getSheetActionInfoDao() {
		return sheetActionInfoDao;
	}

	public void setSheetActionInfoDao(ISheetActionInfoDao sheetActionInfoDao) {
		this.sheetActionInfoDao = sheetActionInfoDao;
	}

	public IMessageManager getMessageManager() {
		return messageManager;
	}

	public void setMessageManager(IMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	public String getTyxrwSql() {
		return tyxrwSql;
	}

	public void setTyxrwSql(String tyxrwSql) {
		this.tyxrwSql = tyxrwSql;
	}

	public String getCdtxSql() {
		return cdtxSql;
	}

	public void setCdtxSql(String cdtxSql) {
		this.cdtxSql = cdtxSql;
	}
}