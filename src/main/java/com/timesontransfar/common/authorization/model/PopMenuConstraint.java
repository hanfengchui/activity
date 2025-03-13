/*
 * 创建日期 2006-5-31
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.authorization.model;

/**
 * @author chenke
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class PopMenuConstraint implements IConstraint,java.io.Serializable{
	private String id;

	public String doConstraint() {
		// Auto-generated method stub
		return "POP__"+this.getId();
	}

	public String getId() {
		// Auto-generated method stub
		return this.id;
	}

	public void setId(String constraintId) {
		// Auto-generated method stub
		this.id=constraintId;
	}
}
