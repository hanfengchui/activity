/**
 * <p>类名：SheetLimitTimeServiceImpl</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：chenjw</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by  chenjw 2008-3-29 15:29:16</p>
 * <p></p>
 */
package com.timesontransfar.customservice.paramconfig.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocate;
import com.timesontransfar.customservice.paramconfig.dao.IsheetLimitTimeDao;
import com.timesontransfar.customservice.paramconfig.service.IsheetLimitTimeService;

/**
 * @author chenjw
 * @date 2008-3-29 15:29:16
 */
@Component("sheetLimitTimeServ")
public class SheetLimitTimeServiceImpl implements IsheetLimitTimeService {
	@Autowired
	private IsheetLimitTimeDao sheetLimitTimeDao;

	/**
	 * 得到处理时限和预警时限
	 * @param region 地域
	 * @param serviceType 服务类型
	 * @param tacheId 环节
	 * @param limitType 时限类型
	 * @return
	 */
	public SheetLimitTimeCollocate getSheetLimitime(int region,int serviceType,int tacheId,int limitType,int custGread,int urgency) {
		SheetLimitTimeCollocate bena =  this.sheetLimitTimeDao.getSheetLimitime(region, serviceType, tacheId, limitType,custGread,urgency);
		if(bena == null) {
			bena = new SheetLimitTimeCollocate();
			bena.setLimitTime(9999);
			bena.setPrealarmValue(0);
		}
		return bena;
	}

	public int[] getLimitTimeNew(int servType, int comeCategory, int countOrder, int lastXX, int bestOrder) {
		String where = " AND service_type = " + servType;
		if (720130000 == servType) {
			if (707907001 == comeCategory) {
				where += " AND clique_flag = 0";
			} else if (707907002 == comeCategory) {
				where += " AND clique_flag = 1";
			} else {
				where += " AND clique_flag = 2";
			}
		}
		where += " AND other_condition IN (0";
		if (countOrder > 0) {
			where += ", 1";
		}
		if (bestOrder > 100122410) {
			where += ", 2";
		}
		where += ", " + lastXX + ")";
		return this.sheetLimitTimeDao.getLimitTimeNew(where);
	}
}