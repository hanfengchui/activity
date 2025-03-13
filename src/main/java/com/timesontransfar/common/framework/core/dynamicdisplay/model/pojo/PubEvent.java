package com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo;

/**
 *
 * 控件需要实现的相关事件
 * @author gobeyond
 *
 */
public class PubEvent  implements java.io.Serializable {

    // Fields

	private String ctrlId;
	private java.lang.String eventId;
	private java.lang.String eventType;
	private java.lang.String funId;
	private java.lang.String javaClass;
	private java.lang.String javaBean;
	private java.lang.String method;
	private java.lang.String javascript;
	private Integer displayType;
	private java.lang.String pageId;
	private Integer performType;


    // Constructors

    public Integer getPerformType() {
		return performType;
	}

	public void setPerformType(Integer performType) {
		this.performType = performType;
	}

	/** default constructor */
    public PubEvent() {
    }

    /** constructor with id */
    public PubEvent(java.lang.String eventId) {
        this.eventId = eventId;
    }




    // Property accessors
    /**
     * 取得事件的id
     */
    public java.lang.String getEventId () {
        return this.eventId;
    }
   /**
    * 设置事件的id id只是事件在后台的表示 web页面可以不用关注
    * @param EventId
    */
   public void setEventId (java.lang.String eventId) {
        this.eventId = eventId;
    }
    /**
     * 返回事件类型 比如onclick等
     * @return
     */
    public String getEventType () {
        return this.eventType;
    }
   /**
    * 设置事件类型 如onfocus等
    * @param EventType
    */
   public void setEventType (String eventType) {
        this.eventType = eventType;
    }
    /**
     * 事件对应控件所在功能的id
     * @return 功能id
     */
    public java.lang.String getFunId () {
        return this.funId;
    }
   /**
    * 设置事件对应控件所在的功能id
    * @param FunId 功能id
    */
   public void setFunId (java.lang.String funId) {
        this.funId = funId;
    }
    /**
     * 取得事件发生时要执行的后台程序的类名
     * @return 后台程序的类名
     */
    public java.lang.String getJavaClass () {
        return this.javaClass;
    }
   /**
    * 设置事件激发后要执行的后台程序类名
    * @param Javaclass 后台程序类名
    */
   public void setJavaClass (java.lang.String javaclass) {
        this.javaClass = javaclass;
    }
    /**
     * 后台执行的类在Spring中的beanname
     * @return
     */
    public java.lang.String getJavaBean () {
        return this.javaBean;
    }

   public void setJavaBean (java.lang.String javaBean) {
        this.javaBean = javaBean;
    }
    /**
     * 事件触发是要执行的后台类的具体方法
     * @return 方法名称
     */
    public java.lang.String getMethod () {
        return this.method;
    }
    /**
     * 事件触发是要执行的后台类的具体方法
     * @param 方法名称
     */
   public void setMethod (java.lang.String method) {
        this.method = method;
    }
    /**
     * 事件触发是要执行的javascript代码
     * @return
     */
    public java.lang.String getJavascript () {
        return this.javascript;
    }
   /**
    * 设置事件触发是要执行的javascript代码
    * @param Javascript javascript代码
    */
   public void setJavascript (java.lang.String javascript) {
        this.javascript = javascript;
    }
    /**
     * 所在显示页面的id
     * @return
     */
    public java.lang.String getPageId () {
        return this.pageId;
    }
   /**
    * 设置所在显示页面的id
    * @param PageId 页面id
    */
   public void setPageId (java.lang.String pageId) {
        this.pageId = pageId;
    }
    /**
     * 取得事件执行结果的展现类型 比如弹开页面
     * @return
     */
   public Integer getDisplayType() {
	   return displayType;
   }
  /**
   * 设置事件执行结果的展现类型
   * @param displayType
   */
   public void setDisplayType(Integer displayType) {
	   this.displayType = displayType;
   }
   /**
    * 事件所在的控件id
    * @return
    */
   public String getCtrlId() {
	  return ctrlId;
   }
   /**
    * 设置有哪个控件触发本事件
    * @param ctrlId 控件id
    */
   public void setCtrlId(String ctrlId) {
	  this.ctrlId = ctrlId;
   }

}