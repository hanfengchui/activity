package com.timesontransfar.common.workflow.model.pojo;

public class OuterSystemInfo implements Cloneable,java.io.Serializable{
	private String systemid;

	private String systemname;

	private String jmsurl;

	private String jmsqueue;

	public String getJmsqueue() {
		return jmsqueue;
	}

	public void setJmsqueue(String jmsqueue) {
		this.jmsqueue = jmsqueue;
	}

	public String getJmsurl() {
		return jmsurl;
	}

	public void setJmsurl(String jmsurl) {
		this.jmsurl = jmsurl;
	}

	public String getSystemid() {
		return systemid;
	}

	public void setSystemid(String systemid) {
		this.systemid = systemid;
	}

	public String getSystemname() {
		return systemname;
	}

	public void setSystemname(String systemname) {
		this.systemname = systemname;
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		OuterSystemInfo copy = null;
		try {
			copy = (OuterSystemInfo)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
}
