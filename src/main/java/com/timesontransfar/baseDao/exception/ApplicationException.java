/**
 * Copyright @ 2013 transfar
 * All right reserved
 */
package com.timesontransfar.baseDao.exception;


/**
 * <p>Class Name: ApplicationException</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-13</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class ApplicationException extends BaseRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7283622245255273019L;

	public ApplicationException() {
		super();
	}
	
	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ApplicationException(String message) {
		super(message);
	}

	public ApplicationException(Throwable cause) {
		super(cause);
	}
}
