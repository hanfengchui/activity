package com.timesontransfar.common.cache.exceptions;


/**
 * Something went wrong in the cache
 */
public class CacheException extends Exception {

	public CacheException(String s) {
		super(s);
	}

	public CacheException(String s, Exception e) {
		super(s, e);
	}

	public CacheException(Exception e) {
		super(e);
	}

}






