package com.timesontransfar.common.exception;
/**
 * 用于抛出业务异常
 * @author 罗翔 创建于2005-12-14
 */

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.exceptions.CacheException;

public class BusinessException extends RuntimeException {
	private String id;
	private ICache cache;

	public BusinessException() {
		super();
		// Auto-generated constructor stub
	}

	public BusinessException(String message) {
		super(message);
		// Auto-generated constructor stub
	}

	public BusinessException(Throwable cause) {
		super(cause);
		// Auto-generated constructor stub
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
		// Auto-generated constructor stub
	}

	public BusinessException(String message,String id){
		super(message);
		this.setId(id);
	}

	public BusinessException(String message, Throwable cause,String id) {
		super(message, cause);
		this.setId(id);
		// Auto-generated constructor stub
	}

	public String getMessage(){
		String message=super.getMessage();
		try{
			if(this.id!=null){
				BusinessExceptionObject object=(BusinessExceptionObject)this.cache.get(id);
				if(object!=null){
					message+="Business Infomation:<";
					message+="Type:"+object.getType();
					message+="Name:"+object.getName();
					message+="PossibleCause:"+object.getPossibleCause();
					message+="Description:"+object.getDescription();
					return message;
				}else{
					return message;
				}
			}else{
				return message;
			}
		}catch(CacheException e){
			return message;
		}
	}



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType(){
		try{
			if(this.id!=null){
				BusinessExceptionObject object=(BusinessExceptionObject)this.cache.get(id);
				if(object!=null){
					return object.getType();
				}else{
					return "BusinessException";
				}
			}else{
				return "BusinessException";
			}
		}catch(CacheException e){
			return "CacheException";
		}
	}

	public ICache getCache() {
		return cache;
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}
}
