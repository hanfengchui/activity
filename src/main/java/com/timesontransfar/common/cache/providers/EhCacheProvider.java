package com.timesontransfar.common.cache.providers;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.ICacheProvider;
import com.timesontransfar.common.cache.exceptions.CacheException;

public class EhCacheProvider implements ICacheProvider {

    private static final Log log = LogFactory.getLog(EhCacheProvider.class);

	private static int referenceCount = 0;

	private CacheManager manager;

	private boolean open;

    public ICache buildCache(String name, Properties properties) throws CacheException {
	    try {
			manager = CacheManager.create();
            net.sf.ehcache.Cache cache = manager.getCache(name);
            if (cache == null) {
                log.debug("Could not find configuration [" + name + "]; using defaults.");
                manager.addCache(name);
                cache = manager.getCache(name);
                log.debug("started EHCache region: " + name);
            }
            EhCache ehCache=new EhCache(cache);
            ehCache.setOpen(this.open);
            return ehCache;
	    }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
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
