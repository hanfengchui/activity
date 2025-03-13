/*
 * 创建日期 2006-8-17
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.caucho.hessian.client.HessianProxyFactory;
import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.workflow.IWorkFlowInvoke;

/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public class WorkFlowInvokeImpl implements IWorkFlowInvoke,ApplicationContextAware {
	private ApplicationContext applicationContext;

	private ICache cache;

	private HessianProxyFactory factory = new HessianProxyFactory();

	private Map businessMap;


	/* （非 Javadoc）
	 * @see com.timesontransfar.common.workflow.IWorkFlowInvoke#invoke(String url,String name,String method,Object[] params)
	 */
	public Object invoke(String url,String name,String method,Object[] params){
		Object object = null;

		if (name.indexOf(".") > 0) {
			object = this.classInvoke(url, name, method,params);
			return object;
		} else {
			object = this.beanInvoke(url, name, method,params);
			return object;
		}

	}
	/* （非 Javadoc）
	 * @see com.timesontransfar.common.workflow.IWorkFlowInvoke#BeanInvoke(java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public Object beanInvoke(String url, String beanName, String method,
			Object[] params) {
		// 自动生成方法存根
		if(url!=null){
			if(url.length()>10){
				//如果Url比较合理，则使用Hessian的远程调用
				IWorkFlowInvoke remoteInvoke=this.getBusinessInvoke(url);
				return remoteInvoke.invoke(null,beanName,method,params);
			}else{
				return this.invokeFunction(beanName,method,params,true);
			}
		}else{
			return this.invokeFunction(beanName,method,params,true);
		}
	}

	/* （非 Javadoc）
	 * @see com.timesontransfar.common.workflow.IWorkFlowInvoke#ClassInvoke(java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public Object classInvoke(String url, String className, String method,
			Object[] params) {
		// 自动生成方法存根
		if(url!=null){
			if(url.length()>10){
				//如果Url比较合理，则使用Hessian的远程调用
				IWorkFlowInvoke remoteInvoke=this.getBusinessInvoke(url);
				return remoteInvoke.invoke(null,className,method,params);
			}else{
				return this.invokeFunction(className,method,params,false);
			}
		}else{
			return this.invokeFunction(className,method,params,false);
		}
	}

	/* （非 Javadoc）
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// 自动生成方法存根
		this.applicationContext=arg0;
	}

	/**
	 * 反射调用
	 * @param pubMessage
	 * @param function
	 * @throws BusinessException
	 */
	private Object invokeFunction(String name,String methodName,Object[] params,boolean isBean) throws RuntimeException {
		try {
			Object instanceObject=null;
			if(isBean){
				instanceObject = this.applicationContext.getBean(name);
			}else{
				instanceObject=this.instantialObject(name);
			}
			if(instanceObject==null){
				throw new RuntimeException("不能根据名称实例化指定的类或者Bean！");
			}
/*			Class[] parameterTypes = new Class[params.length];
			for(int i=0;i<params.length;i++){
				//parameterTypes[i]=params[i].getClass().;
				此处因为是反射调用，必须确定参数的具体类，
				 * 因此要求被工作流调用的方法都必须只有一个参数
				 * 且这个参数是Map，而Map也完全可以满足传递多参数的需求
				 *
				parameterTypes[i]=Map.class;
			}
*/
			Class[] parameterTypes=new Class[]{Map.class};

			Method invokeMethod = instanceObject.getClass().getDeclaredMethod(
					methodName, parameterTypes);
			if(invokeMethod!=null){
				return invokeMethod.invoke(instanceObject, params);
			}else{
				throw new RuntimeException("没有找到和Bean、方法、以及参数对应的方法！");
			}
		} catch (Throwable e) {
			StringWriter writer = new StringWriter();
			PrintWriter s = new PrintWriter(writer);
			e.printStackTrace(s);
			String exception = "调用方法错误，Bean ID='" + name + "',方法名：'"
					+ methodName+ "'";
			exception += "\n错误信息为：" + writer.toString() + " ";
			throw new RuntimeException(exception);
		}
	}

	private Object instantialObject(String className) {
		try{
	        Object instanceObject = null;
	        instanceObject = this.cache.get(className);
	        if(instanceObject!=null){
	        	return instanceObject;
	        }else{
	    		Class clazz=Class.forName(className);
	    		Constructor constructor = clazz.getConstructor(new Class[]{});
	    		instanceObject = constructor.newInstance(new Object[]{});
	    		Method[] methods=clazz.getMethods();
	    		for(int i=0;i<methods.length;i++){
	    			String methodName=methods[i].getName();
	    			if(methodName.startsWith("set")){
	    				String beanName=methodName.substring(3);
	    				Object attributeInstance=this.applicationContext.getBean(beanName);
	    			    methods[i].invoke(instanceObject,attributeInstance);
	    			}
	    		}
	    		this.cache.put(className,instanceObject);
	        }
			return instanceObject;
		}catch(Throwable e){
			StringWriter writer = new StringWriter();
			PrintWriter s = new PrintWriter(writer);
			e.printStackTrace(s);
			String exception = "\n错误信息为：" + writer.toString() + " ";
			throw new RuntimeException("根据类名创建实例失败！" + exception);
		}
	}

	private IWorkFlowInvoke getBusinessInvoke(String url) throws RuntimeException{
		IWorkFlowInvoke businessInvoke=(IWorkFlowInvoke)this.businessMap.get(url);
		if(businessInvoke!=null){
			return businessInvoke;
		}
		try{
			//Basic basic = (Basic) factory.create(Basic.class, url);
			businessInvoke=(IWorkFlowInvoke)factory.create(IWorkFlowInvoke.class,url);
			this.businessMap.put(url,businessInvoke);
			return businessInvoke;
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}



	/**
	 * @return 返回 cache。
	 */
	public ICache getCache() {
		return cache;
	}
	/**
	 * @param cache 要设置的 cache。
	 */
	public void setCache(ICache cache) {
		this.cache = cache;
	}
}
