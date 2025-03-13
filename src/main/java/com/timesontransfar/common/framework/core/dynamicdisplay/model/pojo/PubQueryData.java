package com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
@SuppressWarnings("rawtypes")
public class PubQueryData implements Serializable{
	private int totalCount;
	private int begin;
	private int end;
	private Map keyMap;
	private List resultData;

	public PubQueryData() {
		super();
		// Auto-generated constructor stub
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public Map getKeyMap() {
		return keyMap;
	}

	public void setKeyMap(Map keyMap) {
		this.keyMap = keyMap;
	}

	public List getResultData() {
		return resultData;
	}

	public void setResultData(List resultData) {
		this.resultData = resultData;
	}

	protected void finalize() throws Throwable{
		if (this.resultData != null) {
			this.resultData.clear();
			this.resultData = null;
		}
		super.finalize();
	}

}
