package com.timesontransfar.common.cache.providers;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.exceptions.CacheException;;
@SuppressWarnings("rawtypes")
public class EhCache implements ICache {
    private static final Log log = LogFactory.getLog(EhCache.class);
    private boolean open;

    private net.sf.ehcache.Cache cache;

    public EhCache(net.sf.ehcache.Cache cache) {
	    this.cache = cache;
    }

    /**
     * Gets a value of an element which matches the given key.
     * @param key the key of the element to return.
     * @return The value placed into the cache with an earlier put, or null if not found or expired
     * @throws CacheException
     */
    public Object get(Object key) throws CacheException {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("key: " + key);
            }
            if (key == null) {
                return null;
            }
            else {
                Element element = cache.get( (Serializable) key );
                if (element == null) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Element for " + key + " is null");
                    }
                    return null;
                }
                else {
                    return element.getValue();
                }
            }
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

	public Object read(Object key) throws CacheException {
		return get(key);
	}


    /**
     * Puts an object into the cache.
     * @param key a {@link Serializable} key
     * @param value a {@link Serializable} value
     * @throws CacheException if the parameters are not {@link Serializable}, the {@link CacheManager}
     * is shutdown or another {@link Exception} occurs.
     */
    public void update(Object key, Object value) throws CacheException {
		put(key, value);
    }

    /**
     * Puts an object into the cache.
     * @param key a {@link Serializable} key
     * @param value a {@link Serializable} value
     * @throws CacheException if the parameters are not {@link Serializable}, the {@link CacheManager}
     * is shutdown or another {@link Exception} occurs.
     */
	public void put(Object key, Object value) throws CacheException {
        try {
            Element element = new Element( (Serializable) key, (Serializable) value );
            cache.put(element);
        }
        catch (IllegalArgumentException e) {
            throw new CacheException(e);
        }
        catch (IllegalStateException e) {
            throw new CacheException(e);
        }

    }

    /**
     * Removes the element which matches the key.
     * <p>
     * If no element matches, nothing is removed and no Exception is thrown.
     * @param key the key of the element to remove
     * @throws CacheException
     */
    public void remove(Object key) throws CacheException {
        try {
            cache.remove( (Serializable) key );
        }
        catch (ClassCastException e) {
            throw new CacheException(e);
        }
        catch (IllegalStateException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Remove all elements in the cache, but leave the cache
     * in a useable state.
     * @throws CacheException
     */
    public void clear() throws CacheException {
        try {
            cache.removeAll();
        }
        catch (IllegalStateException e) {
            throw new CacheException(e);
        }
        catch (IOException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Remove the cache and make it unuseable.
     * @throws CacheException
     */
    public void destroy() throws CacheException {
        try {
            CacheManager.getInstance().removeCache( cache.getName() );
        }
        catch (IllegalStateException e) {
            throw new CacheException(e);
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Calls to this method should perform there own synchronization.
     * It is provided for distributed caches. Because EHCache is not distributed
     * this method does nothing.
     */
    public void lock(Object key) throws CacheException { //空白方法
    }

    /**
     * Calls to this method should perform there own synchronization.
     * It is provided for distributed caches. Because EHCache is not distributed
     * this method does nothing.
     */
    public void unlock(Object key) throws CacheException {//空白方法
    }

    /**
     * Returns the lock timeout for this cache.
     */
    public int getTimeout() {
        // 60 second lock timeout
        return 1<<12 * 60000;
    }

	public String getRegionName() {
		return cache.getName();
	}

	public long getSizeInMemory() {
		try {
			return cache.calculateInMemorySize();
		}
		catch(Throwable t) {
			return -1;
		}
	}

	public long getElementCountInMemory() {
	    long result = 0;
		try {

			result = cache.getSize();
		}
		catch (net.sf.ehcache.CacheException ce) {
            if ( log.isWarnEnabled() ) {
                log.debug("Exception: " + ce );
            }
		}

		return result;
	}

	public long getElementCountOnDisk() {
		return cache.getDiskStoreSize();
	}

	public Map toMap() {
		Map result = new HashMap();
		try {
			Iterator iter = cache.getKeys().iterator();
			while ( iter.hasNext() ) {
				Object key = iter.next();
				result.put( key, cache.get( (Serializable) key ).getValue() );
			}

		}
		catch (Exception e) {
            if ( log.isWarnEnabled() ) {
                log.debug("Exception: " + e );
            }
		}
		return result;
	}

	public String toString() {
		return "EHCache(" + getRegionName() + ')';
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public Iterator getEntryIterator() {
		// Auto-generated method stub
		return null;
	}

	public Iterator getKeyIterator() {
		// Auto-generated method stub
		try{
			return cache.getKeys().iterator();			
		}catch(Throwable e){
			return null;			
		}
	}

}