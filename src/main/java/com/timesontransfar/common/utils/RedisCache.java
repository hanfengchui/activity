package com.timesontransfar.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * spring redis 工具类
 * 
 * @author huangbaijun
 **/

//@Component
@SuppressWarnings("rawtypes")
public class RedisCache {
	//@Autowired
	@Resource 
	public RedisTemplate redisTemplate;

	/**
	 * 缓存基本的对象，Integer、String、实体类等
	 * @param key 缓存的键值
	 * @param value 缓存的值
	 * @return 缓存的对象
	 */
	public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		operation.set(key, value);
		return operation;
	}

	/**
	 * 缓存基本的对象，Integer、String、实体类等
	 *
	 * @param key
	 *            缓存的键值
	 * @param value
	 *            缓存的值
	 * @param timeout
	 *            时间
	 * @param timeUnit
	 *            时间颗粒度
	 * @return 缓存的对象
	 */
	public <T> ValueOperations<String, T> setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit) {
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		operation.set(key, value, timeout, timeUnit);
		return operation;
	}

	/**
	 * 获得缓存的基本对象。
	 *
	 * @param key
	 *            缓存键值
	 * @return 缓存键值对应的数据
	 */
	public <T> T getCacheObject(String key) {
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}

	/**
	 * 删除单个对象
	 *
	 * @param key
	 */
	public void deleteObject(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 删除集合对象
	 *
	 * @param collection
	 */
	public void deleteObject(Collection collection) {
		redisTemplate.delete(collection);
	}

	/**
	 * 缓存List数据
	 *
	 * @param key
	 *            缓存的键值
	 * @param dataList
	 *            待缓存的List数据
	 * @return 缓存的对象
	 */
	public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList) {
		ListOperations listOperation = redisTemplate.opsForList();
		if (null != dataList) {
			int size = dataList.size();
			for (int i = 0; i < size; i++) {
				listOperation.leftPush(key, dataList.get(i));
			}
		}
		return listOperation;
	}
	
	public void setCacheListSun(String key, List dataList) {
        //清空
        while (redisTemplate.opsForList().size(key) > 0){
            redisTemplate.opsForList().leftPop(key);
        }
        //存储
        redisTemplate.opsForList().rightPushAll(key, dataList);
	}

	/**
	 * 获得缓存的list对象
	 *
	 * @param key
	 *            缓存的键值
	 * @return 缓存键值对应的数据
	 */
	public <T> List<T> getCacheList(String key) {
		List<T> dataList = new ArrayList<T>();
		ListOperations<String, T> listOperation = redisTemplate.opsForList();
		Long size = listOperation.size(key);
		for (int i = 0; i < size; i++) {
			dataList.add(listOperation.index(key, i));
		}
		return dataList;
	}
	
	public <T> List<T> getCacheListSun(String key) {
        List<T> list = null;
        try {
        	list = redisTemplate.opsForList().range(key, 0, -1);
		} catch (Exception e) {
			// handle exception
		}
        return list;
	}
	

	/**
	 * 缓存Set
	 *
	 * @param key
	 *            缓存键值
	 * @param dataSet
	 *            缓存的数据
	 * @return 缓存数据的对象
	 */
	public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet) {
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		Iterator<T> it = dataSet.iterator();
		while (it.hasNext()) {
			setOperation.add(it.next());
		}
		return setOperation;
	}

	/**
	 * 获得缓存的set
	 *
	 * @param key
	 * @return
	 */
	public <T> Set<T> getCacheSet(String key) {
		Set<T> dataSet = new HashSet<T>();
		BoundSetOperations<String, T> operation = redisTemplate.boundSetOps(key);
		dataSet = operation.members();
		return dataSet;
	}

	/**
	 * 缓存Map
	 *
	 * @param key
	 * @param dataMap
	 * @return
	 */
	public <T> HashOperations<String, String, T> setCacheMap(String key, Map<String, T> dataMap) {
		HashOperations hashOperations = redisTemplate.opsForHash();
		if (null != dataMap) {
			for (Map.Entry<String, T> entry : dataMap.entrySet()) {
				hashOperations.put(key, entry.getKey(), entry.getValue());
			}
		}
		return hashOperations;
	}

	/**
	 * 获得缓存的Map
	 *
	 * @param key
	 * @return
	 */
	public <T> Map<String, T> getCacheMap(String key) {
		Map<String, T> map = redisTemplate.opsForHash().entries(key);
		return map;
	}

	/**
	 * 获得缓存的基本对象列表
	 * 
	 * @param pattern
	 *            字符串前缀
	 * @return 对象列表
	 */
	public Collection<String> keys(String pattern) {
		return redisTemplate.keys(pattern);
	}
	
	/**
	 * 删除以preStr为前缀的缓存
	 * @param preStr
	 */
	public void batchDel(String preStr) {
		Set<String> keys = redisTemplate.keys(preStr + "*");
		redisTemplate.delete(keys);
	}
}
