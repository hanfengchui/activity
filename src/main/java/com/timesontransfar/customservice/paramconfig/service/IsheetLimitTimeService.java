/**
 * <p>类名：IsheetLimitTimeService</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：chenjw</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by  chenjw 2008-3-29 15:21:53</p>
 * <p></p>
 */
package com.timesontransfar.customservice.paramconfig.service;

import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocate;
/**
 * @author chenjw
 * @date 2008-3-29 15:21:53
 */
public interface IsheetLimitTimeService {

	/**
	 * 得到处理时限和预警时限
	 * @param region 地域
	 * @param serviceType 服务类型
	 * @param tacheId 环节
	 * @param limitType 时限类型
	 * @return
	 */
	public SheetLimitTimeCollocate getSheetLimitime(
			int region,
			int serviceType,
			int tacheId,
			int limitType,
			int custGread,
			int urgency
	);

	public int[] getLimitTimeNew(int servType, int comeCategory, int countOrder, int lastXX, int bestOrder);
}