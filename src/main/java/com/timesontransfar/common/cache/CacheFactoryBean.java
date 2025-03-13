package com.timesontransfar.common.cache;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.timesontransfar.common.cache.exceptions.CacheException;
@SuppressWarnings("rawtypes")
public final class CacheFactoryBean implements FactoryBean, InitializingBean{

	private static final Log log = LogFactory.getLog(CacheFactoryBean.class);

	private ICacheProvider cacheProvider;
	private String regionName;
	private String prefix;
	private ICache cache;
	private Properties properties;

	public void afterPropertiesSet() throws CacheException{

		if ( this.prefix!=null ) this.regionName = this.prefix + '.' + this.regionName;

		if ( log.isDebugEnabled() ) log.debug("instantiating cache region: " + regionName);

		try {
			cache = this.cacheProvider.buildCache(regionName, properties);
		}
		catch (CacheException e) {
			e.printStackTrace();
			throw new CacheException( "Could not instantiate cache implementation", e );
		}

	}

	public Object getObject() {
		return this.cache;
	}

	public Class getObjectType() {
		return (this.cache != null ? this.cache.getClass() :ICache.class);
	}

	public boolean isSingleton() {
		return true;
	}

    public ICacheProvider getCacheProvider() {
        return this.cacheProvider;
    }

    public void setCacheProvider(ICacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }


    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

   public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
