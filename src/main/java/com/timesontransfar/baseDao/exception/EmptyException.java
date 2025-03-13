/**
 * Copyright @ 2013 transfar
 * All right reserved
 */
package com.timesontransfar.baseDao.exception;



/**
 * <p>Class Name: EmptyException</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2013-12-12</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class EmptyException extends BaseRuntimeException {
    /** 
    * @Fields serialVersionUID :. 
    */
    private static final long serialVersionUID = -6909075549931611924L;

    /**
     *  创建一个新的实例 EmptyException.
     * <p>Title:带异常对象的构造函数 </p>
     * <p>Description: 带异常对象的构造函数</p>
     * @param e 异常对象
     */
    public EmptyException(Throwable e) {
        super(e);
    }

    /**
     * 创建一个新的实例 EmptyException.
     * <p>Title: 带消息以及异常对象的构造函数</p>
     * <p>Description: 带消息以及异常对象的构造函数</p>
     * @param message 消息
     * @param e 异常对象
     */
    public EmptyException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * 创建一个新的实例 EmptyException.
     * <p>Title:带消息的构造函数 </p>
     * <p>Description:带消息的构造函数 </p>
     * @param message 消息
     */
    public EmptyException(String message) {
        super(message);
    }
}
