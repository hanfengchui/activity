/**
 * <p>类名：SheetLimitTimePojo</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：chenjw</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by  chenjw 2008-3-29 10:28:47</p>
 * <p></p>
 */
package com.timesontransfar.customservice.paramconfig.pojo;

import java.io.Serializable;

/**
 * @author chenjw
 * @date 2008-3-29 10:28:47
 */
public class SheetLimitTimePojo implements Serializable{
	private static final long serialVersionUID = 1L;
	private SheetLimitTimeItem[] sheetLimitTimeItem;//时限规则项Pojo
	private SheetLimitTimeRule sheetLimitTimeRule;//时限规则Pojo
	/**
	 * @return 返回 sheetLimitTimeItem。
	 */
	public SheetLimitTimeItem[] getSheetLimitTimeItem() {
		return sheetLimitTimeItem;
	}
	/**
	 * @param sheetLimitTimeItem 要设置的 sheetLimitTimeItem。
	 */
	public void setSheetLimitTimeItem(SheetLimitTimeItem[] sheetLimitTimeItem) {
		this.sheetLimitTimeItem = sheetLimitTimeItem;
	}
	/**
	 * @return 返回 sheetLimitTimeRule。
	 */
	public SheetLimitTimeRule getSheetLimitTimeRule() {
		return sheetLimitTimeRule;
	}
	/**
	 * @param sheetLimitTimeRule 要设置的 sheetLimitTimeRule。
	 */
	public void setSheetLimitTimeRule(SheetLimitTimeRule sheetLimitTimeRule) {
		this.sheetLimitTimeRule = sheetLimitTimeRule;
	}

}
