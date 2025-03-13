package com.timesontransfar.common.cache.providers;

import java.util.Iterator;
import java.util.Map;


import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.exceptions.CacheException;


public class OSCache implements ICache {

	private GeneralCacheAdministrator cache = new GeneralCacheAdministrator();

	private final int refreshPeriod;
	private final String cron;
	private final String regionName;
	private boolean open;

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	private String toString(Object key) {
		return String.valueOf(key) + '.' + regionName;
	}

	public OSCache(int refreshPeriod, String cron, String region) {
		this.refreshPeriod = refreshPeriod;
		this.cron = cron;
		this.regionName = region;
	}

	public void setCacheCapacity(int cacheCapacity) {
		cache.setCacheCapacity(cacheCapacity);
	}

	public Object get(Object key) throws CacheException {
		try {
			return this.open ? cache.getFromCache( toString(key), refreshPeriod, cron ):null;
		}
		catch (NeedsRefreshException e) {
			cache.cancelUpdate( toString(key) );
			return null;
		}
	}

	public Object read(Object key) throws CacheException {
		return get(key);
	}

	public void update(Object key, Object value) throws CacheException {
		if(this.open){
			this.put(key, value);			
		}
	}

	public void put(Object key, Object value) throws CacheException {
		cache.putInCache( toString(key), value );
	}

	public void remove(Object key) throws CacheException {
		cache.flushEntry( toString(key) );
	}

	public void clear() throws CacheException {
		cache.flushAll();
	}

	public void destroy() throws CacheException {
		cache.destroy();
	}

	public void lock(Object key) throws CacheException {
		// local cache, so we use synchronization
	}

	public void unlock(Object key) throws CacheException {
		// local cache, so we use synchronization
	}

	public int getTimeout() {
		return 1<<12 * 60000; //ie. 60 seconds
	}

	public String getRegionName() {
		return regionName;
	}

	public long getSizeInMemory() {
		return -1;
	}

	public long getElementCountInMemory() {
		return -1;
	}

	public long getElementCountOnDisk() {
		return -1;
	}

	public Map toMap() {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		return "OSCache(" + regionName + ')';
	}

	public Iterator getEntryIterator() {
		// Auto-generated method stub
		return null;
	}

	public Iterator getKeyIterator() {
		// Auto-generated method stub
		return null;
	}

}
