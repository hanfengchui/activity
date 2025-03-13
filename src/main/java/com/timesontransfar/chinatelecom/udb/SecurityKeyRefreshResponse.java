/**
 * SsSecurityKeyNotifyResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.timesontransfar.chinatelecom.udb;


@SuppressWarnings("serial")
public class SecurityKeyRefreshResponse  implements java.io.Serializable {

	    private int resultCode;

	    private java.lang.String description;

	    public SecurityKeyRefreshResponse() {
	    }

	    public SecurityKeyRefreshResponse(
	           int resultCode,
	           java.lang.String description) {
	           this.resultCode = resultCode;
	           this.description = description;
	    }


	    /**
	     * Gets the resultCode value for this SsSecurityKeyNotifyResponse.
	     * 
	     * @return resultCode
	     */
	    public int getResultCode() {
	        return resultCode;
	    }


	    /**
	     * Sets the resultCode value for this SsSecurityKeyNotifyResponse.
	     * 
	     * @param resultCode
	     */
	    public void setResultCode(int resultCode) {
	        this.resultCode = resultCode;
	    }


	    /**
	     * Gets the description value for this SsSecurityKeyNotifyResponse.
	     * 
	     * @return description
	     */
	    public java.lang.String getDescription() {
	        return description;
	    }


	    /**
	     * Sets the description value for this SsSecurityKeyNotifyResponse.
	     * 
	     * @param description
	     */
	    public void setDescription(java.lang.String description) {
	        this.description = description;
	    }

	    // Type metadata
	    private static org.apache.axis.description.TypeDesc typeDesc =
	        new org.apache.axis.description.TypeDesc(SecurityKeyRefreshResponse.class, true);

	    static {
	        typeDesc.setXmlType(new javax.xml.namespace.QName("http://udb.chinatelecom.com", ">SsSecurityKeyNotifyResponse"));
	        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
	        elemField.setFieldName("resultCode");
	        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "ResultCode"));
	        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
	        elemField.setNillable(false);
	        typeDesc.addFieldDesc(elemField);
	        elemField = new org.apache.axis.description.ElementDesc();
	        elemField.setFieldName("description");
	        elemField.setXmlName(new javax.xml.namespace.QName("http://udb.chinatelecom.com", "Description"));
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
