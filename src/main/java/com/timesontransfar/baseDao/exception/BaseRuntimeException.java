/**
 * Copyright @ 2013 transfar
 * All right reserved
 */
package com.timesontransfar.baseDao.exception;

/**
 * <p>Class Name: BaseRuntimeException</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-12</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class BaseRuntimeException  extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2830859660662120702L;
	

	private Throwable throwable = null;
	
	public Throwable getThrowable() {
		return throwable;
	}
	
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public BaseRuntimeException() {
		super();
	}
	
	public BaseRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BaseRuntimeException(String message) {
		super(message);
	}

	public BaseRuntimeException(Throwable cause) {
		super(cause);
	}

}
