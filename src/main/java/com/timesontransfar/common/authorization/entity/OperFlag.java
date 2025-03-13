package com.timesontransfar.common.authorization.entity;

public  final class OperFlag {
	/*
	 *  0 查询  1 新增   2 修改  3 删除
	 */
	public static final OperFlag INSERT = new OperFlag(1);
	public static final OperFlag SELECT = new OperFlag(0);
	public static final OperFlag UPDATE = new OperFlag(2);
	public static final OperFlag DELETE = new OperFlag(3);
	
	private int operType ; 
	
    private OperFlag(int operType){
    	this.operType = operType;
    }
    
    public int operFlag(){
    	return this.operType;
    }
}
