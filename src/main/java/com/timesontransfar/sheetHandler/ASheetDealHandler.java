/*
 * 文 件 名：ASheetDealHandler.java
 * 版    权：
 * 描    述：抽象类，提供工单处理的帮助方法
 * 修 改 人：LiJiahui
 * 修改时间：2013-2-16
 * 修改内容：新增
 */
package com.timesontransfar.sheetHandler;

import org.springframework.beans.factory.annotation.Autowired;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

/**
 * 抽象类，提供工单处理的帮助方法
 * 
 * @author LiJiahui
 * @version
 * @since 2013-02-16
 */
public abstract class ASheetDealHandler {
	@Autowired
    private ISheetActionInfoDao sheetActionInfoDao;
	@Autowired
    private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
    private IorderAskInfoDao orderAskInfoDao;
	@Autowired
    private PubFunc pubFunc;

    /**
     * 执行工单挂起的操作，包括工单状态的修改，工单、订单挂起时间、时长的修改等
     * 
     * @param sheetId
     *            工单的ID
     * @param comment
     *            处理内容描述
     * 
     * @return HANGING挂起失败，因为工单已经挂起；SUCCESS挂起成功
     */
    public abstract String hangup(String sheetId, String comment);

    /**
     * 挂起工单
     * 
     * @param sheetId
     *            工单号
     * @param comment
     *            工单挂起操作的备注
     * @param flag
     *            1 强制挂起，不需审批；0 需要审批才可挂起，会校验lock_flag
     * @param holdStateId
     *            工单挂起状态的ID，服务类型不同，挂起状态的ID不同，所以需要传入
     * @param newLockFlag
     *            新的LockFlag值
     * @return
     */
    public int hangup(String sheetId, String comment, int flag, int holdStateId, int newLockFlag) {
        SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId, false);
        int state = sheetPubInfo.getLockFlag();
        // 挂起前必须做审批,审批为3
        if (flag == 0 && state != 3) {// 为可以直接挂起
        	return 0;
        }

        // 更新工单状态为挂起
        String stateDesc = pubFunc.getStaticName(holdStateId);
        if (null == stateDesc || stateDesc.length() == 0) {
            return 0;
        }
        this.sheetPubInfoDao.updateSheetState(sheetId, holdStateId, stateDesc,
                sheetPubInfo.getMonth(), newLockFlag);

        // 更受理单下挂起工单的数量和时间等信息
        String orderId = sheetPubInfo.getServiceOrderId();
        OrderAskInfo orderAskInfo = this.orderAskInfoDao.getOrderAskInfo(orderId, false);

        int subSheetHoldNum = orderAskInfo.getSubSheetCount();// 挂单数量
        String firstSheetHoldTime = orderAskInfo.getHangStartTime();// 首张单挂起始时间
        int totalHoldTime = orderAskInfo.getHangTimeSum();// 挂单总时间

