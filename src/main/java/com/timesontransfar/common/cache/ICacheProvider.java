package com.timesontransfar.common.cache;

import java.util.Properties;

import com.timesontransfar.common.cache.exceptions.CacheException;

public interface ICacheProvider {

	public ICache buildCache(String regionName, Properties properties) throws CacheException;

	public boolean isMinimalPutsEnabledByDefault();

}
