/**
 * SsSecurityKeyNotifyRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.timesontransfar.chinatelecom.udb;

@SuppressWarnings("serial")
public class SecurityKeyRefreshRequest  implements java.io.Serializable {
    private java.lang.String srcDeviceNo;

    private int keyRefreshType;

    private int encryType;

    private java.lang.String updateKeyValue;

    private java.lang.String desInitialValue;

    private int updateMode;

    private java.lang.String timeStamp;

    private java.lang.String normalRefreshEffectMode;

    private java.lang.String effectTimeStamp;

    public SecurityKeyRefreshRequest() {
        //构造函数
    }

    /**
     * Gets the srcDeviceNo value for this SsSecurityKeyNotifyRequest.
     * 
     * @return srcDeviceNo
     */
    public java.lang.String getSrcDeviceNo() {
        return srcDeviceNo;
    }


    /**
     * Sets the srcDeviceNo value for this SsSecurityKeyNotifyRequest.
     * 
     * @param srcDeviceNo
     */
    public void setSrcDeviceNo(java.lang.String srcDeviceNo) {
        this.srcDeviceNo = srcDeviceNo;
    }


    /**
     * Gets the keyRefreshType value for this SsSecurityKeyNotifyRequest.
     * 
     * @return keyRefreshType
     */
    public int getKeyRefreshType() {
        return keyRefreshType;
    }


    /**
     * Sets the keyRefreshType value for this SsSecurityKeyNotifyRequest.
     * 
     * @param keyRefreshType
     */
    public void setKeyRefreshType(int keyRefreshType) {
        this.keyRefreshType = keyRefreshType;
    }


    /**
     * Gets the encryType value for this SsSecurityKeyNotifyRequest.
     * 
     * @return encryType
     */
    public int getEncryType() {
        return encryType;
    }


    /**
     * Sets the encryType value for this SsSecurityKeyNotifyRequest.
     * 
     * @param encryType
     */
    public void setEncryType(int encryType) {
        this.encryType = encryType;
    }


    /**
     * Gets the upadateKeyValue value for this SsSecurityKeyNotifyRequest.
     * 
     * @return upadateKeyValue
     */
    public java.lang.String getUpdateKeyValue() {
        return updateKeyValue;
    }


    /**
     * Sets the upadateKeyValue value for this SsSecurityKeyNotifyRequest.
     * 
     * @param upadateKeyValue
     */
    public void setUpdateKeyValue(java.lang.String updateKeyValue) {
        this.updateKeyValue = updateKeyValue;
    }

    public java.lang.String getDesInitialValue() {
		return desInitialValue;
	}

	public void setDesInitialValue(java.lang.String desInitialValue) {
		this.desInitialValue = desInitialValue;
	}

	/**
     * Gets the updateMode value for this SsSecurityKeyNotifyRequest.
     * 
     * @return updateMode
     */
    public int getUpdateMode() {
        return updateMode;
    }


    /**
     * Sets the updateMode value for this SsSecurityKeyNotifyRequest.
     * 
     * @param updateMode
     */
    public void setUpdateMode(int updateMode) {
        this.updateMode = updateMode;
    }


    /**
     * Gets the timeStamp value for this SsSecurityKeyNotifyRequest.
     * 
     * @return timeStamp
     */
    public java.lang.String getTimeStamp() {
        return timeStamp;
    }


    /**
     * Sets the timeStamp value for this SsSecurityKeyNotifyRequest.
     * 
     * @param timeStamp
     */
    public void setTimeStamp(java.lang.String timeStamp) {
        this.timeStamp = timeStamp;
    }


    /**
     * Gets the normalRefreshEffectMode value for this SsSecurityKeyNotifyRequest.
     * 
     * @return normalRefreshEffectMode
     */
    public java.lang.String getNormalRefreshEffectMode() {
        return normalRefreshEffectMode;
    }


    /**
     * Sets the normalRefreshEffectMode value for this SsSecurityKeyNotifyRequest.
     * 
     * @param normalRefreshEffectMode
     */
    public void setNormalRefreshEffectMode(java.lang.String normalRefreshEffectMode) {
        this.normalRefreshEffectMode = normalRefreshEffectMode;
    }


    /**
     * Gets the effectTimeStamp value for this SsSecurityKeyNotifyRequest.
     * 
     * @return effectTimeStamp
     */
    public java.lang.String getEffectTimeStamp() {
        return effectTimeStamp;
    }


    /**
     * Sets the effectTimeStamp value for this SsSecurityKeyNotifyRequest.
     * 
     * @param effectTimeStamp
     */
    public void setEffectTimeStamp(java.lang.String effectTimeStamp) {
        this.effectTimeStamp = effectTimeStamp;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SecurityKeyRefreshRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://udb.chinatelecom.com", ">SsSecurityKeyNotifyRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("srcDeviceNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "SrcDeviceNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("keyRefreshType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "KeyRefreshType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("encryType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "EncryType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateKeyValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "UpdateKeyValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("DESInitialValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "DESInitialValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateMode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "UpdateMode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeStamp");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "TimeStamp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("normalRefreshEffectMode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "NormalRefreshEffectMode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("effectTimeStamp");
        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "EffectTimeStamp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }
    
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
