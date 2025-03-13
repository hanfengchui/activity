package com.timesontransfar.common.util;

import java.lang.reflect.Method;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Various utilities, mostly to make up for JDK 1.4 functionallity that is not
 * in JDK 1.3
 * 
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public final class LocalUtil {
	/**
	 * splitInbound() returns the type info in this parameter
	 */
	public static final int INBOUND_INDEX_TYPE = 0;

	/**
	 * splitInbound() returns the value info in this parameter
	 */
	public static final int INBOUND_INDEX_VALUE = 1;

	/**
	 * Prevent instansiation
	 */
	private LocalUtil() {
	}

	/**
	 * URL decode a value. This method gets around the lack of a decode(String,
	 * String) method in JDK 1.3.
	 * 
	 * @param value
	 *            The string to decode
	 * @return The decoded string
	 */
	public static String decode(String value) {
		if (!testedDecoder) {
			try {
				decode14 = URLDecoder.class.getMethod(
						"decode", String.class, String.class); //$NON-NLS-1$
			} catch (Exception ex) {
				if (!warn13) {
					log
							.warn("URLDecoder.decode(String, String) is not available. Falling back to 1.3 variant."); //$NON-NLS-1$
					warn13 = true;
				}
			}

			testedDecoder = true;
		}

		if (decode14 != null) {
			try {
				return (String) decode14.invoke(null, value,
						"UTF-8" ); //$NON-NLS-1$
			} catch (Exception ex) {
				log.warn("Failed to use JDK 1.4 decoder", ex); //$NON-NLS-1$
			}
		}
		try {
			return URLDecoder.decode(value,"UTF-8");

		} catch (Exception e) {
			return value;
		}

	}

	/**
	 * The log stream
	 */
	private static final Log log = LogFactory.getLog(LocalUtil.class);

	private static boolean warn13 = false;

	private static boolean testedDecoder = false;

	private static Method decode14 = null;
}
