package com.timesontransfar.baseDao.util;

/**
 * Copyright @ 2013 transfar
 * All right reserved
 */
import java.util.Collection;

import com.timesontransfar.baseDao.exception.EmptyException;

/**
 * <p>Class Name: EmptyUtil</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-12</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class EmptyUtil {
    /**
     * 创建一个新的实例 EmptyUtil.
     * <p>Title:空构造函数 </p>
     * <p>Description: 空构造函数</p>
     */
    private EmptyUtil() {

    }

    /**
     * 描述: 空值，或者空字符串.
     * @param str 输入值
     * @return 空值返回true否则返回false
     * @author     "sunli"
     * <p>Sample: 该方法使用样例</p>
     * date        2011-11-30
     * --------------------------------------------------
     * 修改人                                               修改日期                               修改描述
     * "sunli"        2011-11-30                               创建
     * --------------------------------------------------
     * @Version  Ver1.0
     */
    public static boolean isBlank(String str) {
        if ((str == null) || ("".equals(str.trim()))) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 描述: 判断字符串是否不为空值或空字符串.
     * @param str 输入字符串
     * @return 不为空字符串 返回tue 否则返回false
     * @author     "sunli"
     * <p>Sample: 该方法使用样例</p>
     * date        2011-11-30
     * ----------------------------------------------------
     * 修改人                                             修改日期                                 修改描述
     * "sunli"        2011-11-30            创建
     * ---------------------------------------------------
     * @Version  Ver1.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 描述:判断集合是否为空 或者大小为零.
     * @param coll 结合
     * @return 如果集合为空或者大小为零返回true，否则返回false
     * @author     "sunli"
     * <p>Sample: 该方法使用样例</p>
     * date        2011-11-30
     * ---------------------------------------------------
     * 修改人                                          修改日期                                   修改描述
     * "sunli"        2011-11-30                               创建
     * ---------------------------------------------------
     * @Version  Ver1.0
     */
    public static boolean isEmpty(Collection<?> coll) {
        if (coll == null || coll.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 描述: 判断集合是否不为空.
     * @param coll 集合
     * @return 如果集合为空返回true，否则返回false
     * @author     "sunli"
     * <p>Sample: 该方法使用样例</p>
     * date        2011-11-30
     * ---------------------------------------------------
     * 修改人                                          修改日期                                   修改描述
     * "sunli"        2011-11-30          创建
     * ---------------------------------------------------
     * @Version  Ver1.0
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * 描述: 判断数组是否为空或者大小为0.
     * @param obj 数组
     * @return 如果数组为空或者数组大小为零返回true，否则返回false
     * @author     "sunli"
     * <p>Sample: 该方法使用样例</p>
     * date        2011-11-30
     * ---------------------------------------------------
     * 修改人                                          修改日期                                   修改描述
     * "sunli"        2011-11-30         创建
     * ---------------------------------------------------
     * @Version  Ver1.0
     */
    public static boolean isEmpty(Object[] obj) {
        if ((obj == null) || (obj.length == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 描述: 判断数组对象是否为空.
     * @param obj 数组对象
     * @return 如果数组对象为空返回true，否则返回false
     * @author     sunli
     * <p>Sample: 该方法使用样例</p>
     * date        2012-9-6
     * -----------------------------------------------------------
     * 修改人                                             修改日期                                   修改描述
     * sunli               2012-9-6              创建
     * -----------------------------------------------------------
     * @Version  Ver1.0
     */
    public static boolean isNotEmpty(Object[] obj) {
        return !isEmpty(obj);
    }

    /**
     * 描述: 如果输入值为空，抛出运行时异常.
     * @param str 输入值
     * @param msg 消息
     * @author     sunli
     * <p>Sample: 该方法使用样例</p>
     * date        2012-9-6
     * -----------------------------------------------------------
     * 修改人                                             修改日期                                   修改描述
     * sunli               2012-9-6              创建
     * -----------------------------------------------------------
     * @Version  Ver1.0
     */
    public static void isEmptyException(String str, String msg) {
        if (isBlank(str)) {
            throw new EmptyException(msg);
        }
    }
    /**
     * 描述: 如果对象为空抛出空值异常.
     * @param obj 对象
     * @param msg 消息
     * @author     sunli
     * <p>Sample: 该方法使用样例</p>
     * date        2012-9-6
     * -----------------------------------------------------------
     * 修改人                                             修改日期                                   修改描述
     * sunli               2012-9-6              创建
     * -----------------------------------------------------------
     * @Version  Ver1.0
     */
    public static void isEmptyException(Object obj, String msg) {
        if (obj == null) {
            throw new EmptyException(msg);
        }
    }
    
    public static boolean isObjEmpty(Object obj) {
    	if(obj == null) {
    		return false;
    	}
    	return true;
    }

}
