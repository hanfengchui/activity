/*
 * 创建日期 2005-12-22
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.authorization.model;

/**
 * @author chenke
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class CtrlConstraint implements IConstraint,java.io.Serializable {
	private String id;
	/* （非 Javadoc）
	 * @see com.timesontransfar.common.authorization.model.IConstraint#getId()
	 */
	public String getId() {
		return this.id;
	}

	/* （非 Javadoc）
	 * @see com.timesontransfar.common.authorization.model.IConstraint#setId(java.lang.String)
	 */
	public void setId(String constraintId) {
		this.id=constraintId;
	}

	/* （非 Javadoc）
	 * @see com.timesontransfar.common.authorization.model.IConstraint#doConstraint()
	 */
	public String doConstraint() {
		return "CTRL__"+this.getId();
	}

}
