package com.timesontransfar.common.cache.providers;

import java.util.Properties;

import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.cache.ICacheProvider;
import com.timesontransfar.common.cache.exceptions.CacheException;
import com.timesontransfar.common.util.PropertiesHelper;
import com.timesontransfar.common.util.StringHelper;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.Config;

/**
 * Support for OpenSymphony OSCache. This implementation assumes
 * that identifiers have well-behaved <tt>toString()</tt> methods.
 */
public class OSCacheProvider implements ICacheProvider {

	/**
	 * The <tt>OSCache</tt> refresh period property suffix.
	 */
	public static final String OSCACHE_REFRESH_PERIOD = "refresh.period";
	/**
	 * The <tt>OSCache</tt> CRON expression property suffix.
	 */
	public static final String OSCACHE_CRON = "cron";
	/**
	 * The <tt>OSCache</tt> cache capacity property suffix.
	 */
	public static final String OSCACHE_CAPACITY = "capacity";

	private static final Properties OSCACHE_PROPERTIES = new Config().getProperties();

	private boolean open;

	public ICache buildCache(String region, Properties properties) throws CacheException {

        Properties defaultedProps = new Properties(OSCACHE_PROPERTIES);
        if(properties != null)
            defaultedProps.putAll(properties);

		int refreshPeriod = PropertiesHelper.getInt(
			StringHelper.qualify(region, OSCACHE_REFRESH_PERIOD),
			defaultedProps,
			CacheEntry.INDEFINITE_EXPIRY
		);
		String cron = defaultedProps.getProperty( StringHelper.qualify(region, OSCACHE_CRON) );

		// construct the cache
		final OSCache cache = new OSCache(refreshPeriod, cron, region);

		Integer capacity = PropertiesHelper.getInteger( StringHelper.qualify(region, OSCACHE_CAPACITY), defaultedProps );
		if ( capacity!=null ) cache.setCacheCapacity( capacity.intValue() );
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
