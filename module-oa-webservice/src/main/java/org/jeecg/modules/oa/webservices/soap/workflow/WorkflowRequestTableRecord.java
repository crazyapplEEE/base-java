/**
 * WorkflowRequestTableRecord.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;

/**
 * @author weaver
 */
public class WorkflowRequestTableRecord implements java.io.Serializable {
    /** Type medata */
    private static final org.apache.axis.description.TypeDesc TYPE_DESC =
        new org.apache.axis.description.TypeDesc(WorkflowRequestTableRecord.class, true);

    static {
        TYPE_DESC.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestTableRecord"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recordOrder");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "recordOrder"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowRequestTableFields");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowRequestTableFields"));
        elemField.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestTableField"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestTableField"));
        TYPE_DESC.addFieldDesc(elemField);
    }

    /**
     * -- GETTER --
     *  Gets the recordOrder value for this WorkflowRequestTableRecord.
     *
     * @return recordOrder
     */
    @Getter private Integer recordOrder;
    /**
     * -- GETTER --
     *  Gets the workflowRequestTableFields value for this WorkflowRequestTableRecord.
     *
     * @return workflowRequestTableFields
     */
    @Getter private org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestTableField[] workflowRequestTableFields;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowRequestTableRecord() {
    }

    public WorkflowRequestTableRecord(Integer recordOrder,
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestTableField[] workflowRequestTableFields) {
        this.recordOrder = recordOrder;
        this.workflowRequestTableFields = workflowRequestTableFields;
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
     * Sets the recordOrder value for this WorkflowRequestTableRecord.
     *
     * @param recordOrder
     */
    public void setRecordOrder(Integer recordOrder) {
        this.recordOrder = recordOrder;
    }

    /**
     * Sets the workflowRequestTableFields value for this WorkflowRequestTableRecord.
     *
     * @param workflowRequestTableFields
     */
    public void setWorkflowRequestTableFields(
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestTableField[] workflowRequestTableFields) {
        this.workflowRequestTableFields = workflowRequestTableFields;
    }

    @Override public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowRequestTableRecord)) {
            return false;
        }
        WorkflowRequestTableRecord other = (WorkflowRequestTableRecord)obj;
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals =
            ((this.recordOrder == null && other.getRecordOrder() == null) || (this.recordOrder != null && this.recordOrder.equals(
                other.getRecordOrder()))) && ((this.workflowRequestTableFields == null && other.getWorkflowRequestTableFields() == null) || (this.workflowRequestTableFields != null && java.util.Arrays.equals(
                this.workflowRequestTableFields, other.getWorkflowRequestTableFields())));
        __equalsCalc = null;
        return _equals;
    }

    @Override public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getRecordOrder() != null) {
            _hashCode += getRecordOrder().hashCode();
        }
        if (getWorkflowRequestTableFields() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowRequestTableFields()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowRequestTableFields(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
