/*
* 创建日期 2006-8-26
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow;


/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public interface IMessageSenderFactory  {
	/**
	 * 根据jndiName取得发送对象
	 * @param jndiName
	 * @return
	 */
	public Object getObject(String jndiName);
}
