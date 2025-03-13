package com.timesontransfar.common.authorization.entity;

import java.io.Serializable;
/*
 * 数据来源类
 */
public final class FuncType  implements Serializable {
	/*
	 *  0 菜单
	 */
	public static final FuncType MENU = new FuncType(0);

	private int funcType ;

    private FuncType(int funcType){
    	this.funcType = funcType;
    }

    public int funcType(){
    	return this.funcType;
    }
}