        // 为同一张定单下第一张挂起的工单
        if (subSheetHoldNum == 0) {
            this.orderAskInfoDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum + 1,
                    pubFunc.getSysDate(), totalHoldTime);
        } else {
            this.orderAskInfoDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum + 1,
                    firstSheetHoldTime, totalHoldTime);
        }
        // 更新工单的挂起时间
        int sheetTotalHoldTime = sheetPubInfo.getHangupTimeSum();
        String angupStrTime = pubFunc.getSysDate();
        this.sheetPubInfoDao.updateTotalHold(angupStrTime, sheetTotalHoldTime,
                sheetPubInfo.getRegionId(), sheetId);
        int tachId = StaticData.WKST_HOLD_ACTION;
        if (9 == flag) {
        	tachId = StaticData.WKST_TZ_HOLD_ACTION;
        }
        saveSheetDealAction(sheetPubInfo.getServiceOrderId(), sheetId, sheetPubInfo.getRegionId(),
                comment, sheetPubInfo.getMonth(), sheetPubInfo.getTacheId(),
                tachId);
        return 1;
    }

    public String unHangup(SheetPubInfo sheetPubInfo) {
        int sheetStatu = pubFunc.getSheetStatu(sheetPubInfo.getTacheId(),
                sheetPubInfo.getLockFlag(), sheetPubInfo.getSheetType());
        String stateDesc = pubFunc.getStaticName(sheetStatu);
        sheetPubInfoDao.updateSheetState(sheetPubInfo.getWorkSheetId(), sheetStatu, stateDesc,
                sheetPubInfo.getMonth(), sheetPubInfo.getLockFlag());

        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(
                sheetPubInfo.getServiceOrderId(), false);
        int subSheetHoldNum = orderAskInfo.getSubSheetCount();// 挂单数量
        String firstSheetHoldTime = orderAskInfo.getHangStartTime();// 首张单挂起始时间
        firstSheetHoldTime = PubFunc.dbDateToStr(firstSheetHoldTime);
        int totalHoldTime = orderAskInfo.getHangTimeSum();// 挂单总时间 (分)
        String sysdate = pubFunc.getSysDate();
        // 最后一张挂起的工单解挂时
        if (subSheetHoldNum - 1 == 0) {
            // 时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
            int timeBetweenFlag = 2;
            int holdTime = (int) PubFunc.getTimeBetween(firstSheetHoldTime, sysdate,
                    timeBetweenFlag);
            orderAskInfoDao.updateSubSheetHoldInfo(sheetPubInfo.getServiceOrderId(),
                    subSheetHoldNum - 1, "", totalHoldTime + holdTime);
        } else {
            orderAskInfoDao.updateSubSheetHoldInfo(sheetPubInfo.getServiceOrderId(),
                    subSheetHoldNum - 1, firstSheetHoldTime, totalHoldTime);
        }
        String startTime = sheetPubInfo.getHangupStrTime();
        
        // 时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
        int timeFlag = 2;
        int sheetHoldTime = 0;
        if (startTime != null && startTime.length() != 0) {
            sheetHoldTime = (int) PubFunc.getTimeBetween(startTime, sysdate, timeFlag);
        }

        int sumTotalTime = sheetPubInfo.getHangupTimeSum() + sheetHoldTime;
        sheetPubInfoDao.updateTotalHoldNew(sumTotalTime, sheetPubInfo.getRegionId(),
                sheetPubInfo.getWorkSheetId());
        return "SUCCESS";
    }

	public String unHangupAndRelease(SheetPubInfo sheetPubInfo) {
		int tacheId = sheetPubInfo.getTacheId();
		int sheetType = sheetPubInfo.getSheetType();
		String sheetId = sheetPubInfo.getWorkSheetId();
		String orderId = sheetPubInfo.getServiceOrderId();
		Integer month = sheetPubInfo.getMonth();
		int regionId = sheetPubInfo.getRegionId();
		int sheetStatu = pubFunc.getSheetStatu(tacheId, sheetPubInfo.getLockFlag(), sheetType);
		String stateDesc = pubFunc.getStaticName(sheetStatu);
		sheetPubInfoDao.updateSheetState(sheetId, sheetStatu, stateDesc, month, sheetPubInfo.getLockFlag());
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		boolean release = isAutoZDXChannel(orderAskInfo.getAskChannelId());// 配置的渠道
		int subSheetHoldNum = orderAskInfo.getSubSheetCount();// 挂单数量
		String firstSheetHoldTime = orderAskInfo.getHangStartTime();// 首张单挂起始时间
		firstSheetHoldTime = PubFunc.dbDateToStr(firstSheetHoldTime);
		int totalHoldTime = orderAskInfo.getHangTimeSum();// 挂单总时间 (分)
		String sysdate = pubFunc.getSysDate();
		// 最后一张挂起的工单解挂时
		if (subSheetHoldNum - 1 == 0) {
			// 时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
			int timeBetweenFlag = 2;
			int holdTime = (int) PubFunc.getTimeBetween(firstSheetHoldTime, sysdate, timeBetweenFlag);
			orderAskInfoDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum - 1, "", totalHoldTime + holdTime);
		} else {
			orderAskInfoDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum - 1, firstSheetHoldTime, totalHoldTime);
		}
		String startTime = sheetPubInfo.getHangupStrTime();
		// 时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
		int timeFlag = 2;
		int sheetHoldTime = 0;
		if (startTime != null && startTime.length() != 0) {
			sheetHoldTime = (int) PubFunc.getTimeBetween(startTime, sysdate, timeFlag);
		}
		int sumTotalTime = sheetPubInfo.getHangupTimeSum() + sheetHoldTime;
		sheetPubInfoDao.updateTotalHoldNew(sumTotalTime, regionId, sheetId);
		if (release) {
			SheetPubInfo cur = sheetPubInfoDao.getSheetObj(sheetId, regionId, month, true);
			int lf = cur.getLockFlag();
			if (1 == lf) {// 我的任务
				String doi = cur.getDealOrgId();
				if (!"400082".equals(doi)) {// 非集团工单处理组
					// 更新提单员工信息及工单状态
					sheetPubInfoDao.updateFetchSheetStaff(sheetId, 0, "", "", "");
					int spiSheetStatu = pubFunc.getSheetStatu(tacheId, 0, sheetType);
					String spiStateDesc = pubFunc.getStaticName(spiSheetStatu);
					sheetPubInfoDao.updateSheetState(sheetId, spiSheetStatu, spiStateDesc, month, 0);
					sheetPubInfoDao.updateReceiveOrgBySheetId("400082", "集团工单处理组", sheetId);// 400082-集团工单处理组
					// 记录工单动作
					SheetActionInfo sheetActionInfo = new SheetActionInfo();
					sheetActionInfo.setWorkSheetId(sheetId);
					sheetActionInfo.setComments("强制释放我的任务中的集团二次派单工单到集团工单处理组");
					sheetActionInfo.setRegionId(regionId);
					sheetActionInfo.setServOrderId(orderId);
					sheetActionInfo.setMonth(month);
					saveSheetDealAction(sheetActionInfo, tacheId, StaticData.WKST_FORCE_RELEASE_ALLOCATE);
				}
			} else if (0 == lf) {// 工单池
				String roi = cur.getRcvOrgId();
				if (!"400082".equals(roi)) {// 非集团工单处理组
					sheetPubInfoDao.updateReceiveOrgBySheetId("400082", "集团工单处理组", sheetId);// 400082-集团工单处理组
					// 记录工单动作
					SheetActionInfo sheetActionInfo = new SheetActionInfo();
					sheetActionInfo.setWorkSheetId(sheetId);
					sheetActionInfo.setComments("强制释放工单池中的集团二次派单工单到集团工单处理组");
					sheetActionInfo.setRegionId(regionId);
					sheetActionInfo.setServOrderId(orderId);
					sheetActionInfo.setMonth(month);
					saveSheetDealAction(sheetActionInfo, tacheId, StaticData.WKST_FORCE_RELEASE_ALLOCATE);
				}
			}
		}
		return "SUCCESS";
	}

	// 2025-02,以上渠道，集团二次派单后工单流向了服务管理部（省投诉中心）岗，导致处理人员无法处理。因集团网站存在二次派单，现需将二次及多次派单后工单流向集团工单处理组。
	private boolean isAutoZDXChannel(int acceptChannelId) {
		return !"".equals(pubFunc.getSelRefundHotName(acceptChannelId, "15"));
	}

	private boolean saveSheetDealAction(SheetActionInfo sheetActionInfo, int tacheId, int actionType) {
		TsmStaff staff = pubFunc.getStaff(20001797);// 省投派单岗
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String orgId = staff.getOrganizationId();
		String orgName = staff.getOrgName();
		String guid = pubFunc.crtGuid();
		sheetActionInfo.setActionGuid(guid);
		sheetActionInfo.setTacheId(tacheId);
		sheetActionInfo.setTacheName(pubFunc.getStaticName(tacheId));
		sheetActionInfo.setActionCode(actionType);
		sheetActionInfo.setActionName(pubFunc.getStaticName(actionType));
		sheetActionInfo.setOpraOrgId(orgId);
		sheetActionInfo.setOpraOrgName(orgName);
		sheetActionInfo.setOpraStaffId(staffId);
		sheetActionInfo.setOpraStaffName(staffName);
		sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
		return true;
	}

	/**
	 * 查询最近一次挂起动作类型
	 * @param workSheetId 工单号
	 * @return 动作ID
	 */
    public int queryLastActionCodeBySheetId(String workSheetId) {
    	return sheetActionInfoDao.queryLastActionCodeBySheetId(workSheetId);
    }

	/**
	 * 隐藏工单
	 * @param workSheetId 工单号
	 * @param endDate 到期日期
	 * @return
	 */
	public int hidden(String workSheetId, String endDate) {
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
		int opraStaff = 0;
		if (pubFunc.getSystemAuthorization().getHttpSession() != null) {
			TsmStaff staff = pubFunc.getLogonStaff();
			opraStaff = Integer.parseInt(staff.getId());
		}
		if (1 == sheetActionInfoDao.saveSheetHiddenAction(workSheetId, sheetPubInfo.getServiceOrderId(), opraStaff, endDate)) {
			String result = saveSheetDealAction(sheetPubInfo.getServiceOrderId(), workSheetId, sheetPubInfo.getRegionId(), "到期提醒时间：" + endDate,
					sheetPubInfo.getMonth(), sheetPubInfo.getTacheId(), StaticData.WKST_TZ_HIDDEN_ACTION);
			if ("SUCCESS".equals(result)) {
				return 1;
			}
		}
		return 0;
	}

    /**
     * 隐藏工单(针对投诉，查询单隐藏)
     * @param workSheetId 工单号
     * @param endDate 到期日期
     * @return
     */
    public int hiddenSheet(String workSheetId, String endDate) {
        SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
        int opraStaff = 0;
        if (pubFunc.getSystemAuthorization().getHttpSession() != null) {
            TsmStaff staff = pubFunc.getLogonStaff();
            opraStaff = Integer.parseInt(staff.getId());
        }
        if (1 == sheetActionInfoDao.saveSheetHidden(workSheetId, sheetPubInfo.getServiceOrderId(), opraStaff, endDate)) {
            String result = saveSheetDealAction(sheetPubInfo.getServiceOrderId(), workSheetId, sheetPubInfo.getRegionId(), "到期提醒时间：" + endDate,
                    sheetPubInfo.getMonth(), sheetPubInfo.getTacheId(), StaticData.WKST_TZ_HIDDEN);
            if ("SUCCESS".equals(result)) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 手动取消隐藏
     * @param workSheetId 工单号
     * @return
     */
    public int stopHide(String workSheetId) {
        if (1 == sheetActionInfoDao.updateSheetHiddenStateBySheetId(1, workSheetId)) {
            SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
            if ("SUCCESS".equals(saveSheetDealAction(sheetPubInfo.getServiceOrderId(), workSheetId, sheetPubInfo.getRegionId(), "手动终止调账隐藏", sheetPubInfo.getMonth(),
                    sheetPubInfo.getTacheId(), StaticData.WKST_TZ_MANUAL_UNHIDDEN))) {
                return 1;
            }
        }
        return 0;
    }

	/**
	 * 手动取消隐藏
	 * @param workSheetId 工单号
	 * @return
	 */
	public int unHidden(String workSheetId) {
		if (1 == sheetActionInfoDao.updateSheetHiddenActionStateBySheetId(1, workSheetId)) {
			SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(workSheetId, false);
			if ("SUCCESS".equals(saveSheetDealAction(sheetPubInfo.getServiceOrderId(), workSheetId, sheetPubInfo.getRegionId(), "手动终止调账隐藏", sheetPubInfo.getMonth(),
					sheetPubInfo.getTacheId(), StaticData.WKST_TZ_MANUAL_UNHIDDEN_ACTION))) {
				return 1;
			}
		}
		return 0;
	}

    /**
     * 保存工单动作
     * 
     * @param orderId
     *            订单ID
     * @param sheetId
     *            工单ID
     * @param regionId
     *            地域ID
     * @param comment
     *            处理内容
     * @param month
     *            月分区
     * @param tachId
     *            所处环节ID
     * @param actionId
     *            动作ID
     * @param type
     * @return SUCCESS成功
     */
    public String saveSheetDealAction(String orderId, String sheetId, int regionId, String comment,
            Integer month, int tachId, int actionId) {
        SheetActionInfo action = new SheetActionInfo();
        action.setServOrderId(orderId);
        action.setWorkSheetId(sheetId);
        action.setRegionId(regionId);
        action.setComments(comment);
        action.setMonth(month);
        action.setTacheId(tachId);
        action.setTacheName(pubFunc.getStaticName(tachId));
        action.setActionCode(actionId);
        action.setActionName(pubFunc.getStaticName(actionId));
        action.setActionGuid(pubFunc.crtGuid());

        if (pubFunc.isLogonFlag()) {
            TsmStaff staff = pubFunc.getLogonStaff();
            action.setOpraOrgId(staff.getOrganizationId());
            action.setOpraOrgName(staff.getOrgName());
            action.setOpraStaffId(Integer.parseInt(staff.getId()));
            action.setOpraStaffName(staff.getName());
        }
        sheetActionInfoDao.saveSheetActionInfo(action);
        return "SUCCESS";
    }

    /**
     * 取得sheetActionInfoDao
     * 
     * @return 返回sheetActionInfoDao。
     */
    protected ISheetActionInfoDao getSheetActionInfoDao() {
        return sheetActionInfoDao;
    }

    /**
     * 设置sheetActionInfoDao
     * 
     * @param sheetActionInfoDao
     *            要设置的sheetActionInfoDao。
     */
    public void setSheetActionInfoDao(ISheetActionInfoDao sheetActionInfoDao) {
        this.sheetActionInfoDao = sheetActionInfoDao;
    }

    /**
     * 取得pubFunc
     * 
     * @return 返回pubFunc。
     */
    protected PubFunc getPubFunc() {
        return pubFunc;
    }

    /**
     * 设置pubFunc
     * 
     * @param pubFunc
     *            要设置的pubFunc。
     */
    public void setPubFunc(PubFunc pubFunc) {
        this.pubFunc = pubFunc;
    }

    /**
     * 取得sheetPubInfoDao
     * 
     * @return 返回sheetPubInfoDao。
     */
    protected ISheetPubInfoDao getSheetPubInfoDao() {
        return sheetPubInfoDao;
    }

    /**
     * 设置sheetPubInfoDao
     * 
     * @param sheetPubInfoDao
     *            要设置的sheetPubInfoDao。
     */
    public void setSheetPubInfoDao(ISheetPubInfoDao sheetPubInfoDao) {
        this.sheetPubInfoDao = sheetPubInfoDao;
    }

    /**
     * 取得orderAskInfoDao
     * 
     * @return 返回orderAskInfoDao。
     */
    protected IorderAskInfoDao getOrderAskInfoDao() {
        return orderAskInfoDao;
    }

    /**
     * 设置orderAskInfoDao
     * 
     * @param orderAskInfoDao
     *            要设置的orderAskInfoDao。
     */
    public void setOrderAskInfoDao(IorderAskInfoDao orderAskInfoDao) {
        this.orderAskInfoDao = orderAskInfoDao;
    }
}
