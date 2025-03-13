package com.timesontransfar.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

@SuppressWarnings("rawtypes")
public final class PropertiesHelper {

	public static boolean getBoolean(String property, Properties properties) {
		return Boolean.parseBoolean(properties.getProperty(property));
	}

	public static boolean getBoolean(String property, Properties properties, boolean defaultValue) {
		String setting = properties.getProperty(property);
		return (setting==null) ? defaultValue : Boolean.parseBoolean(setting);
	}

	public static int getInt(String property, Properties properties, int defaultValue) {
		String propValue = properties.getProperty(property);
		return (propValue==null) ? defaultValue : Integer.parseInt(propValue);
	}

	public static String getString(String property, Properties properties, String defaultValue) {
		String propValue = properties.getProperty(property);
		return (propValue==null) ? defaultValue : propValue;
	}

	public static Integer getInteger(String property, Properties properties) {
		String propValue = properties.getProperty(property);
		return (propValue==null) ? null : Integer.valueOf(propValue);
	}

	@SuppressWarnings("unchecked")
	public static Map toMap(String property, String delim, Properties properties) {
		Map map = new HashMap();
		String propValue = properties.getProperty(property);
		if (propValue!=null) {
			StringTokenizer tokens = new StringTokenizer(propValue, delim);
			while ( tokens.hasMoreTokens() ) {
				map.put(
					tokens.nextToken(),
					tokens.hasMoreElements() ? tokens.nextToken() : ""
				);
			}
		}
		return map;
	}

	/**
	 * replace a property by a starred version
	 *
	 * @param props properties to check
	 * @param key proeprty to mask
	 * @return cloned and masked properties
	 */
	public static Properties maskOut(Properties props, String key) {
		Properties clone = (Properties) props.clone();
		if (clone.get(key) != null) {
			clone.setProperty(key, "****");
		}
		return clone;
	}



	private PropertiesHelper() {}
}






