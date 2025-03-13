/**
 * Copyright @ 2013 transfar
 * All right reserved
 */
package com.timesontransfar.baseDao.exception;


/**
 * <p>Class Name: DaoException</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-13</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class DaoException extends BaseRuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3193338282388551297L;

	public DaoException() {
		super();
	}
	
	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DaoException(String message) {
		super(message);
	}

	public DaoException(Throwable cause) {
		super(cause);
	}
	
	@Override
	public String toString() {
		String throwableMessage = "";
		if(getThrowable() != null) {
			throwableMessage = getThrowable().toString();
		}
		return super.toString() + throwableMessage;
	}
	
	
}
