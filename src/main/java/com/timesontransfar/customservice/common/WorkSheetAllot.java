/*
 * 说明：提供系统自动派发各类工单至个人时使用的公共方法
 * 时间： 2011-10-19
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.customservice.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.dbgridData.ConfigurationDataTs;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.IForceDistillDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.flowskillrela.dao.IFlowSkillRelaDao;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.service.IStaffWorkloadService;

import java.util.Map;

/**
 * 提供系统自动派发各类工单至个人时使用的公共方法
 * 
 * @author LiJiahui
 */
@Component("workSheetAllot")
public class WorkSheetAllot {

    /**
     * 操作结果：派至个人成功
     */
    public static final String RST_SUCCESS = "SUCCESS";

    /**
     * 操作结果：没有找到合适员工
     */
    public static final String RST_NONE = "NONE";

    /**
     * 受理单表操作类实例
     */
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;

    /**
     * 操作 CCS.CC_STAFF_WORKLOAD 表的服务类
     */
    @Autowired
    private IStaffWorkloadService staffWorkloadService;

    /**
     * 操作工单动作表
     */
    @Autowired
    private ISheetActionInfoDao sheetActionInfoDao;

    /**
     * 公共方法提供类
     */
    @Autowired
    private PubFunc pubFunc;
    
    /**
     * 操作工单表
     */
    @Autowired
    private ISheetPubInfoDao  sheetPubInfoDao;
    
    /**
     * 流向部门与技能类型关系配置
     */
    @Autowired
    private IFlowSkillRelaDao flowSkillRelaDao;
	@Autowired
	private IForceDistillDao forceDistillDao;
	@Autowired
	private ConfigurationDataTs configurationDataTs;

    // 业务工单监控箱自动分派
    public String allotForceDistill(SheetPubInfo sheetPubInfo, int forceStaff) {
		changeSheetPubInfo(forceStaff, sheetPubInfo);
		int sheetStatu = pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
		sheetPubInfo.setSheetStatu(sheetStatu);
		sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
		String comment = "强制提取工单,员工" + sheetPubInfo.getRcvStaffName() + ",工单号为" + sheetPubInfo.getWorkSheetId();
		saveSheetDealAction(sheetPubInfo, StaticData.WKST_ACTION_FORCEDISTILL, 1, comment);
		return RST_SUCCESS;
    }

