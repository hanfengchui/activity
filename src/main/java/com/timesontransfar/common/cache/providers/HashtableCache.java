package com.timesontransfar.common.cache.providers;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import java.util.Map;

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.exceptions.CacheException;

@SuppressWarnings("rawtypes")
public class HashtableCache implements ICache {

	private final Map hashtable = new Hashtable();
	private final String regionName;
	private boolean open;

	public HashtableCache(String regionName) {
		this.regionName = regionName;
	}

	public String getRegionName() {
		return regionName;
	}

	public Object read(Object key) throws CacheException {
		return hashtable.get(key);
	}

	public Object get(Object key) throws CacheException {
		return hashtable.get(key);
	}

	public void update(Object key, Object value) throws CacheException {
		put(key, value);
	}

	public void put(Object key, Object value) throws CacheException {
		hashtable.put(key, value);
	}

	public void remove(Object key) throws CacheException {
		hashtable.remove(key);
	}

	public void clear() throws CacheException {
		hashtable.clear();
	}

	public void destroy() throws CacheException { //空白方法

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

	public long getSizeInMemory() {
		return -1;
	}

	public long getElementCountInMemory() {
		return hashtable.size();
	}

	public long getElementCountOnDisk() {
		return 0;
	}

	public Map toMap() {
		return Collections.unmodifiableMap(hashtable);
	}

	public String toString() {
		return "HashtableCache(" + regionName + ')';
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public Iterator getEntryIterator() {
		// Auto-generated method stub
		try{
			return this.hashtable.entrySet().iterator();
		}catch(Throwable e){
			return null;			
		}
	}

	public Iterator getKeyIterator() {
		// Auto-generated method stub
		try{
			return this.hashtable.keySet().iterator();
		}catch(Throwable e){
			return null;			
		}
	}

}
