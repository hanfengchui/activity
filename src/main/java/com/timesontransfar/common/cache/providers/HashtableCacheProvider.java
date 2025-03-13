package com.timesontransfar.common.cache.providers;

import java.util.Properties;

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.ICacheProvider;
import com.timesontransfar.common.cache.exceptions.CacheException;


public class HashtableCacheProvider implements ICacheProvider {
	private boolean open;

	public ICache buildCache(String regionName, Properties properties) throws CacheException {
		HashtableCache cache=new HashtableCache( regionName );
		cache.setOpen(this.open);
		return cache;
	}

	public boolean isMinimalPutsEnabledByDefault() {
		return false;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

}

