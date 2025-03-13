package com.timesontransfar.complaintservice.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedContact;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedRepeat;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.pubFunction.IPUBFunctionService;
import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.complaintservice.handler.ComplaintDealHandler;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.pojo.PreAssessInfo;
import com.timesontransfar.complaintservice.service.IComplaint;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.common.uploadFile.dao.IAccessorieDao;
import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.service.impl.CompWorksheetFullWebService;
import com.timesontransfar.customservice.worksheet.dao.ISheetMistakeDAO;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.IUnsatisfyTemplateDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.dao.ItsDealQualitative;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.dao.IworkSheetAllotRealDao;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedContactDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedRepeatDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TSOrderMistake;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetDealType;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.timesontransfar.evaluation.SatisfyInfo;
import com.timesontransfar.evaluation.service.EvaluationService;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.timesontransfar.sheetHandler.ASheetDealHandler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComplaintWorksheetDealImpl implements IComplaintWorksheetDeal {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintWorksheetDealImpl.class);
    
    @Autowired
    private ItsDealQualitative tsDealQualitativeDaoImpl;// 部门处理定性表
    @Autowired
    @Qualifier("WorkFlowAttemper")
    private IWorkFlowAttemper workFlowAttemperImpl;// 工作流bean
    @Autowired
    private ISheetPubInfoDao sheetPubInfoDao;
    @Autowired
    private PubFunc pubFunc;
	@Autowired
    private InoteSenList noteSenListDao;
	@Autowired
    private IorderAskInfoDao orderAskInfoDao;
	@Autowired
    private IserviceContentDao serviceContentDao;
	@Autowired
    private IorderCustInfoDao orderCustInfoDao;
	@Autowired
    private IworkSheetAllotRealDao workSheetAllotReal;
	@Autowired
    private ItsWorkSheetDao tsWorkSheetDao;
	@Autowired
    private WorkSheetAllot workSheetAllot;
    @Autowired
    private ItsSheetQualitative sheetQualitative;// 投诉定性表
    @Autowired
    private ItsCustomerVisit customerVisit;// 投诉回访表
    @Autowired
	private IdbgridDataPub dbgridDataPub;
    
	/**
     * 标签存储表的操作实例
     */
    @Autowired
    private ILabelManageDAO labelManageDAO;
    @Autowired
    private ASheetDealHandler sheetDealHandler;
    @Autowired
    private ISheetMistakeDAO sheetMistakeDAO;
    @Autowired
    private CmpUnifiedRepeatDAOImpl cmpUnifiedRepeatDAOImpl;
    @Autowired
	private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
    @Autowired
	private IUnsatisfyTemplateDao unsatisfyTemplate;
	@Autowired
	private CmpUnifiedContactDAOImpl cmpUnifiedContactDAOImpl;
	@Autowired
    private CompWorksheetFullWebService compFull;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDealImpl;
	@Autowired
	private IComplaint complaintImpl;
	@Autowired
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
	@Autowired
	private EvaluationService evaluation;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;
	@Autowired
	private IPUBFunctionService pubFunction;
	@Autowired
	private IAccessorieDao accessorieDao;
	
	/**
	 * 调账挂起
	 * @param workSheetId 工单号
	 * @param endDate 到期解除隐藏时间
	 * @return
	 */
	public int hangupForTZ(String workSheetId, String endDate) {
		SheetPubInfo spi = sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
		if (!(StaticData.TACHE_DEAL_NEW == spi.getTacheId() || StaticData.TACHE_ASSIGN == spi.getTacheId() || StaticData.TACHE_DEAL == spi.getTacheId())) {
			return 0; // 非部门处理
		}
		if (ComplaintDealHandler.isHoldSheet(spi.getSheetStatu())) {
			return 0; // 已经挂起
		}
		int state = spi.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_HOLD_STATE_NEW : StaticData.WKST_HOLD_STATE;
		if ("".equals(endDate)) {
			return sheetDealHandler.hangup(workSheetId, "工单池调账挂起", 9, state, 0);
		} else {
			if (1 == sheetDealHandler.hangup(workSheetId, "我的任务调账挂起", 9, state, 1)) {
				return sheetDealHandler.hidden(workSheetId, endDate);
			}
		}
		return 0;
	}

    /**
     * 隐藏工单
     * @param workSheetId 工单号
     * @param endDate 到期日期
     * @return
     */
    public int hiddenSheet(String workSheetId, String endDate) {
        return sheetDealHandler.hiddenSheet(workSheetId, endDate);
    }

    /**
     * 手动解除隐藏
     * @param workSheetId 工单号
     * @return
     */
    public int stopHide(String workSheetId) {
        return sheetDealHandler.stopHide(workSheetId);
    }

	/**
	 * 查询最近一次挂起动作类型
	 * @param workSheetId 工单号
	 * @return 动作ID
	 */
    public int queryLastActionCodeBySheetId(String workSheetId) {
    	return sheetDealHandler.queryLastActionCodeBySheetId(workSheetId);
    }

    /**
	 * 调账解挂
	 * @param workSheetId 工单号
	 * @return
	 */
	public int unHangupForTZ(String workSheetId) {
		SheetPubInfo spi = sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
		if (!ComplaintDealHandler.isHoldSheet(spi.getSheetStatu())) {
			return 0; // 非挂起状态
		}
		String unHangupRes = sheetDealHandler.unHangup(spi);
		logger.info("unHangup res:" + unHangupRes);
		if ("SUCCESS".equals(unHangupRes)) {
			String actionRes = sheetDealHandler.saveSheetDealAction(
					spi.getServiceOrderId(), 
					workSheetId, 
					spi.getRegionId(), 
					"调账解挂", 
					spi.getMonth(),
					spi.getTacheId(), 
					StaticData.WKST_TZ_UNHOLD_ACTION);
			logger.info("saveSheetDealAction res:" + actionRes);
			if ("SUCCESS".equals(actionRes)) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 手动取消隐藏
	 * @param worksheetId 工单号
	 * @return
	 */
	public int unHidden(String rows) {
		JSONArray json=JSONArray.fromObject(rows);
		String workdSheetId="";
		int num=0;
		for(int i=0;i<json.size();i++) {
			workdSheetId=json.getJSONObject(i).optString("WORK_SHEET_ID");
			num=sheetDealHandler.unHidden(workdSheetId)+num;
		}
		return num;
	}
	
	//@Transactional
    public String sumbitOrgBack(String worksheetId, int regionId, int month, String backReason) {
        Integer monthObj = month;
        SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(worksheetId, regionId, monthObj, true);
        if(isSTATUERROR(sheetInfo.getSheetStatu(), sheetInfo.getLockFlag())){
        	return "STATUERROR";
        }
        String flowSeq=pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(),"1",1);
        TsmStaff staff = pubFunc.getLogonStaff();
        sheetPubInfoDao.updateSheetDealRequire(worksheetId, sheetInfo.getDealRequire(), "", "部门处理退单", backReason, 55,StaticData.TACHE_ASSIGN_NEW);
        backReason = backReason.substring(1);
        // 记录处理类型
        TsSheetDealType typeBean = new TsSheetDealType();
        String guid = this.pubFunc.crtGuid();
        typeBean.setDealTypeId(guid);
        typeBean.setOrderId(sheetInfo.getServiceOrderId());
        typeBean.setWorkSheetId(worksheetId);
        typeBean.setDealType("部门处理退回派单");
        typeBean.setDealTypeDesc("退回派单");
        typeBean.setDealId(0);// ID
        typeBean.setDealDesc("是");
        typeBean.setDealContent(backReason);// 处理内容
        typeBean.setMonth(monthObj);
        tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型

        Map otherParam = new HashMap();
        otherParam.put("DEAL_REQUIRE", backReason);
        otherParam.put("ROUTE_VALUE", StaticData.ROUTE_DEAL_TO_ASSIGN);
        otherParam.put("DEAL_PR_ORGID", staff.getOrganizationId());// 派发部门
        otherParam.put("DEAL_PR_STAFFID", staff.getId());// 派发员工
        otherParam.put("FLOW_SEQUENCE", flowSeq);
        submitWorkFlow(worksheetId, regionId, monthObj, otherParam);
        return "SUCCESS";
    }

    /**
     * 判断是否是最后一张审批或内审批
     */
	public boolean checkLastDeal(String sheetId, int sheetType, int curMonth) {
		boolean flag = true;
		SheetPubInfo curSheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		if (ComplaintDealHandler.isHoldSheet(curSheetInfo.getSheetStatu())) {
			return false;
		}
		if (curSheetInfo.getLockFlag() != 1) {
			return false;
		}
		if (StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW != sheetType && StaticData.SHEET_TYPE_TS_CHECK_DEAL != curSheetInfo.getSheetType()) {
			return true;
		}
		
		WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(sheetId, curMonth);
		if (sheetAllotRealList.length > 0) {
			WorkSheetAllotReal sheetAllotRealobj = null;
			for (int j = sheetAllotRealList.length - 1; j >= 0; j--) {
				sheetAllotRealobj = sheetAllotRealList[j];
				if (sheetAllotRealobj.getMainSheetFlag() == 1) {
					if (sheetAllotRealobj.getCheckFalg() == 0) {
						return false;
					}
					return "0".equals(sheetAllotRealobj.getPreDealSheet());
				}
			}
		}
		return flag;
	}

    /**
     * 判断是否能够直接生成预定性工单
     */
	public boolean checkYDXCreate(String curSheetId, int sheetType, String rcvOrgId, int curMonth) {
        boolean flag = true;
        // 判断是否不需要经过预定性环节
        if (pubFunc.checkSpecialOrg(pubFunc.getAreaOrgId(rcvOrgId), 1)) {
            return false;
        }
        // 部门内审批工单，查询本审批单对应的派单关系记录
        if (StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW != sheetType) {
        	return true;
        }
        
        WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(curSheetId, curMonth);
        if (sheetAllotRealList.length > 0) {
            WorkSheetAllotReal sheetAllotRealobj = null;
            for (int j = sheetAllotRealList.length - 1; j >= 0; j--) {
                sheetAllotRealobj = sheetAllotRealList[j];
                if (sheetAllotRealobj.getMainSheetFlag() == 1) {
                    if (sheetAllotRealobj.getCheckFalg() == 0) {
                        return false;
                    }
                    return "0".equals(sheetAllotRealobj.getPreDealSheet());
                }
            }
        }
        return flag;
    }

    /**
     * 模拟生成预定性单，判断是否权限查看工单池，返回工单池预定性单的数量
     */
    public int checkYDXAuthority(String servOrderId) {
        return pubFunc.checkYDXAuthority(servOrderId);
    }

    @SuppressWarnings({ "all" })
    private String submitOrgDeal(SheetPubInfo sheetPubInfo, int delalId, String dealName, SheetPubInfo curSheetInfo, String batchFinish) {
        String curSheetId = sheetPubInfo.getWorkSheetId();
        int curRegionId = sheetPubInfo.getRegionId();
        Integer curMonth = sheetPubInfo.getMonth();

        boolean goNext = false;
        if (StaticData.SHEET_TYPE_TS_DEAL_NEW == curSheetInfo.getSheetType()) {
            if (curSheetInfo.getMainType() == 1) {
                // “部门处理工单”无派单关系记录，因为没有审批单
                goNext = true;
            }
        } else {
            String allotWorkSheet = ""; // 审批上级工单
            WorkSheetAllotReal workSheetAllotinfo = new WorkSheetAllotReal(); // 更新派单关系表审批状态或处理状态
            // 部门内审批工单
            if (StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW == curSheetInfo.getSheetType()) {
                // 查询本审批单对应的派单关系记录
                WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(
                        curSheetInfo.getWorkSheetId(), curSheetInfo.getMonth().intValue());
                if (sheetAllotRealList.length > 0) {
                    WorkSheetAllotReal sheetAllotRealobj = null;
                    for (int j = sheetAllotRealList.length - 1; j >= 0; j--) {
                        sheetAllotRealobj = sheetAllotRealList[j];
                        if (sheetAllotRealobj.getMainSheetFlag() == 1) {
                            if (sheetAllotRealobj.getCheckFalg() == 0) {
                                return "ALLOTREAL";
                            }
                            // 获得上级工单的工单号
                            allotWorkSheet = sheetAllotRealobj.getPreDealSheet(); // 除了工单号，该值也可能为0或1
                            break;
                        }
                    }
                }
                workSheetAllotinfo.setDealStauts("审批通过");
                workSheetAllotinfo.setWorkSheetId(allotWorkSheet);
            } else {
            	workSheetAllotinfo.setDealStauts("处理完成");
            	workSheetAllotinfo.setWorkSheetId(curSheetId);
            }
            workSheetAllotinfo.setMonth(curMonth);
            workSheetAllotinfo.setCheckFalg(1);

            if ("0".equals(workSheetAllotinfo.getWorkSheetId())) {
                goNext = true;
            } else if (!"1".equals(workSheetAllotinfo.getWorkSheetId())) {
            	workSheetAllotReal.updateWorkSheetAllotReal(workSheetAllotinfo);
                WorkSheetAllotReal sheetAllotObj = workSheetAllotReal.getSheetAllotObj(
                		workSheetAllotinfo.getWorkSheetId(), curMonth);
                if (null != sheetAllotObj) {
                // 根据上级派单关系对象的check工单ID，得到待审批的对象
                SheetPubInfo orderSheetInfo = this.sheetPubInfoDao.getSheetObj(
                        sheetAllotObj.getCheckWorkSheet(), curRegionId, curMonth, true);
                if (sheetAllotObj.getMainSheetFlag() == 1) { // 派单关系中的主办标志为'主办'
                    int sendType = -1;

                    // 投诉单处理完成 自动回单
                    String result = workSheetAllot.allotToApprove(orderSheetInfo);
                    if (WorkSheetAllot.RST_NONE.equals(result)) {
                        orderSheetInfo.setSheetStatu(StaticData.WKST_REPEAL_STATE_NEW);
                        orderSheetInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_REPEAL_STATE_NEW));
                        orderSheetInfo.setMonth(14);
                        orderSheetInfo.setLockFlag(0);
                        sendType = 0;
                    } else {
                        sheetPubInfoDao.updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(),
                                orderSheetInfo.getRcvStaffId(), orderSheetInfo.getRcvStaffName(),
                                orderSheetInfo.getRcvOrgId(), orderSheetInfo.getRcvOrgName());
                        sendType = 1;
                    }

                    sheetPubInfoDao.updateSheetState(orderSheetInfo.getWorkSheetId(),
                            orderSheetInfo.getSheetStatu(), orderSheetInfo.getSheetSatuDesc(),
                            14, orderSheetInfo.getLockFlag());

                    if (sendType >= 0) {
                        sendNoteCont(orderSheetInfo, sendType, curSheetInfo.getDealStaffId());
                    }
                }
                }
            }
        }

        String stateDesc = pubFunc.getStaticName(StaticData.WKST_FINISH_STATE_NEW);
        sheetPubInfoDao.updateSheetState(curSheetId, StaticData.WKST_FINISH_STATE_NEW, stateDesc,
                curMonth, 2);
        sheetPubInfoDao.updateSheetFinishDate(curSheetId);

        // 记录处理类型
        TsSheetDealType typeBean = new TsSheetDealType();
        String guid = pubFunc.crtGuid();
        typeBean.setDealTypeId(guid);
        typeBean.setOrderId(curSheetInfo.getServiceOrderId());
        typeBean.setWorkSheetId(curSheetInfo.getWorkSheetId());
        typeBean.setDealType("部门处理回单");
        typeBean.setDealTypeDesc("处理定性");
        typeBean.setDealId(delalId);// 处理定性ID
        typeBean.setDealDesc(dealName);// 处理定性名
        typeBean.setDealContent(sheetPubInfo.getDealContent());// 处理内容
        typeBean.setMonth(curSheetInfo.getMonth());

        if (curSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW) {
            typeBean.setDealType("部门内审批回单");
            typeBean.setDealTypeDesc("审批意见");
            typeBean.setDealId(1);// 处理定性ID 如果为审批单,0为不同意,1为同意
            typeBean.setDealDesc("审批同意");// 处理定性名
        } else if (curSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_IN_DEAL) {
            typeBean.setDealType("部门内处理回单");
            typeBean.setDealTypeDesc("处理定性");
        }

        if(curSheetInfo.getPrecontractSign() == 1){
        	labelManageDAO.saveFormalAnswerDate(curSheetInfo.getServiceOrderId());
        }
        tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型

        if (goNext) {
            // 判断是否不需要经过预定性环节
            boolean noPre = checkNoPre(curSheetInfo.getRcvOrgId(), batchFinish);
            Map otherParam = new HashMap();
            String dealOrg = "";
            String dealStaffId = "";
            if (curSheetInfo.getDealStaffId() == 0){
                TsmStaff staffObj = pubFunc.getLogonStaff();
                dealOrg = staffObj.getOrganizationId();
                dealStaffId = staffObj.getId();
            } else {
                dealOrg = curSheetInfo.getDealOrgId();
                dealStaffId = Integer.toString(curSheetInfo.getDealStaffId());
            }
            otherParam.put("DEAL_PR_ORGID", dealOrg);
            otherParam.put("DEAL_PR_STAFFID", dealStaffId);
            if (noPre) {
                otherParam.put("ROUTE_VALUE", StaticData.ROUTE_DEAL_TO_FINASSESS);
                sheetPubInfoDao.updateSheetDealRequire(curSheetId, curSheetInfo.getDealRequire(),
                        "", "部门处理到终定性", sheetPubInfo.getDealContent(), 60,
                        StaticData.TACHE_ZHONG_DINGXING_NEW);
            } else {
                otherParam.put("ROUTE_VALUE", StaticData.ROUTE_GOTO_NEXT);
                sheetPubInfoDao.updateSheetDealRequire(curSheetId, curSheetInfo.getDealRequire(),
                        "", "部门处理到预定性", sheetPubInfo.getDealContent(), 59,
                        StaticData.TACHE_DINGXING_NEW);
            }
            labelManageDAO.updateLastAnswerDate(curSheetInfo.getServiceOrderId()); // 2014-03-06 更新最后回复时间
            return submitWorkFlow(curSheetId, curRegionId, sheetPubInfo.getMonth(), otherParam);
        } else {
        	sheetPubInfoDao.updateSheetDealRequire(curSheetId, curSheetInfo.getDealRequire(), "",
                    "部门处理环节回单", sheetPubInfo.getDealContent(), 6, 10);
            return "SUCCESS";
        }
    }

	private boolean checkNoPre(String rcvOrgId, String batchFinish) {
		if ("1".equals(batchFinish)) {
			return true;
		}
		return pubFunc.checkSpecialOrg(pubFunc.getAreaOrgId(rcvOrgId), 1);
	}

    //@Transactional
    public String sumbitOrgDeal(SheetPubInfo sheetPubInfo, int delalId, String dealName, String batchFinish) {
        String curSheetId = sheetPubInfo.getWorkSheetId();
        // 通过工单的基本信息获取其完整信息
        SheetPubInfo curSheetInfo = sheetPubInfoDao.getSheetPubInfo(curSheetId, false);
        if (StaticData.WKST_DEALING_STATE_NEW != curSheetInfo.getSheetStatu() || 1 != curSheetInfo.getLockFlag()) {
            return "STATUSERROR";
        }
        if (StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW == curSheetInfo.getSheetType()) {// 部门内审批工单
            // 查询本审批单对应的派单关系记录
            WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(curSheetInfo.getWorkSheetId(), curSheetInfo.getMonth().intValue());
            WorkSheetAllotReal sheetAllotRealobj = null;
            for (int j = sheetAllotRealList.length - 1; sheetAllotRealList.length > 0&&j >= 0; j--) {
                sheetAllotRealobj = sheetAllotRealList[j];
                if (sheetAllotRealobj.getMainSheetFlag() == 1) {
                    if (sheetAllotRealobj.getCheckFalg() == 0) {
                        return "ALLOTREAL";
                    }
                    break;
                }
            }
        }

        String submitOrgDealFlag = submitOrgDeal(sheetPubInfo, delalId, dealName, curSheetInfo, batchFinish);
		if ("SUCCESS".equals(submitOrgDealFlag)) {
			tsWorkSheetDealImpl.jtxcCancel(curSheetId);
		}
		return submitOrgDealFlag;
    }

	/**
	 * {@inheritDoc}
	 */
	public String submitPreAssess(JSONObject requestJson, PreAssessInfo preInfo) {
		TsSheetQualitative bean = preInfo.getBean();
		int regionId = preInfo.getRegionId();
		int month = preInfo.getMonth();
		String dealContent = preInfo.getDealContent();
		int upgradeIncline = preInfo.getUpgradeIncline();
		String contactStatus = preInfo.getContactStatus();
		String requireUninvited = preInfo.getRequireUninvited();
		String unifiedCode = preInfo.getUnifiedCode();
		String uccJTSS = preInfo.getUccJTSS();
		String sheetId = bean.getSheetId();
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, regionId, month, true);
		if (isSTATUERROR(sheetInfo.getSheetStatu(), sheetInfo.getLockFlag())) { // 非处理中状态的单子不能提交
			return "STATUERROR";
		}
		// 记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		String serviceId = sheetInfo.getServiceOrderId();
		typeBean.setDealTypeId(pubFunc.crtGuid());
		typeBean.setOrderId(serviceId);
		typeBean.setWorkSheetId(sheetId);
		typeBean.setDealType("部门预定性");
		typeBean.setDealTypeDesc("");
		typeBean.setDealId(0);// 处理定性ID
		typeBean.setDealDesc("");// 处理定性名
		typeBean.setDealContent(dealContent);// 处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		typeBean.setUpgradeIncline(upgradeIncline);
		tsWorkSheetDao.saveSheetDealType(typeBean);
		labelManageDAO.updateUpgradeIncline(serviceId, upgradeIncline);
		sheetQualitative.saveTsSheetQualitative(bean); // 记录投诉定性内容
        labelManageDAO.saveQualitative(bean.getControlAreaSec(), bean.getDutyOrg(), pubFunc.getLastYY(bean), serviceId);
		sheetPubInfoDao.updateSheetDealRequire(sheetId, sheetInfo.getDealRequire(), "", "预定性到终定性", dealContent, 61, StaticData.TACHE_ZHONG_DINGXING_NEW);

		// 提交工作流
		Map otherParam = new HashMap();
		TsmStaff staffObj = pubFunc.getLogonStaff();
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_GOTO_NEXT);
		otherParam.put("DEAL_PR_ORGID", staffObj.getOrganizationId());// 派发部门
		otherParam.put("DEAL_PR_STAFFID", staffObj.getId());// 派发员工
		otherParam.put("FLOW_SEQUENCE", String.valueOf(pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(), "1", 1)));// 流水号
		submitWorkFlow(sheetId, regionId, sheetInfo.getMonth(), otherParam);
		
		//保存最终处理意见标识：0-存空 1-存工单号
		if(requestJson.containsKey("isFinalOption") && "1".equals(requestJson.optString("isFinalOption"))) {
			labelManageDAO.saveFinalOptionLabel(bean.getOrderId(), bean.getSheetId());
		}
		if(requestJson.containsKey("receiptEval")) {
			JSONObject receiptJson = requestJson.getJSONObject("receiptEval");
			if(receiptJson!=null && !receiptJson.isEmpty()) {
				//保存定性环节 工单处理质量评价
				tsDealQualitativeDaoImpl.saveReceiptEval(receiptJson);
			}
		}
		
		// 根据预定性单去查找对应的终定性工单ID
		String str = " AND sheet_type = 720130017 AND source_sheet_id='" + sheetId + "'";
		List sheetList = sheetPubInfoDao.getSheetCondition(str, true);
		if (!sheetList.isEmpty()) {
			Map map = (Map) sheetList.get(0);
			SheetPubInfo finSheetBean = sheetPubInfoDao.getSheetPubInfo(map.get("WORK_SHEET_ID").toString(), false);
			autoVisitYDX(bean, finSheetBean, contactStatus, requireUninvited, unifiedCode, uccJTSS);
		}
		return "SUCCESS";
	}

	private String autoVisitYDX(TsSheetQualitative qualitative, SheetPubInfo finSheetBean, String contactStatus, String requireUninvited, String unifiedCode, String uccJTSS) {
		String orderId = finSheetBean.getServiceOrderId();
		if (labelManageDAO.selectAutoVisitFlag(orderId) > 0) {// 这不是订单第一次进入自动回访
			return doAllot(qualitative, finSheetBean);
		}
		String preSheetId = qualitative.getSheetId();
		String unifiedComplaintCode = getUnifiedComplaintCode(orderId);
		if ("".equals(unifiedComplaintCode)) {
			sheetPubInfoDao.updateAutoVisit(4, 10, 1, preSheetId);
			return doAllot(qualitative, finSheetBean);
		}
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		/* 自动回访状态标识：3自动回访接口异常、4未进入自动回访
		  回访原因标识：
		  【3】2规定时间无结果
		  4送自动回访失败
		  【4】1受理时直接办结
		  2后台派单直接办结
		  3部门处理直接办结（E通）
		  4投诉级别为申诉
		  5集团来单
		  6预定性选择转派
		  7地域不满足
		  8协办流程未结束
		  9受理渠道为省服务监督热线
		  10没有集团编码
		  11非省投自处理
		  12非省投诉_分析审核岗
		  13非省集中10000号
		  14领导管控
		  15预定性选择联系不上用户
		  16预定性选择重单
		  17预定性选择重集团/申诉单
		  18用户要求不回访
		  19黑名单号码
		 */
		boolean res = false;
		String result = "进入集团即时测评";
		SatisfyInfo si = new SatisfyInfo();
		si.setServiceOrderId(orderId);
		si.setWorkSheetId(preSheetId);
		si.setUnifiedComplaintCode(unifiedComplaintCode);
		si.setContactStatus(0);
		si.setRequireUninvited(0);
		si.setIsRepeat(0);
		si.setIsUpRepeat(0);
		int autoVisitFlag = 0;// 自动回访状态标识
		int reportNum = 0;
		if ("1".equals(contactStatus)) {
			si.setContactStatus(1);
			autoVisitFlag = 4;
			reportNum = 15;
			res = true;
			result = "联系不上用户";
		}
		if ("1".equals(requireUninvited)) {
			si.setRequireUninvited(1);
			autoVisitFlag = 4;
			reportNum = 18;
			res = true;
			result = "用户要求不回访";
		}
		if (unifiedCode.length() > 0) {
			si.setIsRepeat(1);
			autoVisitFlag = 4;
			reportNum = 16;
			res = true;
			result = "重单：" + unifiedCode;
		}
		if (isUccJTSS(uccJTSS, orderInfo.getComeCategory())) {
			si.setIsUpRepeat(1);
			autoVisitFlag = 4;
			reportNum = 17;
			res = true;
			result = "重集团/申诉单：" + uccJTSS;
		}
		//是否即时测评黑名单用户
		if(evaluation.insertSatisfyInfoYDX(si, orderInfo.getRelaInfo(), orderInfo.getProdNum())) {
			autoVisitFlag = 4;
			reportNum = 19;
			res = true;
			result = "用户要求不测评";
		}
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 1, preSheetId);
		labelManageDAO.updateAutoVisitFlag(2, orderId);
		defaultVisit(orderInfo, qualitative);
		saveDxFinishDate(orderId, unifiedComplaintCode, null, null, 0);
		if (res) {
			return finishZDHFZDX(qualitative, finSheetBean, result);
		}
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_AUTOVISIT_STATU, orderInfo.getMonth(), pubFunc.getStaticName(StaticData.OR_AUTOVISIT_STATU));
		sheetPubInfoDao.updateSheetState(finSheetBean.getWorkSheetId(), StaticData.WKST_ALLOT_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_ALLOT_STATE_NEW), orderInfo.getMonth(), 0);
		return "DONE";
	}

	private void defaultVisit(OrderAskInfo orderInfo, TsSheetQualitative qualitative) {
		String tsVisitResult = "处理满意度：";
		int collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BPJ;// 总体情况
		int tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;// 投诉处理结果
		int tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;// 投诉处理及时性
		int tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;// 投诉处理态度
		switch ((int) qualitative.getSatisfyId()) {
		case 600001166: // 满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_MY;
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
			tsVisitResult = tsVisitResult + "满意";
			break;
		case 600001167:// 一般
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_YB;
			tsDealResult = StaticData.TS_DEAL_RESULT_YB;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_YB;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_YB;
			tsVisitResult = tsVisitResult + "一般";
			break;
		case 600001168:// 不满意
		case 600001169:// 很不满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BMY;
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
			tsVisitResult = tsVisitResult + "不满意";
			break;
		default:// 未评价
			tsVisitResult = tsVisitResult + "未评价";
			break;
		}
		TScustomerVisit tcv = new TScustomerVisit();
		tcv.setServiceOrderId(orderInfo.getServOrderId());
		tcv.setWorkSheetId(qualitative.getSheetId());
		tcv.setMonth(orderInfo.getMonth());
		tcv.setRegionId(orderInfo.getRegionId());
		tcv.setRegionName(orderInfo.getRegionName());
		tcv.setReplyData(pubFunc.getSysDate());
		tcv.setCollectivityCircs(collectivityCircs);
		tcv.setCollectivityCircsName(pubFunc.getStaticName(collectivityCircs));
		tcv.setTsDealAttitude(tsDealAttitude);
		tcv.setTsDealAttitudeName(pubFunc.getStaticName(tsDealAttitude));
		tcv.setTsDealBetimes(tsDealBetimes);
		tcv.setTsDealBetimesName(pubFunc.getStaticName(tsDealBetimes));
		tcv.setTsDealResult(tsDealResult);
		tcv.setTsDealResultName(pubFunc.getStaticName(tsDealResult));
		tcv.setTsVisitResult(tsVisitResult);
		tcv.setVisitType("1");
		customerVisit.saveCustomerVisit(tcv);
		labelManageDAO.updateDealResult(orderInfo.getServOrderId(), tcv.getTsDealResult(), tcv.getTsDealResultName());
	}

	private String finishZDHFZDX(TsSheetQualitative qualitative, SheetPubInfo finSheetBean, String dealContent) {
		String orderId = finSheetBean.getServiceOrderId();
		String finSheetId = finSheetBean.getWorkSheetId();
		if (finSheetBean.getLockFlag() == 0) {// 若终定性单在大库中，则将工单状态从待提取改为已提
			sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_DEALING_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
		}
		qualitative.setSheetId(finSheetId);
		FinAssessInfo finAssessInfo = new FinAssessInfo();
		finAssessInfo.setQualitative(qualitative);
		finAssessInfo.setCustomerVisit(null);
		finAssessInfo.setRegionId(finSheetBean.getRegionId());
		finAssessInfo.setMonth(finSheetBean.getMonth());
		finAssessInfo.setDealContent(dealContent);
		finAssessInfo.setLogonFlag(0);
		finAssessInfo.setValiFlag(3);
		finAssessInfo.setUpgradeIncline(2);
		finAssessInfo.setContactStatus("");
		finAssessInfo.setRequireUninvited("");
		finAssessInfo.setUnifiedCode("");
		finAssessInfo.setUccJTSS("");
		String submitResult = submitFinAssess(finAssessInfo);
		if ("SUCCESS".equals(submitResult)) {
			ComplaintRelation cmpRelaOper = pubFunc.queryListByOid(orderId);
			if (cmpRelaOper != null && cmpRelaOper.getAssignType() == 3) {
				JSONObject info = new JSONObject();
				info.put("complaintWorksheetId", cmpRelaOper.getComplaintWorksheetId());
				info.put("serviceOrderId", orderId);
				info.put("reason", "省内已处理，请集团归档");
				info.put("type", "FINISH");
				accessCliqueServiceFeign.accessCliqueNew(info.toString());
			}
		}
		return "DONE";
	}

	private String doAllot(TsSheetQualitative qualitative, SheetPubInfo finSheetBean) {
		String result = "NONE";
		String orderId = finSheetBean.getServiceOrderId();
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if (isAutoZDXChannel(orderInfo.getAskChannelId())) {
			ComplaintRelation cmpRelaOper = pubFunc.queryListByOid(orderId);
			if (cmpRelaOper != null) {
				if (cmpRelaOper.getAssignType() == 1) {
					result = hangupJTZDX(qualitative, finSheetBean, cmpRelaOper);
				} else if (cmpRelaOper.getAssignType() == 2) {
					result = finishJTZDX(qualitative, finSheetBean, cmpRelaOper);// 终定性-归档-回复他省
				}
			}
		}
		if ("NONE".equals(result)) {
			result = workSheetAllot.allotToFinAssessAddForce(finSheetBean);
			if ("SUCCESS".equals(result)) {
				String finSheetId = finSheetBean.getWorkSheetId();
				sheetPubInfoDao.updateFetchSheetStaff(finSheetId, finSheetBean.getRcvStaffId(), finSheetBean.getRcvStaffName(), finSheetBean.getRcvOrgId(), finSheetBean.getRcvOrgName());
				sheetPubInfoDao.updateSheetState(finSheetId, finSheetBean.getSheetStatu(), finSheetBean.getSheetSatuDesc(), finSheetBean.getMonth(), 1);
			}
		}
		return "DONE";
	}

	private String hangupJTZDX(TsSheetQualitative qualitative, SheetPubInfo finSheetBean, ComplaintRelation cmpRelaOper) {
		String orderId = finSheetBean.getServiceOrderId();
		String finSheetId = finSheetBean.getWorkSheetId();
		SheetPubInfo htpd = sheetPubInfoDao.getLatestSheetByType(orderId, StaticData.SHEET_TYPE_TS_ASSING_NEW, 0);// 后台派单人
		if (null == htpd) {
			return "NONE";
		}
		TsmStaff stf = pubFunc.getStaff(htpd.getDealStaffId());
		if (finSheetBean.getLockFlag() == 0) {// 若终定性单在大库中，则将工单提取到后台派单人
			sheetPubInfoDao.updateFetchSheetStaff(finSheetId, Integer.parseInt(stf.getId()), stf.getName(), stf.getOrganizationId(), stf.getOrgName());
			sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_DEALING_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
		}
		qualitative.setSheetId(finSheetId);
		String submitResult = hangupAssess(qualitative, null, "预定性直接待确认");
		if ("SUCCESS".equals(submitResult)) {
			accessCliqueServiceFeign.updateRelaStatu(cmpRelaOper.getRelaGuid(), 20, "请尽快回复集团！");
			Map poolForm = new HashMap();
			poolForm.put("orderId", orderId);
			poolForm.put("complaintworksheetid", "");
			poolForm.put("prodNum", "");
			poolForm.put("cliqueOrderStatu", "selected");
			poolForm.put("assigntype", "selected");
			poolForm.put("receivecode", "selected");
			poolForm.put("assigncode", "selected");
			poolForm.put("openStaffType", "0");
			poolForm.put("comeFrom", "");
			poolForm.put("isTimeFlag", "false");
			poolForm.put("acceptDate", "");
			poolForm.put("sponFlag", "false");
			poolForm.put("sponsor", "");
			poolForm.put("sponsorId", "");
			poolForm.put("sponsorDate", "");
			Map param = new HashMap();
			param.put("currentPage", 1);
			param.put("poolForm", poolForm);
			String sql = pubFunction.getQryClieque(param);
			Map poolMap = orderAskInfoDao.qrCliqueList(sql);
			String count = poolMap.get("count").toString();
			if (!"0".equals(count)) {
				List list = (List) poolMap.get("list");
				Map map = (Map) list.get(0);
				JSONObject info = pubFunction.addFeedbackInfo(orderId);
				info.put("complaintWorksheetId", cmpRelaOper.getComplaintWorksheetId());
				info.put("serviceOrderId", orderId);
				info.put("applyProcessingObj", map.get("ASSIGN_CODE").toString());
				info.put("content", pubFunc.getLastDealContent(orderId));// 操作原因:传最后一个部门处理结果
				info.put("comment", "无");// 备注内容:填无就行
				info.put("complaintReason", pubFunc.getLastYY(qualitative));// 办结原因:传省内办结原因，四类工单改造后传最新的办结原因
				info.put("auditResult", "Y");// 审核结果:传通过
				info.put("dealPerson", stf.getLogonName());// 处理人:后台派单人
				info.put("dealPhone", "025-83559711");// 处理人电话:025-83559711
				info.put("reason", "不成立");// 定责理由:传不成立
				FileRelatingInfo[] fris = accessorieDao.quryFileInfoNotInJT(orderId);
				if (fris.length == 0) {
					info.put("ftpFileIds", null);
				} else {
					int len = fris.length;
					String[] ftpFileIds = new String[len];
					for (int i = 0; i < len; i++) {
						ftpFileIds[i] = fris[i].getFtpId();
					}
					info.put("ftpFileIds", ftpFileIds);// 本地上传:自动上传省内工单中附件
				}
				info.put("type", "FEEDBACK");
				accessCliqueServiceFeign.accessCliqueNew(info.toString());
				return "SUCCESS";
			}
		}
		return "NONE";
	}

	private String finishJTZDX(TsSheetQualitative qualitative, SheetPubInfo finSheetBean, ComplaintRelation cmpRelaOper) {
		String orderId = finSheetBean.getServiceOrderId();
		String finSheetId = finSheetBean.getWorkSheetId();
		if (finSheetBean.getLockFlag() == 0) {// 若终定性单在大库中，则将工单状态从待提取改为已提
			sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_DEALING_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
		}
		qualitative.setSheetId(finSheetId);
		FinAssessInfo finAssessInfo = new FinAssessInfo();
		finAssessInfo.setQualitative(qualitative);
		finAssessInfo.setCustomerVisit(null);
		finAssessInfo.setRegionId(finSheetBean.getRegionId());
		finAssessInfo.setMonth(finSheetBean.getMonth());
		finAssessInfo.setDealContent("预定性直接回复他省");
		finAssessInfo.setLogonFlag(0);
		finAssessInfo.setValiFlag(3);
		finAssessInfo.setUpgradeIncline(2);
		finAssessInfo.setContactStatus("");
		finAssessInfo.setRequireUninvited("");
		finAssessInfo.setUnifiedCode("");
		finAssessInfo.setUccJTSS("");
		String submitResult = submitFinAssess(finAssessInfo);
		if ("SUCCESS".equals(submitResult)) {
			accessCliqueServiceFeign.updateRelaStatu(cmpRelaOper.getRelaGuid(), 20, "省已处理");
			Map poolForm = new HashMap();
			poolForm.put("orderId", orderId);
			poolForm.put("complaintworksheetid", "");
			poolForm.put("prodNum", "");
			poolForm.put("cliqueOrderStatu", "selected");
			poolForm.put("assigntype", "selected");
			poolForm.put("receivecode", "selected");
			poolForm.put("assigncode", "selected");
			poolForm.put("openStaffType", "0");
			poolForm.put("comeFrom", "");
			poolForm.put("isTimeFlag", "false");
			poolForm.put("acceptDate", "");
			poolForm.put("sponFlag", "false");
			poolForm.put("sponsor", "");
			poolForm.put("sponsorId", "");
			poolForm.put("sponsorDate", "");
			Map param = new HashMap();
			param.put("currentPage", 1);
			param.put("poolForm", poolForm);
			String sql = pubFunction.getQryClieque(param);
			Map poolMap = orderAskInfoDao.qrCliqueList(sql);
			logger.info("qrCliqueList: {}", JSON.toJSON(poolMap));
			String count = poolMap.get("count").toString();
			if (!"0".equals(count)) {
				List list = (List) poolMap.get("list");
				Map map = (Map) list.get(0);
				JSONObject info = pubFunction.addFeedbackInfo(orderId);
				info.put("complaintWorksheetId", cmpRelaOper.getComplaintWorksheetId());
				info.put("serviceOrderId", orderId);
				info.put("applyProcessingObj", map.get("ASSIGN_CODE").toString());
				info.put("content", pubFunc.getLastDealContentHis(orderId));// 操作原因:传最后一个部门处理结果
				info.put("comment", "无");// 备注内容:填无就行
				info.put("complaintReason", pubFunc.getLastYY(qualitative));// 办结原因:传省内办结原因，四类工单改造后传最新的办结原因
				info.put("auditResult", "Y");// 审核结果:传通过
				info.put("dealPerson", pubFunc.getLastPDLogonNameHis(orderId));// 处理人:后台派单人
				info.put("dealPhone", "025-83559711");// 处理人电话:025-83559711
				info.put("reason", "不成立");// 定责理由:传不成立
				FileRelatingInfo[] fris = accessorieDao.quryFileInfoNotInJT(orderId);
				if (fris.length == 0) {
					info.put("ftpFileIds", null);
				} else {
					int len = fris.length;
					String[] ftpFileIds = new String[len];
					for (int i = 0; i < len; i++) {
						ftpFileIds[i] = fris[i].getFtpId();
					}
					info.put("ftpFileIds", ftpFileIds);// 本地上传:自动上传省内工单中附件
				}
				info.put("type", "FEEDBACK");
				accessCliqueServiceFeign.accessCliqueNew(info.toString());
				return "SUCCESS";
			} else {
				logger.error("qrCliqueList查询异常: {}", sql);
			}
		}
		return "NONE";
	}

	// 2023-10,以上渠道工单派发至分公司，增加“回复至集团”选项，选择该选项后，分公司回单不再经省投诉中心审核
	private boolean isAutoZDXChannel(int acceptChannelId) {
		return !"".equals(pubFunc.getSelRefundHotName(acceptChannelId, "15"));
	}

	private boolean isUccJTSS(String uccJTSS, int comeCategory) {
		return 707907001 == comeCategory && (!"".equals(uccJTSS));
	}

	private boolean noUnifiedComplaintCode(String serviceId) {
		ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(serviceId);
		return (null == cur);
	}

	private String getUnifiedComplaintCode(String serviceId) {
		ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(serviceId);
		if (null != cur) {
			String unifiedComplaintCode = cur.getUnifiedComplaintCode();
			if ("0".equals(cur.getResult()) && unifiedComplaintCode.length() > 0) {
				return unifiedComplaintCode;
			}
		}
		return "";
	}

	private boolean isSTATUERROR(int sheetStatu, int lockFlag) {
		return StaticData.WKST_DEALING_STATE_NEW != sheetStatu || 1 != lockFlag;
	}

	public int countSheetAreaByOrderId(String serviceOrderId, int areaFlag) {
		return sheetPubInfoDao.countWorkSheetAreaByOrderId(serviceOrderId, areaFlag);
	}

	public int queryWorkSheetAreaBySheetId(String workSheetId, int sheetType) {
		if (sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL || sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW) {
			SheetPubInfo upSheetInfo = sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
			workSheetId = upSheetInfo.getSourceSheetId();
		}
		return sheetPubInfoDao.selectWorkSheetAreaBySheetId(workSheetId);
	}
	
	private String checkFinAssessStatus(SheetPubInfo sheetInfo, OrderAskInfo orderInfo) {
		// 非处理中状态的单子不能提交
		if (1 != sheetInfo.getLockFlag()) {
            return "STATUERROR";
        }
        if(ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())){
        	if(StaticData.COME_FROM_SN == orderInfo.getAskSource()){
        		return "STATUERROR";
        	}else{
        		sheetDealHandler.unHangup(sheetInfo);
        	}
        }
        if (isSTATUERROR(sheetInfo.getSheetStatu(), sheetInfo.getLockFlag())) {
            return "STATUERROR";
        }
        return "";
	}

	public String submitFinAssess(FinAssessInfo info) {
    	TsSheetQualitative bean = info.getQualitative();
    	TScustomerVisit tscustomerVisit = info.getCustomerVisit();
        int regionId = info.getRegionId();
        int month = info.getMonth();
        String dealContent = info.getDealContent();
        int flag = info.getLogonFlag();
        int valiFlag = info.getValiFlag();
        int upgradeIncline = info.getUpgradeIncline();
        String contactStatus = info.getContactStatus();
        String requireUninvited = info.getRequireUninvited();
        String unifiedCode = info.getUnifiedCode();
        String uccJTSS = info.getUccJTSS();
        
        String sheetId = bean.getSheetId();
        SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, regionId, month, true);
        OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(sheetInfo.getServiceOrderId(), false);
        
        String errorStatus = this.checkFinAssessStatus(sheetInfo, orderInfo);
        if(!"".equals(errorStatus)) {
        	return errorStatus;
        }
        
        // 流水号
        String flowSeq=pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(),"1",1);
        String dealOrg = "";
        String dealStaffId = "0";
        if (flag == 1) {
            TsmStaff staffObj = pubFunc.getLogonStaff();
            dealOrg = staffObj.getOrganizationId();
            dealStaffId = staffObj.getId();
        } else if (flag == 2) {
        	dealOrg = sheetInfo.getDealOrgId();
        	dealStaffId = sheetInfo.getDealStaffId()+"";
        }
        String serviceId = sheetInfo.getServiceOrderId();
		TsSheetQualitative qualitativeOld = sheetQualitative.getLatestQualitativeByOrderId(serviceId, orderInfo.getRegionId());
		if (null != qualitativeOld && !qualitativeOld.getSheetId().equals(bean.getSheetId()) && !pubFunc.getLastYY(qualitativeOld).equals(pubFunc.getLastYY(bean))) {
			SheetPubInfo sheetInfoOld = sheetPubInfoDao.getSheetObj(qualitativeOld.getSheetId(), regionId, month, true);
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(serviceId);
			om.setWorkSheetId(qualitativeOld.getSheetId());
			om.setMistakeOrgId(sheetInfoOld.getDealOrgId());
			om.setMistakeStaffId(sheetInfoOld.getDealStaffId());
			om.setMistakeType(5);
			om.setCheckOrgId(dealOrg);
			om.setCheckStaffId(Integer.parseInt(dealStaffId));
			om.setOldInfo(pubFunc.getConcatYYDesc(qualitativeOld));
			om.setNewInfo(pubFunc.getConcatYYDesc(bean));
			sheetMistakeDAO.insertOrderMistake(om);
		}

        /*
         * 记录处理类型
         */
        TsSheetDealType typeBean = new TsSheetDealType();
        String guid = this.pubFunc.crtGuid();
        typeBean.setDealTypeId(guid);
        typeBean.setOrderId(serviceId);
        typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
        typeBean.setDealType("终定性");
        typeBean.setDealTypeDesc("");
        typeBean.setDealId(0);// 处理定性ID
        typeBean.setDealDesc("");// 处理定性名
        typeBean.setDealContent(dealContent);// 处理内容
        typeBean.setMonth(sheetInfo.getMonth());
        typeBean.setUpgradeIncline(upgradeIncline);
        tsWorkSheetDao.saveSheetDealType(typeBean);
        if (upgradeIncline != 2){
            labelManageDAO.updateUpgradeIncline(serviceId, upgradeIncline);
        }
        sheetQualitative.saveTsSheetQualitative(bean); // 记录投诉定性内容

        /*
         * 记录回访信息 CC_CUSTOMER_VISIT
         */
        if(tscustomerVisit!=null){
            tscustomerVisit.setRegionName(sheetInfo.getRegionName());
            tscustomerVisit.setMonth(sheetInfo.getMonth());
            customerVisit.saveCustomerVisit(tscustomerVisit);
            labelManageDAO.updateDealResult(serviceId, tscustomerVisit.getTsDealResult(), tscustomerVisit.getTsDealResultName());
        }
        // 在订单标签表中，记录订单的定性结果 ，包括责任定性、责任部门、办结原因
        labelManageDAO.saveQualitative(bean.getControlAreaSec(), bean.getDutyOrg(), pubFunc.getLastYY(bean), serviceId);
        // 订单标签表保存四强终判
        labelManageDAO.updateForceCfmFlag(serviceId, bean.getForceFlag());
        // 修改申诉是否有效
        labelManageDAO.updateValidFlag(serviceId, valiFlag);

		sheetPubInfoDao.updateTachSheetFinsh(serviceId, StaticData.WKST_FINISH_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_FINISH_STATE_NEW), 2,
					sheetInfo.getMonth(), StaticData.TACHE_DEAL_NEW);
		sheetPubInfoDao.updateSheetDealRequire(sheetId, sheetInfo.getDealRequire(), "", "终定性到竣工", dealContent, 65, StaticData.TACHE_FINISH_NEW);
		complaintImpl.complaintPostInfo(8, serviceId);
		if (autoVisitZDX(orderInfo, sheetId, contactStatus, requireUninvited, unifiedCode, uccJTSS)) {
			saveDxFinishDate(serviceId, null, orderInfo, sheetId, 0);
	        // 提交工作流
			Map otherParam = new HashMap();
			otherParam.put("DEAL_PR_ORGID", dealOrg);
			otherParam.put("DEAL_PR_STAFFID", dealStaffId);
			otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeq));
			otherParam.put("ROUTE_VALUE", StaticData.FINASSESS_TO_FINISH);
			submitWorkFlow(sheetId, regionId, sheetInfo.getMonth(), otherParam);
		}
		return "SUCCESS";
	}

	private void saveDxFinishDate(String orderId, String unifiedComplaintCode, OrderAskInfo orderInfo, String sheetId, int step) {
		String finishDate = labelManageDAO.queryFinishDate(orderId);
		if (StringUtils.isEmpty(finishDate)) {
			labelManageDAO.saveFinishDate(orderId);
			dapdSheetService.setDapdEndDate(orderId);
			labelManageDAO.updateOverTimeLabel(orderId);
			if (null == unifiedComplaintCode) {
				if (noUnifiedComplaintCode(orderId)) {
					return;
				}
				unifiedComplaintCode = getUnifiedComplaintCode(orderId);
			}
			if (null != orderInfo) {
				updateIVRDegree(orderInfo, sheetId);// 上传集团之前更新IVR满意度
			}
			if (!StringUtils.isEmpty(unifiedComplaintCode) && 2 != step) {// 等即时测评结果再传给集团
				compFull.insertSupplement(orderId);
			}
		}
	}

    private void updateIVRDegree(OrderAskInfo orderInfo, String workSheetId) {
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderInfo.getServOrderId());
		if (!crs.isEmpty()) {
			int reportNum = 1;
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get("SATISFY_DEGREE").toString();
			String judgeDate = cr.get("JUDGE_DATE") == null ? "" : cr.get("JUDGE_DATE").toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1": // 满意
				reportNum = 5;
				tsVisitResult = "即时测评结果满意";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "2":// 一般
				reportNum = 3;
				tsVisitResult = "即时测评结果一般";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "3":// 服务态度冷淡
				tsVisitResult = "服务态度冷淡";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "4":// 业务解释听不懂
				tsVisitResult = "业务解释听不懂";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "5":// 处理速度慢
				tsVisitResult = "处理速度慢";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "6":// 处理方案未达到期望值
				tsVisitResult = "处理方案未达到期望";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "7":// 问题未解决
				tsVisitResult = "问题未解决";
				invitedJudgeZDX(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			default:// 未评价
			}
		}
	}

	private void invitedJudgeZDX(OrderAskInfo orderInfo, String workSheetId, int score, String replyData, String tsVisitResult) {
		TScustomerVisit cv = new TScustomerVisit();
		cv.setServiceOrderId(orderInfo.getServOrderId());
		cv.setWorkSheetId(workSheetId);
		cv.setMonth(orderInfo.getMonth());
		cv.setRegionId(orderInfo.getRegionId());
		cv.setRegionName(orderInfo.getRegionName());
		if ("".equals(replyData)) {
			cv.setReplyData(pubFunc.getSysDate());
		} else {
			cv.setReplyData(replyData);
		}
		int collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BPJ;// 总体情况
		int tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;// 投诉处理结果
		int tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;// 投诉处理及时性
		int tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;// 投诉处理态度
		if (5 == score) { // 满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_MY;
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
		} else if (1 == score) { // 不满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BMY;
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
		} else if (3 == score) { // 一般
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_YB;
			tsDealResult = StaticData.TS_DEAL_RESULT_YB;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_YB;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_YB;
		}
		cv.setCollectivityCircs(collectivityCircs);
		cv.setCollectivityCircsName(pubFunc.getStaticName(collectivityCircs));
		cv.setTsDealAttitude(tsDealAttitude);
		cv.setTsDealAttitudeName(pubFunc.getStaticName(tsDealAttitude));
		cv.setTsDealBetimes(tsDealBetimes);
		cv.setTsDealBetimesName(pubFunc.getStaticName(tsDealBetimes));
		cv.setTsDealResult(tsDealResult);
		cv.setTsDealResultName(pubFunc.getStaticName(tsDealResult));
		cv.setTsVisitResult(tsVisitResult);
		cv.setVisitType("3");
		customerVisit.saveCustomerVisit(cv);
		labelManageDAO.updateDealResult(orderInfo.getServOrderId(), cv.getTsDealResult(), cv.getTsDealResultName());
	}

	public void submitFinAssessFromVisit(String sheetId, String orderId, int region, Integer month) {
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, month, pubFunc.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));// 更新订单状态为终定性
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, region, month, true);
		String flowSeq = pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(), "1", 1);
		String dealOrg = sheetInfo.getDealOrgId();
		String dealStaffId = sheetInfo.getDealStaffId() + "";
		Map otherParam = new HashMap();
		otherParam.put("DEAL_PR_ORGID", dealOrg);
		otherParam.put("DEAL_PR_STAFFID", dealStaffId);
		otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeq));
		otherParam.put("ROUTE_VALUE", StaticData.FINASSESS_TO_FINISH);
		submitWorkFlow(sheetId, region, sheetInfo.getMonth(), otherParam);
	}

	public void toRGHFFromVisit(String sheetId, String orderId, int region, Integer month) {
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_RGHF_STATU, month, pubFunc.getStaticName(StaticData.OR_RGHF_STATU));// 更新订单状态为终定性
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, region, month, true);
		sheetPubInfoDao.updateSheetDealRequire(sheetId, sheetInfo.getDealRequire(), "", "终定性到人工回访", sheetInfo.getDealContent(), 63, StaticData.TACHE_RGHF);
		String flowSeq = pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(), "1", 1);
		String dealOrg = sheetInfo.getDealOrgId();
		String dealStaffId = sheetInfo.getDealStaffId() + "";
		Map otherParam = new HashMap();
		/**
		 * 1、后台派单直接处理，省投派单的回专家岗；
		 * 2、后台派单直接处理，非省投派单的回派单岗；
		 * 3、后台派单转派，最后一次部门处理为省投的回专家岗；
		 * 4、后台派单转派，最后一次部门处理为非省投的非携号转网类型的回处理岗；
		 * 5、后台派单转派，最后一次部门处理为非省投的携号转网类型的回后台派单人；
		 */
		String rghfOrgId = "365596"; // 专家岗
		SheetPubInfo bmcl = sheetPubInfoDao.queryLastSheetNoSystemByType(orderId, StaticData.SHEET_TYPE_TS_DEAL_NEW, 1);
		SheetPubInfo htpd = sheetPubInfoDao.queryLastSheetNoSystemByType(orderId, StaticData.SHEET_TYPE_TS_ASSING_NEW, 0);
		if (null == bmcl) {
			if (null != htpd && !pubFunc.isAffiliated(htpd.getRcvOrgId(), "361143")) {
				rghfOrgId = htpd.getRcvOrgId();
			}
		} else {
			if (!pubFunc.isAffiliated(bmcl.getRcvOrgId(), "361143")) {
				ServiceContent sc = serviceContentDao.getServContentByOrderId(orderId, false, 0);
				if (checkXHZW(sc.getAppealReasonId())) {
					if (null != htpd) {
						rghfOrgId = htpd.getRcvOrgId();
						otherParam.put("RGHF_STAFF_ID", htpd.getDealStaffId());
					}
				} else {
					rghfOrgId = bmcl.getRcvOrgId();
				}
			}
		}
		otherParam.put("RGHF_ORG_ID", rghfOrgId);
		otherParam.put("DEAL_PR_ORGID", dealOrg);
		otherParam.put("DEAL_PR_STAFFID", dealStaffId);
		otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeq));
		otherParam.put("DEAL_REQUIRE", "自动回访不满意回人工");
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_GOTO_NEXT);
		submitWorkFlow(sheetId, region, sheetInfo.getMonth(), otherParam);
	}

	// 判断是否为携号转网
	private boolean checkXHZW(int appealReasonId) {
		String str = String.valueOf(appealReasonId);
		return str.startsWith("111") || "23002503".equals(str);
	}

	private boolean autoVisitZDX(OrderAskInfo orderInfo, String sheetId, String contactStatus, String requireUninvited, String unifiedCode, String uccJTSS) {
		String serviceId = orderInfo.getServOrderId();
		if (labelManageDAO.selectAutoVisitFlag(serviceId) > 0) {// 这不是订单第一次进入自动回访
			return true;
		}
		int autoVisitFlag = 0;// 自动回访状态标识：3自动回访接口异常、4未进入自动回访
		int reportNum = 0;
		if (noUnifiedComplaintCode(serviceId)) {
			autoVisitFlag = 4;
			reportNum = 10;
			sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, sheetId);
			return true;
		}
		String unifiedComplaintCode = getUnifiedComplaintCode(serviceId);
		/* 回访原因标识：
		  【3】2规定时间无结果
		  4送自动回访失败
		  【4】1受理时直接办结
		  2后台派单直接办结
		  3部门处理直接办结（E通）
		  4投诉级别为申诉
		  5集团来单
		  6预定性选择转派
		  7地域不满足
		  8协办流程未结束
		  9受理渠道为省服务监督热线
		  10没有集团编码
		  11非省投自处理
		  12非省投诉_分析审核岗
		  13非省集中10000号
		  14领导管控
		  15预定性选择联系不上用户
		  16预定性选择重单
		  17预定性选择重集团/申诉单
		  18用户要求不回访
		  19黑名单号码
		 */
		boolean res = false;
		SatisfyInfo si = new SatisfyInfo();
		si.setServiceOrderId(serviceId);
		si.setWorkSheetId(sheetId);
		si.setUnifiedComplaintCode(unifiedComplaintCode);
		si.setContactStatus(0);
		si.setRequireUninvited(0);
		si.setIsRepeat(0);
		si.setIsUpRepeat(0);
		if ("1".equals(contactStatus)) {
			si.setContactStatus(1);
			autoVisitFlag = 4;
			reportNum = 15;
			res = true;
		}
		if ("1".equals(requireUninvited)) {
			si.setRequireUninvited(1);
			autoVisitFlag = 4;
			reportNum = 18;
			res = true;
		}
		if (unifiedCode.length() > 0) {
			si.setIsRepeat(1);
			autoVisitFlag = 4;
			reportNum = 16;
			res = true;
		}
		if (isUccJTSS(uccJTSS, orderInfo.getComeCategory())) {
			si.setIsUpRepeat(1);
			autoVisitFlag = 4;
			reportNum = 17;
			res = true;
		}
		if ("".equals(unifiedComplaintCode)) {
			autoVisitFlag = 4;
			reportNum = 10;
			res = true;
		}
		int zdxStep = evaluation.insertSatisfyInfoZDX(si, orderInfo.getRelaInfo(), orderInfo.getProdNum());// 0-默认值，1-黑名单，2-即时测评
		if (1 == zdxStep) {
			autoVisitFlag = 4;
			reportNum = 19;
			res = true;
		}
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, sheetId);
		labelManageDAO.updateAutoVisitFlag(2, serviceId);
		if (!res) {
			orderAskInfoDao.updateOrderStatu(serviceId, StaticData.OR_AUTOVISIT_STATU, orderInfo.getMonth(), pubFunc.getStaticName(StaticData.OR_AUTOVISIT_STATU));
			sheetPubInfoDao.updateSheetState(sheetId, StaticData.WKST_FINISH_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_FINISH_STATE_NEW), orderInfo.getMonth(), 2);
			sheetPubInfoDao.updateSheetFinishDate(sheetId);
			saveDxFinishDate(serviceId, unifiedComplaintCode, null, null, zdxStep);
		}
		return res;
	}

	//@Transactional
	public String submitRGHF(TScustomerVisit tscustomerVisit, String worksheetId, int regionId, int month) {
		Integer monthObj = month;
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(worksheetId, regionId, monthObj, true);
		if (isSTATUERROR(sheetInfo.getSheetStatu(), sheetInfo.getLockFlag())) {
			return "STATUERROR";
		}
		if (tscustomerVisit == null) {
			return "STATUERROR";
		}
		tscustomerVisit.setRegionName(sheetInfo.getRegionName());
		tscustomerVisit.setMonth(sheetInfo.getMonth());
		customerVisit.saveCustomerVisit(tscustomerVisit);
		labelManageDAO.updateDealResult(sheetInfo.getServiceOrderId(), tscustomerVisit.getTsDealResult(), tscustomerVisit.getTsDealResultName());
		String flowSeq = pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(), "1", 1);
		TsmStaff staffObj = pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staffObj.getId());
		String staffName = staffObj.getName();
		String dealOrg = staffObj.getOrganizationId();
		String dealOrgname = this.pubFunc.getOrgName(dealOrg);
		String dealStaffId = staffObj.getId();
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		typeBean.setDealType("人工回访");
		typeBean.setDealTypeDesc("人工回访");
		typeBean.setDealId(0);// 处理定性ID
		typeBean.setDealDesc("人工回访");// 处理定性名
		typeBean.setDealContent(tscustomerVisit.getTsVisitResult());// 处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型
		sheetPubInfoDao.updateFetchSheetStaff(worksheetId, staffId, staffName, dealOrg, dealOrgname);
		sheetPubInfoDao.updateSheetDealRequire(worksheetId, sheetInfo.getDealRequire(), "", "人工回访竣工", tscustomerVisit.getTsVisitResult(), 67,
				StaticData.TACHE_FINISH_NEW);
		orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		Map otherParam = new HashMap();
		otherParam.put("MONTH_FALG", sheetInfo.getMonth());
		otherParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
		otherParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
		otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeq));// 流水号
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_DIN_FIN);
		submitWorkFlow(worksheetId, regionId, monthObj, otherParam);
		return "FINISH";
	}

	/**
     * 工单到岗，发送短信提醒
     * 
     * @param bean
     *            工单实例
     * @param type
     *            发送类型。1 发送到个人；0发送到部门
     */
    private void sendNoteCont(SheetPubInfo bean, int type, int dealStaffId) {
    	if(pubFunc.getSystemAuthorization().getHttpSession() == null){
            return;
        }
        NoteSeand noteBean = null;
        String phone = "";
        String client;
        String sheetGuid;
        String relaPerson = "";
        List tmp = null;
        if (type == 0) {
            tmp = this.noteSenListDao.getNoteSendNum(bean.getRcvOrgId(), null, bean.getTacheId(), 0);
        } else {
            tmp = this.noteSenListDao.getNoteSendNum(bean.getRcvOrgId(),
                    String.valueOf(bean.getDealStaffId()), bean.getTacheId(), 1);
        }
        if(tmp == null) {
        	return;
        }
        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(bean.getServiceOrderId(),
                bean.getMonth(), false);

        String comment = orderAskInfo.getComment();
        int size = tmp.size();
        Map map = null;
        if (size > 0) {
            TsmStaff staff = null;
            if (dealStaffId == 2604457){
                staff = this.pubFunc.getLogonStaffByLoginName("JS15301588119");//取企业信息化部－张静
            } else {
                staff = this.pubFunc.getLogonStaff();//取当前登录员工信息
            }
            int staffId = Integer.parseInt(staff.getId());
            String staffName = staff.getName();
            String orgId = staff.getOrganizationId();
            String orgName = staff.getOrgName();

            for (int i = 0; i < size; i++) {
                map = (Map) tmp.get(i);
                noteBean = new NoteSeand();
                sheetGuid = this.pubFunc.crtGuid();
                phone = map.get("RELAPHONE").toString();
                client = map.get("CLIENT_TYPE").toString();
                relaPerson = map.get("RELA_PERSON").toString();
                noteBean.setSheetGuid(sheetGuid);
                noteBean.setRegionId(bean.getRegionId());
                noteBean.setDestteRmid(phone);
                noteBean.setClientType(Integer.parseInt(client));

                OrderCustomerInfo cus = orderCustInfoDao.getOrderCustByGuid(
                        orderAskInfo.getCustId(), false);
                String cn = cus.getCustName() == null ? "" : cus.getCustName();
                String ph = orderAskInfo.getRelaInfo();

                noteBean.setSendContent(relaPerson + "您好:有一条新的" + bean.getServTypeDesc()
                        + "单派发到你部门,服务单号为:" + bean.getServiceOrderId() + ",处理时限:"
                        + bean.getDealLimitTime() + "小时,受理的内容概述为:" + comment + ",客户姓名:" + cn
                        + ",联系电话:" + ph + ",请注意查收.");
                noteBean.setOrgId(orgId);
                noteBean.setOrgName(orgName);
                noteBean.setStaffId(staffId);
                noteBean.setStaffName(staffName);
                noteBean.setBusiId(bean.getWorkSheetId());
                this.noteSenListDao.saveNoteContent(noteBean);
            }
        }
    }

    /**
     * 提交工作流方法
     * 
     * @param sheetId
     * @param quryRegion
     * @param otherParam
     * @return
     */
    private String submitWorkFlow(String sheetId, int quryRegion, Integer month, Map otherParam) {
        SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId, quryRegion, month,
                true);
        if (sheetPubInfo == null) {
            logger.warn("没有查询到工单号为 : " + sheetId + "的工单!");
            return "SHEETERROR";
        }
        Map inParam = new HashMap();
        inParam.putAll(otherParam);
        inParam.put("SERV_ORDER_ID", sheetPubInfo.getServiceOrderId());
        inParam.put("SHEET_ID", sheetId);
        inParam.put("WF__REGION_ID", quryRegion + "");
        inParam.put("MONTH_FALG", month);

        String wfInstId = sheetPubInfo.getWflInstId();
        String wfNodeInstId = sheetPubInfo.getTacheInstId();
        workFlowAttemperImpl.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
        return "SUCCESS";
    }

    /**
     * 得到工单派发关系对象
     * 
     * @param checkSheetId
     *            审批/审核单的ID
     * @param month
     *            月分区
     * @return 查询结果
     */
	private WorkSheetAllotReal[] dealOrgAudSheet(String checkSheetId, int month) {
		String strWhere = "AND cc_worksheet_allot_rela.check_worksheet_id= '" + checkSheetId + "' "
                + "AND cc_worksheet_allot_rela.month_flag=" + month
                + " and cc_worksheet_allot_rela.main_sheet_flag=1";
		return this.workSheetAllotReal.getWorkSheetAllotReal(strWhere, true);
    }

    @Transactional(propagation=Propagation.REQUIRED)
    public String hangupAssess(TsSheetQualitative bean, TScustomerVisit tscustomerVisit, String dealContent) {
        String sheetId = bean.getSheetId();
        SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
        if (1 != sheetInfo.getLockFlag()) {
            return "STATUERROR";
        }
		TsSheetQualitative qualitativeOld = sheetQualitative
				.getLatestQualitativeByOrderId(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId());
		if (null != qualitativeOld && (!qualitativeOld.getSheetId().equals(bean.getSheetId())) && !pubFunc.getLastYY(qualitativeOld).equals(pubFunc.getLastYY(bean))) {
			TsmStaff staffObj = pubFunc.getLogonStaff();
			SheetPubInfo sheetInfoOld = sheetPubInfoDao.getSheetObj(qualitativeOld.getSheetId(), sheetInfo.getRegionId(), sheetInfo.getMonth(), true);
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(sheetInfo.getServiceOrderId());
			om.setWorkSheetId(qualitativeOld.getSheetId());
			om.setMistakeOrgId(sheetInfoOld.getDealOrgId());
			om.setMistakeStaffId(sheetInfoOld.getDealStaffId());
			om.setMistakeType(5);
			om.setCheckOrgId(staffObj.getOrganizationId());
			om.setCheckStaffId(Integer.parseInt(staffObj.getId()));
			om.setOldInfo(pubFunc.getConcatYYDesc(qualitativeOld));
			om.setNewInfo(pubFunc.getConcatYYDesc(bean));
			sheetMistakeDAO.insertOrderMistake(om);
		}
        // 记录临时回访信息 CC_CUSTOMER_VISIT_TMP
        if(tscustomerVisit!=null){
            tscustomerVisit.setRegionName(sheetInfo.getRegionName());
            tscustomerVisit.setMonth(sheetInfo.getMonth());
            customerVisit.saveCustomerVisitTmp(tscustomerVisit);
        }
        sheetPubInfoDao.updateDealContent(sheetId, dealContent);
        // 记录投诉定性内容
        sheetQualitative.saveTsSheetQualitative(bean);
        // 在订单标签表中，记录订单的定性结果 ，包括责任定性、责任部门、办结原因
        labelManageDAO.saveQualitative(bean.getControlAreaSec(), bean.getDutyOrg(), pubFunc.getLastYY(bean), sheetInfo.getServiceOrderId());
        // 订单标签表保存四强终判
        labelManageDAO.updateForceCfmFlag(sheetInfo.getServiceOrderId(), bean.getForceFlag());

        if (ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())) {
            // 记录挂动作信息
            sheetDealHandler.saveSheetDealAction(sheetInfo.getServiceOrderId(), sheetId,
                    sheetInfo.getRegionId(), dealContent, sheetInfo.getMonth(),
                    sheetInfo.getTacheId(), StaticData.WKST_FINASSESS_UPDATE);
            return "UPDATED";
        } else {
			saveDxFinishDate(sheetInfo.getServiceOrderId(), null, null, null, 0);
            // 挂起工单
            return sheetDealHandler.hangup(sheetId, dealContent);
        }
    }

    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public String dispatchAssess(String worksheetId, String[] main, String[] sub, String dealContent, int dealLimit, int upgradeIncline) {
    	SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetPubInfo(worksheetId, false);
        
        int state = sheetInfo.getLockFlag();
        // 非处理中状态的单子不能提交
        if (state != 1) {
            return "STATUSERROR";
        }
        OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(sheetInfo.getServiceOrderId(), false);
        if(ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())){
        	if(StaticData.COME_FROM_SN == orderInfo.getAskSource()){
        		return "STATUERROR";
        	}
        	sheetDealHandler.unHangup(sheetInfo);
        }
        
        String[] subOrgIds = new String[0];
        String[] subOrgNames = new String[0];
        String[] subStaffIds = new String[0];
        String[] subStaffNames = new String[0];
        int size = 1;
        if(sub.length > 0) {
            if ("0".equals(sub[0])) {
            	subStaffIds = sub[1].split(",");
            	subStaffNames = sub[2].split(",");
            	size = subStaffIds.length + 1;
            } else if ("1".equals(sub[0])) {
            	subOrgIds = sub[1].split(",");
            	subOrgNames = sub[2].split(",");
            	size = subOrgIds.length + 1;
            }
        }
        StringBuilder orgListStr = new StringBuilder("");
        String mainOrg = "主办单位: ";
        String assitOrg = "      协办单位: ";
        SheetPubInfo[] sheets = new SheetPubInfo[size];
        /* 生成主办单 */
        SheetPubInfo tmp = new SheetPubInfo();
        tmp.setWorkSheetId(worksheetId);
        
        String rcvOrgId = null;
        if ("0".equals(main[0])) {// 派给员工
            tmp.setRcvOrgId("STFFID");
            tmp.setRcvOrgName("");
            tmp.setDealRequire(dealContent);
            tmp.setMonth(sheetInfo.getMonth());
            tmp.setRegionId(sheetInfo.getRegionId());
            tmp.setStationLimit(dealLimit);
            tmp.setDealLimitTime(dealLimit);
            tmp.setMainType(1);
            tmp.setServiceOrderId(sheetInfo.getServiceOrderId());
            int rcvSID = Integer.parseInt(main[1]);
            tmp.setRcvStaffId(rcvSID);
            tmp.setRcvStaffName(main[2]);
            rcvOrgId = this.pubFunc.getStaffOrgName(rcvSID);
            String rcvOName = pubFunc.getOrgName(rcvOrgId);
            orgListStr.append(mainOrg + rcvOName + "(" + main[2] + ")");
        } else {
            rcvOrgId = main[1];
            tmp.setRcvOrgId(main[1]);
            tmp.setRcvOrgName(main[2]);
            tmp.setRcvStaffName(" ");
            tmp.setDealRequire(dealContent);
            tmp.setMonth(sheetInfo.getMonth());
            tmp.setRegionId(sheetInfo.getRegionId());
            tmp.setStationLimit(dealLimit);
            tmp.setDealLimitTime(dealLimit);
            tmp.setMainType(1);
            tmp.setServiceOrderId(sheetInfo.getServiceOrderId());
            orgListStr.append(mainOrg + main[2]);
        }
        sheets[0] = tmp;

        /* 生成协办单 */
        for (int i = 0; sub.length > 0 && i < size-1; i++) {
            tmp = new SheetPubInfo();
            if ("0".equals(sub[0])) {// 派给员工
                tmp.setRcvOrgId("STFFID");
                tmp.setRcvOrgName("");
                tmp.setDealRequire(dealContent);
                tmp.setMonth(sheetInfo.getMonth());
                tmp.setRegionId(sheetInfo.getRegionId());
                tmp.setStationLimit(dealLimit);
                tmp.setDealLimitTime(dealLimit);
                tmp.setMainType(1);
                tmp.setServiceOrderId(sheetInfo.getServiceOrderId());
                int rcvSID = Integer.parseInt(subStaffIds[i]);
                tmp.setRcvStaffId(rcvSID);
                tmp.setRcvStaffName(subStaffNames[i]);
                String rcvOID = this.pubFunc.getStaffOrgName(rcvSID);
                String rcvOName = pubFunc.getOrgName(rcvOID);
                orgListStr.append(assitOrg + rcvOName + "(" + subStaffNames[i] + ")");
            } else {
                tmp.setRcvOrgId(subOrgIds[i]);
                tmp.setRcvOrgName(subOrgNames[i]);
                tmp.setRcvStaffName(" ");
                tmp.setDealRequire(dealContent);
                tmp.setMonth(sheetInfo.getMonth());
                tmp.setRegionId(sheetInfo.getRegionId());
                tmp.setStationLimit(dealLimit);
                tmp.setDealLimitTime(dealLimit);
                tmp.setMainType(0);
                tmp.setServiceOrderId(sheetInfo.getServiceOrderId());
                orgListStr.append(assitOrg + subOrgNames[i]);
            }
            sheets[i + 1] = tmp;
        }

        String flowSeq=pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(),"1",1);
        TsmStaff staff = pubFunc.getLogonStaff();
        String dealOrg = staff.getOrganizationId();
        String dealStaffId = staff.getId();

        labelManageDAO.updateDealHours(dealLimit, sheetInfo.getServiceOrderId());
        labelManageDAO.updateZdxCpDate(sheetInfo.getServiceOrderId());

        Map otherParam = new HashMap();
        otherParam.put("DEAL_REQUIRE", dealContent);
        otherParam.put("ROUTE_VALUE", StaticData.DINGXIN_TO_DEAL);
        otherParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
        otherParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
        otherParam.put("SHEETARRAY", sheets);
        otherParam.put("PRECONTRACTSIGN", String.valueOf(2));// 处理类型：2 终定性环节重新派单
        otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeq));// 流水号
        submitWorkFlow(worksheetId, sheetInfo.getRegionId(), sheetInfo.getMonth(), otherParam);

        TsSheetDealType typeBean = new TsSheetDealType();
        String guid = pubFunc.crtGuid();
        typeBean.setDealTypeId(guid);
        typeBean.setOrderId(sheetInfo.getServiceOrderId());
        typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
        typeBean.setDealType("终定性重新派发工单");
        typeBean.setDealTypeDesc("重派列表");
        typeBean.setDealId(0);
        typeBean.setDealDesc(orgListStr.toString());
        typeBean.setDealContent(dealContent);
        typeBean.setMonth(sheetInfo.getMonth());
        typeBean.setUpgradeIncline(upgradeIncline);
        tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型
        labelManageDAO.updateUpgradeIncline(sheetInfo.getServiceOrderId(), upgradeIncline);
        sheetPubInfoDao.updateSheetDealRequire(worksheetId, sheetInfo.getDealRequire(), orgListStr.toString(), "终定性重新派发工单", dealContent, 62,
                StaticData.TACHE_DEAL_NEW);
        //2020-12-15添加，为了修复，重派的终定性工单状态未完成
        String stateDesc = pubFunc.getStaticName(StaticData.WKST_FINISH_STATE_NEW);
        sheetPubInfoDao.updateSheetState(worksheetId, StaticData.WKST_FINISH_STATE_NEW, stateDesc,
        		sheetInfo.getMonth(), 2);
        return "SUCCESS";
    }
    
    public Map getLatestQualitative(String orderID, int regionId) {
        List list = sheetQualitative.getOrderQualitative(orderID);
        if (list.isEmpty()) {
            return null;
        }
        return (Map) list.get(list.size() - 1);
    }

	public int saveUnsatisfyTemplate(String reason, String template, int colOrder) {
		return unsatisfyTemplate.insertUnsatisfyTemplate(reason, template, colOrder);
	}

	public int delUnsatisfyTemplate(String unsatisfyId) {
		return unsatisfyTemplate.deleteUnsatisfyTemplate(unsatisfyId); 
	}

	public int modifyUnsatisfyTemplate(String reason, String template, int colOrder, String unsatisfyId) {
		return unsatisfyTemplate.updateUnsatisfyTemplate(reason, template, colOrder, unsatisfyId);
	}

	public List queryUnsatisfyTemplate() {
		return unsatisfyTemplate.selectUnsatisfyTemplate();
	}

    public String getDefaultMistakeOrg(String orderId) {
        SheetPubInfo latestDS = sheetPubInfoDao.getLatestSheetByType(orderId,
                StaticData.SHEET_TYPE_TS_DEAL_NEW, 1);
        if (latestDS == null) {
            return null;
        }
        return latestDS.getRcvOrgId();
    }

	public String setUnifiedContact(String orderId, String sheetId, String contactStatus, String contactType) {
		ComplaintUnifiedContact old = cmpUnifiedContactDAOImpl.queryUnifiedContactByOrderId(orderId, contactType);
		if (null == old) {
			if ("1".equals(contactStatus)) {
				ComplaintUnifiedContact cuc = new ComplaintUnifiedContact();
				cuc.setServiceOrderId(orderId);
				cuc.setWorkSheetId(sheetId);
				cuc.setOperLogon(pubFunc.getLogonStaff().getLogonName());
				cuc.setContactType(contactType);
				cmpUnifiedContactDAOImpl.saveUnifiedContact(cuc);
			}
		} else {
			if (!"1".equals(contactStatus)) {
				ComplaintUnifiedContact cuc = new ComplaintUnifiedContact();
				cuc.setServiceOrderId(orderId);
				cuc.setWorkSheetId(sheetId);
				cuc.setOperLogon(pubFunc.getLogonStaff().getLogonName());
				cuc.setContactType(contactType);
				cmpUnifiedContactDAOImpl.cancelUnifiedContactStatusByOrderId(cuc);
			}
		}
		return "";
	}

	public String setUnifiedRepeat(String orderId, String sheetId, String ucc, String repeatType) {
		int comeCategory = 707907001;
		if ("2".equals(repeatType)) {
			comeCategory = 707907002;
			if (ucc.length() > 0) {
				OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(ucc, false);
				comeCategory = orderInfo.getComeCategory();
			}
		}
		ComplaintUnifiedRepeat old = cmpUnifiedRepeatDAOImpl.queryUnifiedRepeatByCurSoi(orderId, repeatType);
		if (null == old) {
			if (ucc.length() > 0) {
				ComplaintUnifiedRepeat cur = new ComplaintUnifiedRepeat();
				cur.setCurSoi(orderId);
				cur.setCurWhi(sheetId);
				cur.setNewUcc(ucc);
				cur.setOperLogon(pubFunc.getLogonStaff().getLogonName());
				cur.setRepeatType(repeatType);
				cur.setComeCategory(comeCategory);
				cmpUnifiedRepeatDAOImpl.saveUnifiedRepeat(cur);
			}
		} else {
			if (ucc.length() > 0) {
				if (!ucc.equals(old.getNewUcc())) {
					ComplaintUnifiedRepeat cur = new ComplaintUnifiedRepeat();
					cur.setCurSoi(orderId);
					cur.setCurWhi(sheetId);
					cur.setNewUcc(ucc);
					cur.setOperLogon(pubFunc.getLogonStaff().getLogonName());
					cur.setRepeatType(repeatType);
					cur.setComeCategory(comeCategory);
					cmpUnifiedRepeatDAOImpl.cancelUnifiedRepeatStatusByCurSoi(cur);
					cmpUnifiedRepeatDAOImpl.saveUnifiedRepeat(cur);
				}
			} else {
				ComplaintUnifiedRepeat cur = new ComplaintUnifiedRepeat();
				cur.setCurSoi(orderId);
				cur.setCurWhi(sheetId);
				cur.setOperLogon(pubFunc.getLogonStaff().getLogonName());
				cur.setRepeatType(repeatType);
				cur.setComeCategory(comeCategory);
				cmpUnifiedRepeatDAOImpl.cancelUnifiedRepeatStatusByCurSoi(cur);
			}
		}
		return "";
	}

	public String getJTSSCode(String orderId) {
		OrderAskInfo orderNew = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if (null != orderNew) {
			StringBuilder condition = new StringBuilder();
			condition.append("AND SERVICE_ORDER_ID!='");
			condition.append(orderNew.getServOrderId());
			condition.append("' AND REGION_ID=");
			condition.append(orderNew.getRegionId());
			condition.append(" AND PROD_NUM='");
			condition.append(orderNew.getProdNum());
			condition.append("' AND COME_CATEGORY IN(707907002,707907003) ORDER BY ACCEPT_DATE DESC");
			OrderAskInfo[] orderAskInfos = orderAskInfoDao.getOrderAskInfoByCondition(condition.toString(), false);
			if (orderAskInfos.length > 0) {
				return orderAskInfos[0].getServOrderId();
			}
		}
		return "";
	}

	public boolean checkJTSSCode(String uccJTSS) {
		OrderAskInfo orderNew = orderAskInfoDao.getOrderAskInfo(uccJTSS, false);
		if (null != orderNew) {
			int comeCategory = orderNew.getComeCategory();
			if (707907002 == comeCategory || 707907003 == comeCategory) {
				return true;
			}
		}
		return false;
	}

    public boolean checkUnifiedComplaintCode(String ucc) {
		List list = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByWhere(" AND unified_complaint_code = '" + ucc + "'");
		return !list.isEmpty();
	}

	public List<ComplaintUnifiedReturn> queryUnifiedReturnByWhere(String where) {
		return cmpUnifiedReturnDAOImpl.queryUnifiedReturnByWhere(where);
	}

	@Override
	public List complaintCodeAndLogonName(String orderId) {
		return sheetPubInfoDao.complaintCodeAndLogonName(orderId);
	}

	@Override
	public GridDataInfo getHiddenSheetList(String where) {
		JSONObject obj = JSONObject.fromObject(where);
		String begion = obj.optString("begion");
		String opraStaff = obj.optString("opra_staff");
		String serviceOrderId = obj.optString("service_order_id");
		String prodNum = obj.optString("prod_num");
		String custServGrade = obj.optString("cust_serv_grade");
		String relaPhone = obj.optString("relaPhone");
		String startTime = obj.optString("startTime");
		String endTime = obj.optString("endTime");
		
		String result = "";
		if(!"".equals(opraStaff)) {
			result += " AND opra_staff = "+Integer.valueOf(opraStaff);
		}
		if(!"".equals(serviceOrderId)) {
			result += " AND A.service_order_id = '"+serviceOrderId +"'";
		}
		if(!"".equals(prodNum)) {
			result += " AND A.prod_num = '"+prodNum+"'";
		}
		if(!"".equals(custServGrade)) {
			result += " AND C.cust_serv_grade = "+Integer.valueOf(custServGrade);
		}
		if(!"".equals(relaPhone)) {
			result += " AND rela_info = '"+relaPhone+"'";
		}
		if(!"".equals(startTime)) {
			result += " AND accept_date >= str_to_date('" + startTime + "','%Y-%m-%d %H:%i:%s')";
			result += " AND accept_date < str_to_date('" + endTime + "','%Y-%m-%d %H:%i:%s')";
		}
		
		String sql = "SELECT A.SERVICE_ORDER_ID,\n" +
				"D.WORK_SHEET_ID,\n" +
				"A.PROD_NUM,\n" +
				"DATE_FORMAT(ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\n" +
				"DATE_FORMAT(LOCK_DATE, '%Y-%m-%d %H:%i:%s') LOCK_DATE,\n" +
				"ACCEPT_COME_FROM_DESC,\n" +
				"DATE_FORMAT(OPRA_DATE, '%Y-%m-%d %H:%i:%s') OPRA_DATE,\n" +
				"DATE_FORMAT(END_DATE, '%Y-%m-%d') END_DATE,\n" +
				"SHEET_STATU_DESC,\n" +
				"(SELECT COUNT(1) FROM CC_HASTEN_SHEET F WHERE F.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID) HASTENT_NUM,\n" +
				"ACCEPT_COUNT,\n" +
				"SHEET_TYPE_DESC,\n" +
				"URGENCY_GRADE_DESC,\n" +
				"A.SERVICE_TYPE_DESC,\n" +
				"APPEAL_PROD_NAME,\n" +
				"CUST_SERV_GRADE_NAME,\n" +
				"A.REGION_NAME,\n" +
				"OPRA_STAFF\n" +
				"FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B, CC_ORDER_CUST_INFO C, CC_WORK_SHEET D, CC_SHEET_HIDDEN_ACTION E\n" +
				"WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
				"AND A.CUST_GUID = C.CUST_GUID\n" +
				"AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID\n" +
				"AND D.WORK_SHEET_ID = E.WORK_SHEET_ID\n" +
				"AND HIDDEN_STATE = 0";
		sql += result;
		return dbgridDataPub.getResult(sql, Integer.parseInt(begion), " order by end_date", "getHiddenListByOpraStaff");
	}

	@Override
	public int saveReceiptEval(JSONObject ins) {
		return tsDealQualitativeDaoImpl.saveReceiptEval(ins);
	}
}