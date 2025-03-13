package com.timesontransfar.common.authorization.service;
@SuppressWarnings("rawtypes")
public interface IAuthPersistFactory {
	public IAuthPersist getPersist(Class clazz);
}
