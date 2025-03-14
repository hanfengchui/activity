package com.timesontransfar.common.cache;

import java.util.Iterator;
import java.util.Map;

import com.timesontransfar.common.cache.exceptions.CacheException;




/**
 * Implementors define a caching algorithm. All implementors
 * <b>must</b> be threadsafe.
 */
@SuppressWarnings("rawtypes")
public interface ICache {
	/**
	 * Get an item from the cache
	 * @param key
	 * @return the cached object or <tt>null</tt>
	 * @throws CacheException
	 */
	public Object read(Object key) throws CacheException;
	/**
	 * Get an item from the cache, nontransactionally
	 * @param key
	 * @return the cached object or <tt>null</tt>
	 * @throws CacheException
	 */
	public Object get(Object key) throws CacheException;
	/**
	 * Add an item to the cache, nontransactionally, with
	 * failfast semantics
	 * @param key
	 * @param value
	 * @throws CacheException
	 */
	public void put(Object key, Object value) throws CacheException;
	/**
	 * Add an item to the cache
	 * @param key
	 * @param value
	 * @throws CacheException
	 */
	public void update(Object key, Object value) throws CacheException;
	/**
	 * Remove an item from the cache
	 */
	public void remove(Object key) throws CacheException;
	/**
	 * Clear the cache
	 */
	public void clear() throws CacheException;
	/**
	 * Clean up
	 */
	public void destroy() throws CacheException;
	/**
	 * If this is a clustered cache, lock the item
	 */
	public void lock(Object key) throws CacheException;
	/**
	 * If this is a clustered cache, unlock the item
	 */
	public void unlock(Object key) throws CacheException;
	/**
	 * Get a reasonable "lock timeout"
	 */
	public int getTimeout();

	/**
	 * Get the name of the cache region
	 */
	public String getRegionName();

	/**
	 * The number of bytes is this cache region currently consuming in memory.
	 *
	 * @return The number of bytes consumed by this region; -1 if unknown or
	 * unsupported.
	 */
	public long getSizeInMemory();

	/**
	 * The count of entries currently contained in the regions in-memory store.
	 *
	 * @return The count of entries in memory; -1 if unknown or unsupported.
	 */
	public long getElementCountInMemory();

	/**
	 * The count of entries currently contained in the regions disk store.
	 *
	 * @return The count of entries on disk; -1 if unknown or unsupported.
	 */
	public long getElementCountOnDisk();

	/**
	 * optional operation
	 */
	public Map toMap();
	/**
	 * 取得key的集合 
	 * @return
	 */
	public Iterator getKeyIterator();
	/**
	 * 取得元素的集合
	 * @return
	 */
	public Iterator getEntryIterator();

	public boolean isOpen();
}






