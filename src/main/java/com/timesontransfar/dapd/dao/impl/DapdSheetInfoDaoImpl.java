package com.timesontransfar.dapd.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
import com.dapd.pojo.DapdSheetInfo;
import com.timesontransfar.dapd.dao.IdapdSheetInfoDao;

public class DapdSheetInfoDaoImpl implements IdapdSheetInfoDao {
	private static final Logger log = LoggerFactory.getLogger(DapdSheetInfoDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;

	public int insertDapdSheetInfo(DapdSheetInfo dapd) {
		String str = "INSERT INTO dapd_sheet_info(modify_date,prvnce_id,latn_id,regin_id,sub_grid_id,"
				+ "record_no,call_id,sheet_status,sheet_oprt_chnl,sheet_id_prov,"
				+ "cmplnt_id,sheet_id_comp,sheet_id_croscmplnt,comp_date,comp_user_name,"
				+ "create_date,staff_id,caller,busi_nbr,prod_inst_id,"
				+ "cust_id,contact_no1,contact_no2,cmplnt_desc,cmplnt_source,"
				+ "strictest_sheet_id,strictest_sheet_type,cmplnt_apr_level_1_nm,cmplnt_apr_level_2_nm,cmplnt_apr_level_3_nm,"
				+ "cmplnt_apr_level_1,cmplnt_apr_level_2,cmplnt_apr_level_3,cmplnt_prod_level_1_nm,cmplnt_prod_level_2_nm,"
				+ "cmplnt_prod_level_1,cmplnt_prod_level_2,dvlp_chnl_nm,dvlp_chnl,dvlp_chnl_level_1_nm,"
				+ "dvlp_chnl_level_1,dvlp_chnl_level_2_nm,dvlp_chnl_level_2,dvlp_chnl_level_3_nm,dvlp_chnl_level_3,"
				+ "recmplnt_times_30days,croscmplnt_times_30days,moneyback_90days,sheet_estima_30days,upgrade_trend,"
				+ "cust_type,member_level,serv_age,age,import_cust,"
				+ "dict_key,city_flag,main_offer_id,main_offer_nm,dispute_chnl_nm,"
				+ "dispute_chnl,dispute_chnl_1_nm,dispute_chnl_1,dispute_chnl_2_nm,dispute_chnl_2,"
				+ "dispute_chnl_3_nm,dispute_chnl_3,real_name_status,claim_sorc,comp_depart,"
				+ "comp_conc,comp_ip,comp_status,res_con_channel,nbr_location,"
				+ "sheet_type,cust_name,sheet_estima_30days_sat,sheet_estima_30days_dissat,cmplnt_source_cd,"
				+ "cert_type)"
				+ "VALUES(NOW(),?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "NOW(),?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?,?,?,?,?,"
				+ "?)";
		return jt.update(str, substring(dapd.getPrvnceId(), 3, "-1"),
				substring(dapd.getLatnId(), 5, "-1"),
				substring(dapd.getReginId(), 30, "-1"),
				substring(dapd.getSubGridId(), 50, "-1"),
				substring(dapd.getRecordNo(), 50, null),
				substring(dapd.getCallId(), 50, null),
				"0",
				substring(dapd.getSheetOprtChnl(), 200, "-1"),
				dapd.getSheetIdProv(),
				substring(dapd.getCmplntId(), 50, null),
				substring(dapd.getSheetIdComp(), 50, null),
				substring(dapd.getSheetIdCroscmplnt(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getCompDate(), null),
				substring(dapd.getCompUserName(), 200, null),
				substring(dapd.getStaffId(), 30, "-1"),
				substring(dapd.getCaller(), 50, "-1"),
				substring(dapd.getBusiNbr(), 50, "-1"),
				substring(dapd.getProdInstId(), 30, "-1"),
				substring(dapd.getCustId(), 30, "-1"),
				substring(dapd.getContactNo1(), 50, "-1"),
				substring(dapd.getContactNo2(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getCmplntDesc(), "-1"),
				substring(dapd.getCmplntSource(), 200, "10000号语音坐席"),
				substring(dapd.getStrictestSheetId(), 2, "-1"),
				substring(dapd.getStrictestSheetType(), 5, null),
				substring(dapd.getCmplntAprLevel1Nm(), 200, "-1"),
				substring(dapd.getCmplntAprLevel2Nm(), 200, "-1"),
				substring(dapd.getCmplntAprLevel3Nm(), 200, null),
				substring(dapd.getCmplntAprLevel1(), 50, "-1"),
				substring(dapd.getCmplntAprLevel2(), 50, "-1"),
				substring(dapd.getCmplntAprLevel3(), 50, null),
				substring(dapd.getCmplntProdLevel1Nm(), 200, "-1"),
				substring(dapd.getCmplntProdLevel2Nm(), 200, null),
				substring(dapd.getCmplntProdLevel1(), 50, "-1"),
				substring(dapd.getCmplntProdLevel2(), 50, null),
				substring(dapd.getDvlpChnlNm(), 200, null),
				substring(dapd.getDvlpChnl(), 50, null),
				substring(dapd.getDvlpChnlLevel1Nm(), 200, null),
				substring(dapd.getDvlpChnlLevel1(), 50, null),
				substring(dapd.getDvlpChnlLevel2Nm(), 200, null),
				substring(dapd.getDvlpChnlLevel2(), 50, null),
				substring(dapd.getDvlpChnlLevel3Nm(), 200, null),
				substring(dapd.getDvlpChnlLevel3(), 50, null),
				substring(dapd.getRecmplntTimes30days(), 5, "0"),
				substring(dapd.getCroscmplntTimes30days(), 5, "0"),
				substring(dapd.getMoneyback90days(), 50, "0"),
				substring(dapd.getSheetEstima30days(), 50, null),
				substring(dapd.getUpgradeTrend(), 50, null),
				substring(dapd.getCustType(), 50, "-1"),
				substring(dapd.getMemberLevel(), 50, "-1"),
				substring(dapd.getServAge(), 50, "0"),
				substring(dapd.getAge(), 50, null),
				substring(dapd.getImportCust(), 50, null),
				substring(dapd.getDictKey(), 50, null),
				substring(dapd.getCityFlag(), 50, null),
				substring(dapd.getMainOfferId(), 256, "-1"),
				substring(dapd.getMainOfferNm(), 200, "-1"),
				StringUtils.defaultIfEmpty(dapd.getDisputeChnlNm(), null),
				substring(dapd.getDisputeChnl(), 50, null),
				substring(dapd.getDisputeChnl1Nm(), 200, null),
				substring(dapd.getDisputeChnl1(), 50, null),
				substring(dapd.getDisputeChnl2Nm(), 200, null),
				substring(dapd.getDisputeChnl2(), 50, null),
				substring(dapd.getDisputeChnl3Nm(), 200, null),
				substring(dapd.getDisputeChnl3(), 50, null),
				substring(dapd.getRealNameStatus(), 2, "-1"),
				substring(dapd.getClaimSorc(), 50, null),
				substring(dapd.getCompDepart(), 200, "-1"),
				substring(dapd.getCompConc(), 200, "-1"),
				substring(dapd.getCompIp(), 200, null),
				substring(dapd.getCompStatus(), 2, null),
				substring(dapd.getResConChannel(), 200, null),
				substring(dapd.getNbrLocation(), 50, null),
				dapd.getSheetType(),
				substring(dapd.getCustName(), 500, null),
				substring(dapd.getSheetEstima30daysSat(), 5, "0"),
				substring(dapd.getSheetEstima30daysDissat(), 5, "0"),
				substring(dapd.getCmplntSourceCd(), 50, "LY010101"),
				substring(dapd.getCertType(), 200, "-1"));
	}

	// 修改工单信息
	public int updateDapdSheet(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET prvnce_id=?,latn_id=?,regin_id=?,sub_grid_id=?,caller=?,"
				+ "busi_nbr=?,prod_inst_id=?,cust_id=?,contact_no1=?,cmplnt_desc=?,"
				+ "cmplnt_source=?,strictest_sheet_id=?,strictest_sheet_type=?,cmplnt_apr_level_1_nm=?,cmplnt_apr_level_2_nm=?,"
				+ "cmplnt_apr_level_3_nm=?,cmplnt_apr_level_1=?,cmplnt_apr_level_2=?,cmplnt_apr_level_3=?,cmplnt_prod_level_1_nm=?,"
				+ "cmplnt_prod_level_2_nm=?,cmplnt_prod_level_1=?,cmplnt_prod_level_2=?,dvlp_chnl_nm=?,dvlp_chnl=?,"
				+ "dvlp_chnl_level_1_nm=?,dvlp_chnl_level_1=?,dvlp_chnl_level_2_nm=?,dvlp_chnl_level_2=?,dvlp_chnl_level_3_nm=?,"
				+ "dvlp_chnl_level_3=?,cust_type=?,member_level=?,serv_age=?,age=?,"
				+ "dispute_chnl_nm=?,dispute_chnl=?,dispute_chnl_1_nm=?,dispute_chnl_1=?,dispute_chnl_2_nm=?,"
				+ "dispute_chnl_2=?,dispute_chnl_3_nm=?,dispute_chnl_3=?,real_name_status=?,res_con_channel=?,"
				+ "nbr_location=?,cust_name=?,cmplnt_source_cd=?,cert_type=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getPrvnceId(), 3, "-1"),
				substring(dapd.getLatnId(), 5, "-1"),
				substring(dapd.getReginId(), 30, "-1"),
				substring(dapd.getSubGridId(), 50, "-1"),
				substring(dapd.getCaller(), 50, "-1"),
				substring(dapd.getBusiNbr(), 50, "-1"),
				substring(dapd.getProdInstId(), 30, "-1"),
				substring(dapd.getCustId(), 30, "-1"),
				substring(dapd.getContactNo1(), 50, "-1"),
				StringUtils.defaultIfEmpty(dapd.getCmplntDesc(), "-1"),
				substring(dapd.getCmplntSource(), 200, "10000号语音坐席"),
				substring(dapd.getStrictestSheetId(), 2, "-1"),
				substring(dapd.getStrictestSheetType(), 5, null),
				substring(dapd.getCmplntAprLevel1Nm(), 200, "-1"),
				substring(dapd.getCmplntAprLevel2Nm(), 200, "-1"),
				substring(dapd.getCmplntAprLevel3Nm(), 200, null),
				substring(dapd.getCmplntAprLevel1(), 50, "-1"),
				substring(dapd.getCmplntAprLevel2(), 50, "-1"),
				substring(dapd.getCmplntAprLevel3(), 50, null),
				substring(dapd.getCmplntProdLevel1Nm(), 200, "-1"),
				substring(dapd.getCmplntProdLevel2Nm(), 200, null),
				substring(dapd.getCmplntProdLevel1(), 50, "-1"),
				substring(dapd.getCmplntProdLevel2(), 50, null),
				substring(dapd.getDvlpChnlNm(), 200, null),
				substring(dapd.getDvlpChnl(), 50, null),
				substring(dapd.getDvlpChnlLevel1Nm(), 200, null),
				substring(dapd.getDvlpChnlLevel1(), 50, null),
				substring(dapd.getDvlpChnlLevel2Nm(), 200, null),
				substring(dapd.getDvlpChnlLevel2(), 50, null),
				substring(dapd.getDvlpChnlLevel3Nm(), 200, null),
				substring(dapd.getDvlpChnlLevel3(), 50, null),
				substring(dapd.getCustType(), 50, "-1"),
				substring(dapd.getMemberLevel(), 50, "-1"),
				substring(dapd.getServAge(), 50, "0"),
				substring(dapd.getAge(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getDisputeChnlNm(), null),
				substring(dapd.getDisputeChnl(), 50, null),
				substring(dapd.getDisputeChnl1Nm(), 200, null),
				substring(dapd.getDisputeChnl1(), 50, null),
				substring(dapd.getDisputeChnl2Nm(), 200, null),
				substring(dapd.getDisputeChnl2(), 50, null),
				substring(dapd.getDisputeChnl3Nm(), 200, null),
				substring(dapd.getDisputeChnl3(), 50, null),
				substring(dapd.getRealNameStatus(), 2, "-1"),
				substring(dapd.getResConChannel(), 200, null),
				substring(dapd.getNbrLocation(), 50, null),
				substring(dapd.getCustName(), 500, null),
				substring(dapd.getCmplntSourceCd(), 50, "LY010101"),
				substring(dapd.getCertType(), 200, "-1"),
				dapd.getSheetIdProv());
	}

	// 受理模板提交方法
	public int updateAcceptTemplate(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET offer_nm=?,offer_nbr=?,group_offer_nbr=?,fault_times_30days=?,fault_id_latest=?,"
				+ "deal_rslt_latest=?,longitude=?,latitude=?,problem_sort=?,problem_addr=?,"
				+ "deal_rslt=?,mntnce_staff_id=?,mntnce_staff_id_latest=?,develop_staff=?,accs_time=?,"
				+ "accept_staff=?,outbound_caller=?,main_offer_id=?,main_offer_nm=?,order_id=?,"
				+ "accept_date=?,rmng_flux=?,use_flux=?,rmng_voice=?,stop_type=?,"
				+ "stop_time=?,eff_time=?,exp_time=?,dispute_month=?,account_type=?,"
				+ "dispute_bill=?,recover_time=?,nbr_status=?,non_actvte_apr=?,charge_bill=?,"
				+ "charge_chnl=?,charge_time=?,good_nbr_level=?,oprt_type=?,open_date_prsnt=?,"
				+ "uninstall_time_pre=?,is_dstnc_90days=?,unusual_apr=?,uninstall_time=?,message_status=?,"
				+ "unusual_time=?,charge_nbr=?,error_tips=?,bal_type=?,transfer_acct=?,"
				+ "get_transcd_time=?,portoutnet_time=?,portinnet_time=?,portnet_status=?,caller_callee_nbr=?,"
				+ "rmnd_type=?,exchg_ord=?,exchg_chnl=?,exchg_time=?,exchg_mtrl=?,"
				+ "visit_chnl=?,visit_shop_time=?,argu_points=?,is_exchange_rcrd=?,print_chnl=?,"
				+ "invc_query_chnl=?,invc_mdfy_ctnt=?,push_time=?,invc_push_mthd=?,bill_query_chnl=?,"
				+ "cntct_time=?,reported_chnl=?,undesira_info_ctnt=?,harass_nbr=?,swindle_nbr=?,"
				+ "target_nbr=?,jieu_nm=?,platform_nm=?,charge_phone_nbr=?,error_phone_nbr=?,"
				+ "is_recall=?,offer_type=?,good_offer_name=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getOfferNm(), 200, null),
				substring(dapd.getOfferNbr(), 50, null),
				substring(dapd.getGroupOfferNbr(), 50, null),
				substring(dapd.getFaultTimes30days(), 50, null),
				substring(dapd.getFaultIdLatest(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getDealRsltLatest(), null),
				substring(dapd.getLongitude(), 50, null),
				substring(dapd.getLatitude(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getProblemSort(), null),
				StringUtils.defaultIfEmpty(dapd.getProblemAddr(), null),
				StringUtils.defaultIfEmpty(dapd.getDealRslt(), null),
				substring(dapd.getMntnceStaffId(), 50, null),
				substring(dapd.getMntnceStaffIdLatest(), 100, null),
				substring(dapd.getDevelopStaff(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getAccsTime(), null),
				substring(dapd.getAcceptStaff(), 50, null),
				substring(dapd.getOutboundCaller(), 50, null),
				substring(dapd.getMainOfferId(), 256, "-1"),
				substring(dapd.getMainOfferNm(), 200, "-1"),
				substring(dapd.getOrderId(), 200, null),
				StringUtils.defaultIfEmpty(dapd.getAcceptDate(), null),
				substring(dapd.getRmngFlux(), 50, null),
				substring(dapd.getUseFlux(), 256, null),
				substring(dapd.getRmngVoice(), 50, null),
				substring(dapd.getStopType(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getStopTime(), null),
				StringUtils.defaultIfEmpty(dapd.getEffTime(), null),
				StringUtils.defaultIfEmpty(dapd.getExpTime(), null),
				substring(dapd.getDisputeMonth(), 256, null),
				StringUtils.defaultIfEmpty(dapd.getAccountType(), null),
				substring(dapd.getDisputeBill(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getRecoverTime(), null),
				substring(dapd.getNbrStatus(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getNonActvteApr(), null),
				substring(dapd.getChargeBill(), 256, null),
				substring(dapd.getChargeChnl(), 200, null),
				StringUtils.defaultIfEmpty(dapd.getChargeTime(), null),
				substring(dapd.getGoodNbrLevel(), 50, null),
				substring(dapd.getOprtType(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getOpenDatePrsnt(), null),
				StringUtils.defaultIfEmpty(dapd.getUninstallTimePre(), null),
				substring(dapd.getIsDstnc90days(), 5, null),
				StringUtils.defaultIfEmpty(dapd.getUnusualApr(), null),
				StringUtils.defaultIfEmpty(dapd.getUninstallTime(), null),
				substring(dapd.getMessageStatus(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getUnusualTime(), null),
				StringUtils.defaultIfEmpty(dapd.getChargeNbr(), null),
				StringUtils.defaultIfEmpty(dapd.getErrorTips(), null),
				substring(dapd.getBalType(), 50, null),
				substring(dapd.getTransferAcct(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getGetTranscdTime(), null),
				StringUtils.defaultIfEmpty(dapd.getPortoutnetTime(), null),
				StringUtils.defaultIfEmpty(dapd.getPortinnetTime(), null),
				substring(dapd.getPortnetStatus(), 256, null),
				substring(dapd.getCallerCalleeNbr(), 50, null),
				substring(dapd.getRmndType(), 50, null),
				substring(dapd.getExchgOrd(), 200, null),
				substring(dapd.getExchgChnl(), 200, null),
				StringUtils.defaultIfEmpty(dapd.getExchgTime(), null),
				substring(dapd.getExchgMtrl(), 200, null),
				substring(dapd.getVisitChnl(), 200, null),
				StringUtils.defaultIfEmpty(dapd.getVisitShopTime(), null),
				substring(dapd.getArguPoints(), 50, null),
				substring(dapd.getIsExchangeRcrd(), 5, null),
				substring(dapd.getPrintChnl(), 200, null),
				substring(dapd.getInvcQueryChnl(), 200, null),
				substring(dapd.getInvcMdfyCtnt(), 200, null),
				StringUtils.defaultIfEmpty(dapd.getPushTime(), null),
				substring(dapd.getInvcPushMthd(), 50, null),
				substring(dapd.getBillQueryChnl(), 200, null),
				StringUtils.defaultIfEmpty(dapd.getCntctTime(), null),
				StringUtils.defaultIfEmpty(dapd.getReportedChnl(), null),
				StringUtils.defaultIfEmpty(dapd.getUndesiraInfoCtnt(), null),
				substring(dapd.getHarassNbr(), 256, null),
				substring(dapd.getSwindleNbr(), 50, null),
				substring(dapd.getTargetNbr(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getJieuNm(), null),
				StringUtils.defaultIfEmpty(dapd.getPlatformNm(), null),
				substring(dapd.getChargePhoneNbr(), 50, null),
				substring(dapd.getErrorPhoneNbr(), 50, null),
				substring(dapd.getIsRecall(), 50, null),
				substring(dapd.getOfferType(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getGoodOfferName(), null),
				dapd.getSheetIdProv());
	}

	// 更新标识
	public int updateDapdFlag(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET recmplnt_times_30days=?,croscmplnt_times_30days=?,sheet_estima_30days=?,sheet_estima_30days_sat=?,sheet_estima_30days_dissat=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getRecmplntTimes30days(), 5, "0"),
				substring(dapd.getCroscmplntTimes30days(), 5, "0"),
				substring(dapd.getSheetEstima30days(), 50, null),
				substring(dapd.getSheetEstima30daysSat(), 5, "0"),
				substring(dapd.getSheetEstima30daysDissat(), 5, "0"),
				dapd.getSheetIdProv());
	}

	// 更新申诉信息
	public int updateCompInfo(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET sheet_id_comp=?,comp_date=?,claim_sorc=?,comp_ip=?,comp_status=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getSheetIdComp(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getCompDate(), null),
				substring(dapd.getClaimSorc(), 50, null),
				substring(dapd.getCompIp(), 200, null),
				substring(dapd.getCompStatus(), 2, null),
				dapd.getSheetIdProv());
	}

	// 处理模板提交方法
	public int updateDealTemplate(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET reason=?,check_ctnt=?,is_slv=?,unrslv_rsn=?,deal_result=?,"
				+ "is_charge_rcrd=?,dispute_return_rcrd=?,is_use_rcrd=?,rfd_rcrd=?,into_acct_rcrd=?,"
				+ "out_scomb_flux=?,threshold_rmnd_rcrd=?,blok_dept=?,blok_staff_id=?,estim_repair_time=?,"
				+ "is_scnd_confirm=?,outbound_record_rcrd=?,outbound_staff_id=?,outbound_chnl=?,is_keep_appoint=?,"
				+ "charge_id=?,charge_date=?,get_bill=?,rfd_type=?,rfd_bill=?,"
				+ "rfd_id=?,pre_offer_nm=?,plan_chnge_offer_nm=?,vas=?,busi_status=?,"
				+ "use_ctnt=?,is_refer_ld=?,ld_bill=?,agrmnt_due_time=?,busi_type=?,"
				+ "new_innet_time=?,accept_staff_cd=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, StringUtils.defaultIfEmpty(dapd.getReason(), null),
				StringUtils.defaultIfEmpty(dapd.getCheckCtnt(), null),
				substring(dapd.getIsSlv(), 2, null),
				StringUtils.defaultIfEmpty(dapd.getUnrslvRsn(), null),
				StringUtils.defaultIfEmpty(dapd.getDealResult(), null),
				substring(dapd.getIsChargeRcrd(), 5, null),
				substring(dapd.getDisputeReturnRcrd(), 256, null),
				substring(dapd.getIsUseRcrd(), 5, null),
				substring(dapd.getRfdRcrd(), 256, null),
				substring(dapd.getIntoAcctRcrd(), 50, null),
				substring(dapd.getOutScombFlux(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getThresholdRmndRcrd(), null),
				substring(dapd.getBlokDept(), 50, null),
				substring(dapd.getBlokStaffId(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getEstimRepairTime(), null),
				substring(dapd.getIsScndConfirm(), 5, null),
				substring(dapd.getOutboundRecordRcrd(), 256, null),
				substring(dapd.getOutboundStaffId(), 256, null),
				substring(dapd.getOutboundChnl(), 200, null),
				substring(dapd.getIsKeepAppoint(), 50, null),
				substring(dapd.getChargeId(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getChargeDate(), null),
				substring(dapd.getGetBill(), 50, null),
				substring(dapd.getRfdType(), 20, null),
				substring(dapd.getRfdBill(), 50, null),
				substring(dapd.getRfdId(), 50, null),
				substring(dapd.getPreOfferNm(), 200, null),
				substring(dapd.getPlanChngeOfferNm(), 200, null),
				substring(dapd.getVas(), 200, null),
				substring(dapd.getBusiStatus(), 50, null),
				substring(dapd.getUseCtnt(), 50, null),
				substring(dapd.getIsReferLd(), 50, null),
				substring(dapd.getLdBill(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getAgrmntDueTime(), null),
				substring(dapd.getBusiType(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getNewInnetTime(), null),
				substring(dapd.getAcceptStaffCd(), 50, null),
				dapd.getSheetIdProv());
	}

	// 办结方法
	public int updateDapdEndDate(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET sheet_id_comp=?,comp_date=?,comp_user_name=?,recall_record_no=?,recall_record=?,"
				+ "recall_time=?,recall_staff_id=?,reason=?,is_timeout=?,timeout_rsn=?,"
				+ "is_slv=?,is_raise_finish=?,is_valet_cmplnt=?,valet_cmplnt_map=?,rsn_level_1_nm=?,"
				+ "rsn_level_1=?,rsn_level_2_nm=?,rsn_level_2=?,rsn_level_3_nm=?,rsn_level_3=?,"
				+ "rsn_level_4_nm=?,rsn_level_4=?,rsn_level_5_nm=?,rsn_level_5=?,end_date=NOW(),"
				+ "claim_sorc=?,spcl_claim_type=?,is_same_claim_type=?,comp_depart=?,comp_conc=?,"
				+ "comp_ip=?,comp_status=?,comp_dur=?,if_punish=?,punish_mount=?,"
				+ "punish_depart=?,is_fair_complaint=?,action_flag=?,archive_ctnt=?,res_quality=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getSheetIdComp(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getCompDate(), null),
				substring(dapd.getCompUserName(), 200, null),
				substring(dapd.getRecallRecordNo(), 100, null),
				substring(dapd.getRecallRecord(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getRecallTime(), null),
				substring(dapd.getRecallStaffId(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getReason(), null),
				substring(dapd.getIsTimeout(), 2, "-1"),
				StringUtils.defaultIfEmpty(dapd.getTimeoutRsn(), null),
				substring(dapd.getIsSlv(), 2, "-1"),
				substring(dapd.getIsRaiseFinish(), 2, "-1"),
				substring(dapd.getIsValetCmplnt(), 2, "-1"),
				StringUtils.defaultIfEmpty(dapd.getValetCmplntMap(), null),
				substring(dapd.getRsnLevel1Nm(), 200, null),
				substring(dapd.getRsnLevel1(), 50, null),
				substring(dapd.getRsnLevel2Nm(), 200, null),
				substring(dapd.getRsnLevel2(), 50, null),
				substring(dapd.getRsnLevel3Nm(), 200, null),
				substring(dapd.getRsnLevel3(), 50, null),
				substring(dapd.getRsnLevel4Nm(), 200, null),
				substring(dapd.getRsnLevel4(), 50, null),
				substring(dapd.getRsnLevel5Nm(), 200, null),
				substring(dapd.getRsnLevel5(), 50, null),
				substring(dapd.getClaimSorc(), 50, null),
				substring(dapd.getSpclClaimType(), 50, null),
				substring(dapd.getIsSameClaimType(), 2, null),
				StringUtils.defaultIfEmpty(dapd.getCompDepart(), "-1"),
				substring(dapd.getCompConc(), 200, "-1"),
				substring(dapd.getCompIp(), 200, null),
				substring(dapd.getCompStatus(), 2, null),
				substring(dapd.getCompDur(), 10, null),
				substring(dapd.getIfPunish(), 2, "0"),
				substring(dapd.getPunishMount(), 10, null),
				StringUtils.defaultIfEmpty(dapd.getPunishDepart(), null),
				substring(dapd.getIsFairComplaint(), 5, "-1"),
				substring(dapd.getActionFlag(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getArchiveCtnt(), null),
				StringUtils.defaultIfEmpty(dapd.getResQuality(), null),
				dapd.getSheetIdProv());
	}

	// 客户满意测评：0未回访、1用户未参评、2满意、3不满意
	public int updateDapdSatEval(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET sat_eval=?,modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getSatEval(), 2, "0"), dapd.getSheetIdProv());
	}

	// 归档方法
	public int updateDapdArchiveDate(DapdSheetInfo dapd) {
		String str = "UPDATE dapd_sheet_info SET sheet_status='1',sheet_id_comp=?,comp_date=?,comp_user_name=?,strictest_sheet_id=?,"
				+ "strictest_sheet_type=?,is_valet_cmplnt=?,valet_cmplnt_map=?,archive_date=NOW(),claim_sorc=?,"
				+ "spcl_claim_type=?,is_same_claim_type=?,sat_eval=?,comp_ip=?,comp_status=?,"
				+ "urge_sheet_cnt=?,redirct_times=?,"
				+ "modify_date=NOW()WHERE sheet_id_prov=?";
		return jt.update(str, substring(dapd.getSheetIdComp(), 50, null),
				StringUtils.defaultIfEmpty(dapd.getCompDate(), null),
				substring(dapd.getCompUserName(), 200, null),
				substring(dapd.getStrictestSheetId(), 2, "-1"),
				substring(dapd.getStrictestSheetType(), 5, null),
				substring(dapd.getIsValetCmplnt(), 2, "-1"),
				StringUtils.defaultIfEmpty(dapd.getValetCmplntMap(), null),
				substring(dapd.getClaimSorc(), 50, null),
				substring(dapd.getSpclClaimType(), 50, null),
				substring(dapd.getIsSameClaimType(), 2, null),
				substring(dapd.getSatEval(), 2, "0"),
				substring(dapd.getCompIp(), 200, null),
				substring(dapd.getCompStatus(), 2, null),
				substring(dapd.getUrgeSheetCnt(), 3, null),
				substring(dapd.getRedirctTimes(), 3, null),
				dapd.getSheetIdProv());
	}

	public DapdSheetInfo selectDapdSheetInfoBySheetIdProv(String sheetIdProv) {
		String str = "SELECT DATE_FORMAT(modify_date,'%Y-%m-%d %H:%i:%s')modify_date,prvnce_id,latn_id,regin_id,sub_grid_id,"
				+ "record_no,call_id,sheet_status,sheet_oprt_chnl,sheet_id_prov,"
				+ "cmplnt_id,sheet_id_comp,sheet_id_croscmplnt,DATE_FORMAT(comp_date,'%Y-%m-%d %H:%i:%s')comp_date,comp_user_name,"
				+ "DATE_FORMAT(create_date,'%Y-%m-%d %H:%i:%s')create_date,staff_id,caller,busi_nbr,prod_inst_id,"
				+ "cust_id,contact_no1,contact_no2,cmplnt_desc,cmplnt_source,"
				+ "strictest_sheet_id,strictest_sheet_type,cmplnt_apr_level_1_nm,cmplnt_apr_level_2_nm,cmplnt_apr_level_3_nm,"
				+ "cmplnt_apr_level_1,cmplnt_apr_level_2,cmplnt_apr_level_3,cmplnt_prod_level_1_nm,cmplnt_prod_level_2_nm,"
				+ "cmplnt_prod_level_1,cmplnt_prod_level_2,dvlp_chnl_nm,dvlp_chnl,dvlp_chnl_level_1_nm,"
				+ "dvlp_chnl_level_1,dvlp_chnl_level_2_nm,dvlp_chnl_level_2,dvlp_chnl_level_3_nm,dvlp_chnl_level_3,"
				+ "recmplnt_times_30days,croscmplnt_times_30days,moneyback_90days,sheet_estima_30days,upgrade_trend,"
				+ "cust_type,member_level,serv_age,age,import_cust,"
				+ "dict_key,city_flag,offer_nm,offer_nbr,group_offer_nbr,"
				+ "fault_times_30days,fault_id_latest,deal_rslt_latest,longitude,latitude,"
				+ "problem_sort,problem_addr,deal_rslt,mntnce_staff_id,mntnce_staff_id_latest,"
				+ "develop_staff,DATE_FORMAT(accs_time,'%Y-%m-%d %H:%i:%s')accs_time,accept_staff,outbound_caller,main_offer_id,"
				+ "main_offer_nm,order_id,DATE_FORMAT(accept_date,'%Y-%m-%d %H:%i:%s')accept_date,rmng_flux,use_flux,"
				+ "rmng_voice,stop_type,DATE_FORMAT(stop_time,'%Y-%m-%d %H:%i:%s')stop_time,DATE_FORMAT(eff_time,'%Y-%m-%d %H:%i:%s')eff_time,DATE_FORMAT(exp_time,'%Y-%m-%d %H:%i:%s')exp_time,"
				+ "dispute_month,account_type,dispute_bill,DATE_FORMAT(recover_time,'%Y-%m-%d %H:%i:%s')recover_time,nbr_status,"
				+ "non_actvte_apr,charge_bill,charge_chnl,DATE_FORMAT(charge_time,'%Y-%m-%d %H:%i:%s')charge_time,good_nbr_level,"
				+ "oprt_type,DATE_FORMAT(open_date_prsnt,'%Y-%m-%d %H:%i:%s')open_date_prsnt,DATE_FORMAT(uninstall_time_pre,'%Y-%m-%d %H:%i:%s')uninstall_time_pre,is_dstnc_90days,unusual_apr,"
				+ "DATE_FORMAT(uninstall_time,'%Y-%m-%d %H:%i:%s')uninstall_time,message_status,DATE_FORMAT(unusual_time,'%Y-%m-%d %H:%i:%s')unusual_time,charge_nbr,error_tips,"
				+ "bal_type,transfer_acct,DATE_FORMAT(get_transcd_time,'%Y-%m-%d %H:%i:%s')get_transcd_time,DATE_FORMAT(portoutnet_time,'%Y-%m-%d %H:%i:%s')portoutnet_time,DATE_FORMAT(portinnet_time,'%Y-%m-%d %H:%i:%s')portinnet_time,"
				+ "portnet_status,caller_callee_nbr,rmnd_type,exchg_ord,exchg_chnl,"
				+ "DATE_FORMAT(exchg_time,'%Y-%m-%d %H:%i:%s')exchg_time,exchg_mtrl,visit_chnl,DATE_FORMAT(visit_shop_time,'%Y-%m-%d %H:%i:%s')visit_shop_time,argu_points,"
				+ "is_exchange_rcrd,print_chnl,invc_query_chnl,invc_mdfy_ctnt,DATE_FORMAT(push_time,'%Y-%m-%d %H:%i:%s')push_time,"
				+ "invc_push_mthd,bill_query_chnl,DATE_FORMAT(cntct_time,'%Y-%m-%d %H:%i:%s')cntct_time,reported_chnl,undesira_info_ctnt,"
				+ "harass_nbr,swindle_nbr,dispute_chnl_nm,dispute_chnl,dispute_chnl_1_nm,"
				+ "dispute_chnl_1,dispute_chnl_2_nm,dispute_chnl_2,dispute_chnl_3_nm,dispute_chnl_3,"
				+ "recall_record_no,recall_record,DATE_FORMAT(recall_time,'%Y-%m-%d %H:%i:%s')recall_time,recall_staff_id,reason,"
				+ "check_ctnt,is_timeout,timeout_rsn,is_slv,unrslv_rsn,"
				+ "is_raise_finish,is_valet_cmplnt,valet_cmplnt_map,rsn_level_1_nm,rsn_level_1,"
				+ "rsn_level_2_nm,rsn_level_2,rsn_level_3_nm,rsn_level_3,rsn_level_4_nm,"
				+ "rsn_level_4,rsn_level_5_nm,rsn_level_5,deal_result,DATE_FORMAT(end_date,'%Y-%m-%d %H:%i:%s')end_date,"
				+ "DATE_FORMAT(archive_date,'%Y-%m-%d %H:%i:%s')archive_date,is_charge_rcrd,dispute_return_rcrd,is_use_rcrd,rfd_rcrd,"
				+ "into_acct_rcrd,out_scomb_flux,threshold_rmnd_rcrd,blok_dept,blok_staff_id,"
				+ "real_name_status,DATE_FORMAT(estim_repair_time,'%Y-%m-%d %H:%i:%s')estim_repair_time,is_scnd_confirm,outbound_record_rcrd,outbound_staff_id,"
				+ "outbound_chnl,is_keep_appoint,charge_id,DATE_FORMAT(charge_date,'%Y-%m-%d %H:%i:%s')charge_date,get_bill,"
				+ "rfd_type,rfd_bill,rfd_id,pre_offer_nm,plan_chnge_offer_nm,"
				+ "vas,busi_status,use_ctnt,is_refer_ld,ld_bill,"
				+ "DATE_FORMAT(agrmnt_due_time,'%Y-%m-%d %H:%i:%s')agrmnt_due_time,busi_type,claim_sorc,spcl_claim_type,is_same_claim_type,"
				+ "comp_depart,comp_conc,sat_eval,comp_ip,comp_status,"
				+ "comp_dur,urge_sheet_cnt,redirct_times,if_punish,punish_mount,"
				+ "punish_depart,is_fair_complaint,action_flag,res_con_channel,nbr_location,"
				+ "sheet_type,target_nbr,jieu_nm,platform_nm,archive_ctnt,"
				+ "DATE_FORMAT(new_innet_time,'%Y-%m-%d %H:%i:%s')new_innet_time,accept_staff_cd,charge_phone_nbr,error_phone_nbr,is_recall,"
				+ "cust_name,offer_type,good_offer_name,res_quality,sheet_estima_30days_sat,"
				+ "sheet_estima_30days_dissat,cmplnt_source_cd,cert_type"
				+ " FROM dapd_sheet_info WHERE sheet_id_prov=?";
		List<Map<String, Object>> tmpList = jt.queryForList(str, sheetIdProv);
		if (tmpList.isEmpty()) {
			log.debug("没有查询到受理单号为:{}的DapdSheetInfo信息,请查证单号是否正确!", sheetIdProv);
			return null;
		}
		List<DapdSheetInfo> list = JSON.parseArray(JSON.toJSONString(tmpList), DapdSheetInfo.class);
		return list.get(0);
	}

	private String substring(String str, int endIndex, String defaultStr) {
		if (StringUtils.isEmpty(str)) {
			return defaultStr;
		}
		return StringUtils.substring(str, 0, endIndex);
	}

	public JdbcTemplate getJt() {
		return jt;
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}
}