package com.timesontransfar.common.authorization.service;


import org.springframework.dao.DataAccessException;

public interface IAuthPersist {
	public String save(Object object) throws DataAccessException;

	public String delete(Object object) throws DataAccessException;

}
