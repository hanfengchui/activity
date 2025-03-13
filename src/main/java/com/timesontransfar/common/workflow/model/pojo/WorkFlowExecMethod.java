/*
 * 创建日期 2006-8-15
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.util.List;

/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public class WorkFlowExecMethod implements Cloneable,java.io.Serializable{
	private String methodcode;

	private double version;

	private String methodguid;

	private String methodname;

	private String url;

	private String javaclass;

	private String javamethod;

	private List outParameterConfig;

	private List inParameterConfig;

	public String getJavaclass() {
		return javaclass;
	}

	public void setJavaclass(String javaclass) {
		this.javaclass = javaclass;
	}

	public String getJavamethod() {
		return javamethod;
	}

	public void setJavamethod(String javamethod) {
		this.javamethod = javamethod;
	}

	public String getMethodcode() {
		return methodcode;
	}

	public void setMethodcode(String methodcode) {
		this.methodcode = methodcode;
	}

	public String getMethodguid() {
		return methodguid;
	}

	public void setMethodguid(String methodguid) {
		this.methodguid = methodguid;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	/**
	 * @return 返回 inParameterConfig。
	 */
	public List getInParameterConfig() {
		return inParameterConfig;
	}
	/**
	 * @param inParameterConfig 要设置的 inParameterConfig。
	 */
	public void setInParameterConfig(List inParameterConfig) {
		this.inParameterConfig = inParameterConfig;
	}
	/**
	 * @return 返回 outParameterConfig。
	 */
	public List getOutParameterConfig() {
		return outParameterConfig;
	}
	/**
	 * @param outParameterConfig 要设置的 outParameterConfig。
	 */
	public void setOutParameterConfig(List outParameterConfig) {
		this.outParameterConfig = outParameterConfig;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() {
		if(this.inParameterConfig!=null){
			this.inParameterConfig.clear();
		}
		if(this.outParameterConfig!=null){
			this.outParameterConfig.clear();
		}
	}

	public Object clone() {
		WorkFlowExecMethod copy = null;
		try {
			copy = (WorkFlowExecMethod)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
}