	public String allotSheet(SheetPubInfo sheetPubInfo) {
        switch (sheetPubInfo.getTacheId()) {
            case StaticData.TACHE_ASSIGN:
            case StaticData.TACHE_ASSIGN_NEW:
            case StaticData.TACHE_RGHF:
                // 后台派单环节工单的自动派发
                return allotToAllot(sheetPubInfo);

            case StaticData.TACHE_DEAL:
            case StaticData.TACHE_DEAL_NEW:
                if (sheetPubInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL
                        || sheetPubInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW) {
                    // 部门处理环节工单的自动派发--审批单
                    return allotToApprove(sheetPubInfo);
                } else {
                    // 部门处理环节工单的自动派发--处理单
                    return allotToDeal(sheetPubInfo);
                }

            case StaticData.TACHE_AUIT:
                // 后台审核环节工单的自动派发
                return allotToVerify(sheetPubInfo);

            case StaticData.TACHE_ZHONG_DINGXING_NEW:
                // 终定性环节工单的自动派发，如果是预定性生成的终定性单，不在生成工单的时候派发
        		SheetPubInfo sourceSheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetPubInfo.getSourceSheetId(), false);
        		if (StaticData.TACHE_DINGXING_NEW == sourceSheetInfo.getTacheId()) {
        			return RST_NONE;
        		} else {
        			return allotToFinAssessAddForce(sheetPubInfo);
        		}
            default:
                break;
        }
        return RST_NONE;
    }

	@SuppressWarnings({ "rawtypes" })
    private String localAllot(SheetPubInfo sheetPubInfo, int newSheetStatu){
        OrderAskInfo orderAskInfo = this.orderAskInfoDao.getOrderAskInfo(
                sheetPubInfo.getServiceOrderId(), false);
    	int skillId = 0;
    	if(orderAskInfo.getServiceDate() == 1) {
    		skillId = flowSkillRelaDao.getSkillIdWithDate1(sheetPubInfo.getRcvOrgId(), sheetPubInfo.getServiceOrderId());
    	}else if(orderAskInfo.getServiceDate() == 3) {
    		skillId = flowSkillRelaDao.getSkillIdWithDate3(sheetPubInfo.getRcvOrgId(), sheetPubInfo.getServiceOrderId());
    	}
    	if(skillId == 0) {
    		return RST_NONE;
		}
		Map map = staffWorkloadService.queryStaffWorkloadRepeat(skillId, orderAskInfo, sheetPubInfo);
		if (map.isEmpty()) {
			map = staffWorkloadService.getStaffWorkload(sheetPubInfo.getRcvOrgId(), skillId, sheetPubInfo.getTacheId(),
					String.valueOf(orderAskInfo.getServiceDate()), orderAskInfo.getServType());
		}
		if(map.isEmpty()) {
			return RST_NONE;
		}
		StaffWorkloadInfo info = (StaffWorkloadInfo) map.get("info");
		String apportionFlag = map.get("apportion").toString();

    	// 收单员工ID
    	int revStaffId = 0;
    	if (null != info) {
    		revStaffId = info.getStaffId();
    		changeSheetPubInfo(revStaffId, sheetPubInfo);
    		sheetPubInfo.setSheetStatu(newSheetStatu);
    		sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(newSheetStatu));
			String comment = "";
			if ("1".equals(apportionFlag)){
				staffWorkloadService.updateApportion(String.valueOf(revStaffId));
				comment = "申请分派成功,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
						+ sheetPubInfo.getRcvStaffName() + sheetPubInfo.getRcvStaffId();
			}else if("2".equals(apportionFlag)){
				int num = staffWorkloadService.allotWork(info, true); // 算工作量
				comment = "一单到底分派成功,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
						+ sheetPubInfo.getRcvStaffName() + sheetPubInfo.getRcvStaffId() +",工作量为"+num;
			}else {
				int num = staffWorkloadService.allotWork(info, true); // 算工作量
				comment = "系统自动分派工单,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
						+ sheetPubInfo.getRcvStaffName() + sheetPubInfo.getRcvStaffId() +",工作量为"+num;
			}
    		saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment);
    		return RST_SUCCESS;
    	}
    	return RST_NONE;
    }

    //派发处于“终定性”环节的集团渠道工单
    private String localJTAllot(SheetPubInfo sheetPubInfo, int newSheetStatu){
    	SheetPubInfo sheetPubInfoBMCL = sheetPubInfoDao.getLatestSheetByType(sheetPubInfo.getServiceOrderId(),StaticData.SHEET_TYPE_TS_DEAL_NEW,1);
    	SheetPubInfo sheetPubInfoHTPD = sheetPubInfoDao.getLatestSheetByType(sheetPubInfo.getServiceOrderId(),StaticData.SHEET_TYPE_TS_ASSING_NEW,0);
    	try {
			if(null == sheetPubInfoHTPD){
				if(null != sheetPubInfoBMCL){
					int retStaffId = sheetPubInfoBMCL.getRetStaffId();
					if (0 != retStaffId) {
						changeSheetPubInfo(retStaffId, sheetPubInfo);
						sheetPubInfo.setSheetStatu(newSheetStatu);
						sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(newSheetStatu));
						String comment = "系统自动分派工单,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
								+ sheetPubInfo.getDealStaffName() + sheetPubInfo.getDealStaffId();
						saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment);
						return RST_SUCCESS;
					}
				}
			}else{
				int dealStaffId = sheetPubInfoHTPD.getDealStaffId();
				if(20001797 == dealStaffId) {//省投派单岗
					dealStaffId = sheetPubInfoBMCL.getDealStaffId();
				}
				if (0 != dealStaffId) {
					changeSheetPubInfo(dealStaffId, sheetPubInfo);
					sheetPubInfo.setSheetStatu(newSheetStatu);
					sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(newSheetStatu));
					String comment = "系统自动分派工单,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
							+ sheetPubInfo.getDealStaffName() + sheetPubInfo.getDealStaffId();
					saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment);
					return RST_SUCCESS;
				}
			}
    	} catch (Exception e) {
    		//  Auto-generated catch block
    		e.printStackTrace();
    	}
    	return RST_NONE;
    }
    /**
     * 派发处于“后台派单”环节的工单
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
    public String allotToAllot(SheetPubInfo sheetPubInfo) {
    	//全省后台派单都走本地网流程
    	return allotToAllotLocal(sheetPubInfo);    
    }

    /**
     * 派发“后台派单”环节的工单，本地网流程<br>
     * 查询到员工信息后，更新工单对象的收单信息和状态，不操作数据库
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
    private String allotToAllotLocal(SheetPubInfo sheetPubInfo) {
        int statu = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_DEALING_STATE_NEW
                : StaticData.WKST_DEALING_STATE;
        return localAllot(sheetPubInfo, statu);
    }

    /**
     * 派发处于“部门处理”环节的处理工单
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
    public String allotToDeal(SheetPubInfo sheetPubInfo) {
    	//全省部门处理都走本地网流程
    	return allotToDealLocal(sheetPubInfo);
    }

    /**
     * 派发处于“部门处理”环节的处理工单，本地网流程<br>
     * 查询到员工信息后，更新工单对象的收单信息和状态，不操作数据库
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
    private String allotToDealLocal(SheetPubInfo sheetPubInfo) {
        int statu = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_DEALING_STATE_NEW
                : StaticData.WKST_ORGDEALING_STATE;
        return localAllot(sheetPubInfo, statu);
    }

    /**
     * 派发处于“部门处理”环节的审批工单
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
	public String allotToApprove(SheetPubInfo sheetPubInfo) {
		String result = RST_NONE;
		int forceStaff = forceDistillDao.selectForceStaffByOrderId(sheetPubInfo.getServiceOrderId());
		if (forceStaff > 0) {// 参与业务工单监控箱自动分派
			result = allotForceDistill(sheetPubInfo, forceStaff);
		}
		if (RST_NONE.equals(result)) {
			int servType = sheetPubInfo.getServType();
			int retStaffId = sheetPubInfo.getRetStaffId();
			if (isReturnSP(sheetPubInfo)) {
				int statu = servType == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_DEALING_STATE_NEW : StaticData.WKST_ORGAUDING_STATE;
				changeSheetPubInfo(retStaffId, sheetPubInfo);
				sheetPubInfo.setSheetStatu(statu);
				sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(statu));
				StringBuilder comment = new StringBuilder("系统自动回流工单,工单号为");
				comment.append(sheetPubInfo.getWorkSheetId());
				comment.append("的工单回流给员工");
				comment.append(sheetPubInfo.getRcvStaffName());
				comment.append(sheetPubInfo.getRcvStaffId());
				saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment.toString());
				return RST_SUCCESS;
			}
		}
		if (RST_NONE.equals(result) && pubFunc.isAffiliated(sheetPubInfo.getRetOrgId(), StaticData.ORG_NJ_CUSTOM_SERVICE_CENTRAL)) {
			result = allotToApproveLocal(sheetPubInfo);
		}
		return result;
	}

	// 判断审批单是否回流
	// 时荣菊:2024.12.18 10:15:54 请问奕姐， 投诉、咨询、跟踪工单的审批单回流到派单人岗， 是南京分公司下所有部门的审批单都要这么回流还是只是下派到区县回来的审批单要回流呢
	// 刘奕 :2024.12.18 10:23:54 所有的
	private boolean isReturnSP(SheetPubInfo sheetPubInfo) {
		if (isLargeCategoryTS(sheetPubInfo.getServType())) {// 投诉、查询、跟踪
			String retOrgId = sheetPubInfo.getRetOrgId();
			if (pubFunc.checkSpecialOrg(pubFunc.getAreaOrgId(retOrgId), 3)) {// 南京分公司
				int retStaffId = sheetPubInfo.getRetStaffId();
				int count = configurationDataTs.isAllotConfigStaffByZpStaffId(retStaffId);
				if (0 == count) {// 回流员工不是转派虚拟岗
					OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(sheetPubInfo.getServiceOrderId(), false);
					if (StaticData.COME_FROM_SN == orderAskInfo.getAskSource()) {// 只有省内才回流
						StaffWorkloadInfo info = staffWorkloadService.queryInWork(retStaffId);
						if (null != info) {// 查看员工retStaffId是否在工作状态
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	// 投诉、查询、跟踪
	private boolean isLargeCategoryTS(int servType) {
		return StaticData.SERV_TYPE_NEWTS == servType || StaticData.SERV_TYPE_CX == servType || StaticData.SERV_TYPE_GZ == servType;
	}

    /**
     * 派发处于“部门处理”环节的审批工单，本地网流程<br>
     * 查询到员工信息后，更新工单对象的收单信息和状态，不操作数据库
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
    private String allotToApproveLocal(SheetPubInfo sheetPubInfo) {
        int staffId = sheetPubInfo.getRetStaffId();
        // 查看员工staffId是否在工作状态
        StaffWorkloadInfo info = staffWorkloadService.queryInWork(staffId);
        int statu = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_DEALING_STATE_NEW
                : StaticData.WKST_ORGAUDING_STATE;
        
        if (null != info) {
            changeSheetPubInfo(staffId, sheetPubInfo);
            sheetPubInfo.setSheetStatu(statu);
            sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(statu));
            String comment = "系统自动分派工单,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
                    + sheetPubInfo.getRcvStaffName() + sheetPubInfo.getRcvStaffId();
            saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment);
            return RST_SUCCESS;
        }
        return RST_NONE;
    }

    /**
     * 派发处于“后台审核”环节的审核工单
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
	public String allotToVerify(SheetPubInfo sheetPubInfo) {
		String result = RST_NONE;
		int forceStaff = forceDistillDao.selectForceStaffByOrderId(sheetPubInfo.getServiceOrderId());
		if (forceStaff > 0) {
			// 参与业务工单监控箱自动分派
			result = allotForceDistill(sheetPubInfo, forceStaff);
		}
		if (RST_NONE.equals(result)) {
			// 属于南京客服中心的回单
			if (pubFunc.isAffiliated(sheetPubInfo.getRetOrgId(), StaticData.ORG_NJ_CUSTOM_SERVICE_CENTRAL)) {
				result = allotToVerifyLocal(sheetPubInfo);
			} else {
				result = localAllot(sheetPubInfo, StaticData.WKST_AUDING_STATE);
			}
		}
		return result;
	}

    /**
     * 派发处于“后台审核”环节的审核工单，本地网流程<br>
     * 查询到员工信息后，更新工单对象的收单信息和状态，不操作数据库
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            待派发的工单对象
     * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
     */
    private String allotToVerifyLocal(SheetPubInfo sheetPubInfo) {
        int staffId = sheetPubInfo.getRetStaffId();

        // 查看员工staffId是否在工作状态
        StaffWorkloadInfo info = staffWorkloadService.queryInWork(staffId);
        if (null == info) {
            return localAllot(sheetPubInfo, StaticData.WKST_AUDING_STATE);
        } else {
            changeSheetPubInfo(staffId, sheetPubInfo);
            sheetPubInfo.setSheetStatu(StaticData.WKST_AUDING_STATE);
            sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_AUDING_STATE));
            String comment = "系统自动分派工单,工单号为" + sheetPubInfo.getWorkSheetId() + "的工单分派给员工"
                    + sheetPubInfo.getRcvStaffName() + sheetPubInfo.getRcvStaffId();
            saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment);
            return RST_SUCCESS;
        }
    }

	public String allotToFinAssessAddForce(SheetPubInfo sheetPubInfo) {
		String result = RST_NONE;
		int forceStaff = forceDistillDao.selectForceStaffByOrderId(sheetPubInfo.getServiceOrderId());
		if (forceStaff > 0) {
			// 参与业务工单监控箱自动分派
			result = allotForceDistill(sheetPubInfo, forceStaff);
		}
		if (RST_NONE.equals(result)) {
			result = allotToFinAssess(sheetPubInfo);
		}
		return result;
	}

	/**
	 * 派发处于“终定性”环节的工单
	 * 
	 * @author LiJiahui
	 * @date 2013-06-14
	 * @param sheetPubInfo
	 *            待派发的工单对象
	 * @return 操作结果的描述。SUCCESS表示派到个人成功；NONE表示派至个人失败，没有找到合适员工
	 */
	private String allotToFinAssess(SheetPubInfo sheetPubInfo) {
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(sheetPubInfo.getServiceOrderId(), false);
		/**
		 * 0、针对流向“省投诉-投诉处理班组”的工单，优化需求如下：
		 * 1、省内同现行流程：在“后台派单”环节设置自动分派，在“终定性”环节回“后台派单”环节处理人员的库里。
		 * 2、集团渠道工单在“后台派单”和“终定性”环节均在大库时执行自动分派。
		 * 3、申诉工单在“后台派单”不设置自动分派，在大库由班长手工分派；在“终定性”环节回“后台派单”环节处理人员的库里。
		 * 2022-11，经朱圆圆、金晶确认，所有省投后台派单的单子直接回到本人
		 */
		if (pubFunc.isAffiliated(sheetPubInfo.getRcvOrgId(), StaticData.ORG_STSGDYZ_CENTER)) {// 收单部门为省投诉_投诉处理班
			if (orderInfo.getAskSource() == StaticData.COME_FROM_SN) {
				return localJTAllot(sheetPubInfo, StaticData.WKST_DEALING_STATE_NEW);
			} else if (orderInfo.getAskSource() == StaticData.ACCEPT_COME_FROM_JT) {
				SheetPubInfo sourceSheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetPubInfo.getSourceSheetId(), false);
				// 省投诉中心集团单后派单直接办结回后台派单人库，转派的工单终定性回在班人库
				if (StaticData.TACHE_ASSIGN_NEW == sourceSheetInfo.getTacheId()) {
					return localJTAllot(sheetPubInfo, StaticData.WKST_DEALING_STATE_NEW);
				} else {
					return localAllot(sheetPubInfo, StaticData.WKST_DEALING_STATE_NEW);
				}
			} else {
				return localJTAllot(sheetPubInfo, StaticData.WKST_DEALING_STATE_NEW);
			}
		} else {
			if (orderInfo.getAskSource() == StaticData.ACCEPT_COME_FROM_JT) {// 20201127修改，受理来源为集团渠道，谁派单，则自动回到谁的岗进行定性处理
				return localJTAllot(sheetPubInfo, StaticData.WKST_DEALING_STATE_NEW);
			} else {
				// 由属于南京客服中心的回单改为全省回单
				return localAllot(sheetPubInfo, StaticData.WKST_DEALING_STATE_NEW);
			}
		}
	}

    /**
     * 更新工单的收单人、处理人信息等相关信息<br>
     * 仅更新工单对象，不操作数据库
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param rcvStaffId
     *            收单员工ID
     * @param sheetPubInfo
     *            工单对象
     */
    private void changeSheetPubInfo(int rcvStaffId, SheetPubInfo sheetPubInfo) {
    	TsmStaff rcvStaff = this.pubFunc.getStaff(rcvStaffId);
        sheetPubInfo.setRcvStaffId(rcvStaffId); // 收单员工ID
        sheetPubInfo.setRcvStaffName(rcvStaff.getName()); // 收单员工姓名
        sheetPubInfo.setDealStaffId(rcvStaffId); // 处理员工ID，即收单员工ID
        sheetPubInfo.setDealStaffName(rcvStaff.getName()); // 处理员工名，即收单员工名
        sheetPubInfo.setDealOrgId(rcvStaff.getOrganizationId()); // 处理单位ID，即收单员工所在部门ID
        sheetPubInfo.setDealOrgName(rcvStaff.getOrgName()); // 处理单位名称，即收单员工所在部门名称
        sheetPubInfo.setLockFlag(1); // Lock置为1
    }

    /**
     * 操作数据库，保存工单派发动作
     * 
     * @author LiJiahui
     * @date 2011-10-19
     * @param sheetPubInfo
     *            工单对象
     * @param actionType
     *            动作类型
     * @param type
     *            动作 1为自动派发 ，2为联络部成功， 3为提取工单，4为释放工单，5为挂起工单，6为解挂工单，7为追回工单
     * @param comment
     *            描述语句
     * @return 是否成功
     */
    public boolean saveSheetDealAction(SheetPubInfo sheetPubInfo, int actionType, int type,
            String comment) {
        SheetActionInfo sheetActionInfo = new SheetActionInfo();
        String guid = pubFunc.crtGuid();
        sheetActionInfo.setActionGuid(guid);
        sheetActionInfo.setWorkSheetId(sheetPubInfo.getWorkSheetId());
        sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
        sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
        sheetActionInfo.setComments(comment);
        sheetActionInfo.setMonth(sheetPubInfo.getMonth());
        sheetActionInfo.setTacheId(sheetPubInfo.getTacheId());
        sheetActionInfo.setTacheName(pubFunc.getStaticName(sheetPubInfo.getTacheId()));
        sheetActionInfo.setActionCode(actionType);
        sheetActionInfo.setActionName(pubFunc.getStaticName(actionType));

        try{
            // 取当前登录员工信息
            TsmStaff staff = pubFunc.getLogonStaff();
            int staffId = Integer.parseInt(staff.getId());
            String staffName = staff.getName();
            String orgId = staff.getOrganizationId();
            String orgName = staff.getOrgName();
            sheetActionInfo.setOpraOrgId(orgId);
            sheetActionInfo.setOpraOrgName(orgName);
            sheetActionInfo.setOpraStaffId(staffId);
            sheetActionInfo.setOpraStaffName(staffName);
        }catch(Exception e){
            sheetActionInfo.setOpraOrgId(sheetPubInfo.getRcvOrgId());
            sheetActionInfo.setOpraOrgName(sheetPubInfo.getRcvOrgName());
        	e.printStackTrace();
        }
        sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
        return true;
    }

    /**
     * 南京客服中心的员工手动提取、班长派单时的工作量计算
     * 
     * @author LiJiahui
     * @date 2011-10-20
     * 
     * @param sheetType
     *            工单类型
     * @param sheetStatu
     *            工单状态
     * @param retStaffId
     *            派单员工ID
     * @param rcvStaffId
     *            收单员工ID
     * @param rcvOrgId
     *            收单部门ID
     * @param type
     *            操作类型。1 手动提取；2 班长派单
     */
    public int countWorkload(int sheetType, int sheetStatu, int retStaffId, int rcvStaffId,
            String rcvOrgId, int type) {
    	StaffWorkloadInfo info = staffWorkloadService.queryInWork(rcvStaffId);
    	if (sheetType != StaticData.SHEET_TYPE_PREASSESS) {
    		if (this.pubFunc.isAffiliated(rcvOrgId, StaticData.ORG_NJ_CUSTOM_SERVICE_CENTRAL)) {           
                // 审批审核单
                if (sheetStatu == StaticData.WKST_ORGAUDING_STATE
                        || sheetStatu == StaticData.WKST_AUDING_STATE
                        || (sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW && sheetStatu == StaticData.WKST_DEALING_STATE_NEW)) {
                    // 该单不是该员工的单子，计入该员工工作量
                    if (retStaffId != rcvStaffId) {
                        return staffWorkloadService.allotWork(info, true);
                    }
                } else {
                	return staffWorkloadService.allotWork(info, true);
                }
            } else {
            	return staffWorkloadService.allotWork(info, true);
            }
    	}
    	return 0;
    }

}
