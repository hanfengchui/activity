/*
 * 创建日期 2006-8-17
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
public interface IWorkFlowInvoke{
	/**
	 * 反射调用方法
	 * @param url 如果有Url地址，将是一个远程的Hessian调用，在使用这个方法的时候，需要将本接口及实现类部署在远程系统中，才可使用此方法
	 * @param name Bean或者类的名称
	 * @param method 方法名称
	 * @param params 参数
	 * @return 方法调用后返回的对象
	 */
	public Object invoke(String url,String name,String method,Object[] params);
}
