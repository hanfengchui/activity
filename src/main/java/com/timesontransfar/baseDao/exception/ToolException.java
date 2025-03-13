/**
 * @Title: ToolException.java.
 * @Package com.transfar.coresystem.exception
 * Copyright: Copyright (c) 2012-10-4
 * Company:湖南创发科技有限责任公司
 * @author Comsys-sunli
 * @date 2012-10-4 下午9:14:25
 * @version V1.0
 */
package com.timesontransfar.baseDao.exception;

/**
 * <p>Class Name: ToolException.</p>
 * <p>Description: 类功能说明:工具类异常</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: sunli</p>
 * <p>Date: 2012-10-4</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class ToolException extends BaseRuntimeException {
    /**
    * @Fields serialVersionUID :流水号.
    */
    private static final long serialVersionUID = 3194516052995874267L;

    /**
     * 创建一个新的实例 ToolException.
     * <p>Title:默认构造函数 </p>
     * <p>Description:默认构造函数 </p>
     */
    public ToolException() {
        super();
    }

    /**
     * 创建一个新的实例 ToolException.
     * <p>Title: </p>
     * <p>Description: </p>
     * @param message 消息
     * @param cause 异常
     */
    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个新的实例 ToolException.
     * <p>Title: </p>
     * <p>Description: </p>
     * @param message 消息
     */
    public ToolException(String message) {
        super(message);
    }

    /**
     * 创建一个新的实例 ToolException.
     * <p>Title: </p>
     * <p>Description: </p>
     * @param cause 异常
     */
    public ToolException(Throwable cause) {
        super(cause);
    }
}
