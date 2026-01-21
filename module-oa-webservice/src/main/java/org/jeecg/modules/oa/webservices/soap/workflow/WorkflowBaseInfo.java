/**
 * WorkflowBaseInfo.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;

/**
 * @author weaver
 */
public class WorkflowBaseInfo implements java.io.Serializable {
    /**
     * Type medata
     */
    private static final org.apache.axis.description.TypeDesc TYPE_DESC =
        new org.apache.axis.description.TypeDesc(WorkflowBaseInfo.class, true);

    static {
        TYPE_DESC.setXmlType(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowBaseInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowTypeId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowTypeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowTypeName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowTypeName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
    }

    /**
     * -- GETTER --
     *  Gets the workflowId value for this WorkflowBaseInfo.
     *
     * @return workflowId
     */
    @Getter private String workflowId;
    /**
     * -- GETTER --
     *  Gets the workflowName value for this WorkflowBaseInfo.
     *
     * @return workflowName
     */
    @Getter private String workflowName;
    /**
     * -- GETTER --
     *  Gets the workflowTypeId value for this WorkflowBaseInfo.
     *
     * @return workflowTypeId
     */
    @Getter private String workflowTypeId;
    /**
     * -- GETTER --
     *  Gets the workflowTypeName value for this WorkflowBaseInfo.
     *
     * @return workflowTypeName
     */
    @Getter private String workflowTypeName;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowBaseInfo() {
    }

    public WorkflowBaseInfo(String workflowId, String workflowName, String workflowTypeId, String workflowTypeName) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.workflowTypeId = workflowTypeId;
        this.workflowTypeName = workflowTypeName;
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return TYPE_DESC;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(String mechType, java.lang.Class _javaType,
        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, TYPE_DESC);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(String mechType, java.lang.Class _javaType,
        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, TYPE_DESC);
    }

    /**
     * Sets the workflowId value for this WorkflowBaseInfo.
     *
     * @param workflowId
     */
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Sets the workflowName value for this WorkflowBaseInfo.
     *
     * @param workflowName
     */
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    /**
     * Sets the workflowTypeId value for this WorkflowBaseInfo.
     *
     * @param workflowTypeId
     */
    public void setWorkflowTypeId(String workflowTypeId) {
        this.workflowTypeId = workflowTypeId;
    }

    /**
     * Sets the workflowTypeName value for this WorkflowBaseInfo.
     *
     * @param workflowTypeName
     */
    public void setWorkflowTypeName(String workflowTypeName) {
        this.workflowTypeName = workflowTypeName;
    }

    @Override public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowBaseInfo)) {
            return false;
        }
        WorkflowBaseInfo other = (WorkflowBaseInfo)obj;
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals =
            ((this.workflowId == null && other.getWorkflowId() == null) || (this.workflowId != null && this.workflowId.equals(
                other.getWorkflowId()))) && ((this.workflowName == null && other.getWorkflowName() == null) || (this.workflowName != null && this.workflowName.equals(
                other.getWorkflowName()))) && ((this.workflowTypeId == null && other.getWorkflowTypeId() == null) || (this.workflowTypeId != null && this.workflowTypeId.equals(
                other.getWorkflowTypeId()))) && ((this.workflowTypeName == null && other.getWorkflowTypeName() == null) || (this.workflowTypeName != null && this.workflowTypeName.equals(
                other.getWorkflowTypeName())));
        __equalsCalc = null;
        return _equals;
    }

    @Override public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getWorkflowId() != null) {
            _hashCode += getWorkflowId().hashCode();
        }
        if (getWorkflowName() != null) {
            _hashCode += getWorkflowName().hashCode();
        }
        if (getWorkflowTypeId() != null) {
            _hashCode += getWorkflowTypeId().hashCode();
        }
        if (getWorkflowTypeName() != null) {
            _hashCode += getWorkflowTypeName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
