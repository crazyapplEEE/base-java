/**
 * WorkflowDetailTableInfo.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;

/**
 * @author weaver
 */
public class WorkflowDetailTableInfo implements java.io.Serializable {
    /** Type medata */
    private static final org.apache.axis.description.TypeDesc TYPE_DESC =
        new org.apache.axis.description.TypeDesc(WorkflowDetailTableInfo.class, true);

    static {
        TYPE_DESC.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowDetailTableInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tableDBName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "tableDBName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tableFieldName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "tableFieldName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "string"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tableTitle");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "tableTitle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowRequestTableRecords");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowRequestTableRecords"));
        elemField.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestTableRecord"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestTableRecord"));
        TYPE_DESC.addFieldDesc(elemField);
    }

    /**
     * -- GETTER --
     *  Gets the tableDBName value for this WorkflowDetailTableInfo.
     *
     * @return tableDBName
     */
    @Getter private String tableDBName;
    /**
     * -- GETTER --
     *  Gets the tableFieldName value for this WorkflowDetailTableInfo.
     *
     * @return tableFieldName
     */
    @Getter private String[] tableFieldName;
    /**
     * -- GETTER --
     *  Gets the tableTitle value for this WorkflowDetailTableInfo.
     *
     * @return tableTitle
     */
    @Getter private String tableTitle;
    /**
     * -- GETTER --
     *  Gets the workflowRequestTableRecords value for this WorkflowDetailTableInfo.
     *
     * @return workflowRequestTableRecords
     */
    @Getter private org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestTableRecord[] workflowRequestTableRecords;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowDetailTableInfo() {
    }

    public WorkflowDetailTableInfo(String tableDBName, String[] tableFieldName, String tableTitle,
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestTableRecord[] workflowRequestTableRecords) {
        this.tableDBName = tableDBName;
        this.tableFieldName = tableFieldName;
        this.tableTitle = tableTitle;
        this.workflowRequestTableRecords = workflowRequestTableRecords;
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
     * Sets the tableDBName value for this WorkflowDetailTableInfo.
     *
     * @param tableDBName
     */
    public void setTableDBName(String tableDBName) {
        this.tableDBName = tableDBName;
    }

    /**
     * Sets the tableFieldName value for this WorkflowDetailTableInfo.
     *
     * @param tableFieldName
     */
    public void setTableFieldName(String[] tableFieldName) {
        this.tableFieldName = tableFieldName;
    }

    /**
     * Sets the tableTitle value for this WorkflowDetailTableInfo.
     *
     * @param tableTitle
     */
    public void setTableTitle(String tableTitle) {
        this.tableTitle = tableTitle;
    }

    /**
     * Sets the workflowRequestTableRecords value for this WorkflowDetailTableInfo.
     *
     * @param workflowRequestTableRecords
     */
    public void setWorkflowRequestTableRecords(
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestTableRecord[] workflowRequestTableRecords) {
        this.workflowRequestTableRecords = workflowRequestTableRecords;
    }

    @Override public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowDetailTableInfo)) {
            return false;
        }
        WorkflowDetailTableInfo other = (WorkflowDetailTableInfo)obj;
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals =
            ((this.tableDBName == null && other.getTableDBName() == null) || (this.tableDBName != null && this.tableDBName.equals(
                other.getTableDBName()))) && ((this.tableFieldName == null && other.getTableFieldName() == null) || (this.tableFieldName != null && java.util.Arrays.equals(
                this.tableFieldName,
                other.getTableFieldName()))) && ((this.tableTitle == null && other.getTableTitle() == null) || (this.tableTitle != null && this.tableTitle.equals(
                other.getTableTitle()))) && ((this.workflowRequestTableRecords == null && other.getWorkflowRequestTableRecords() == null) || (this.workflowRequestTableRecords != null && java.util.Arrays.equals(
                this.workflowRequestTableRecords, other.getWorkflowRequestTableRecords())));
        __equalsCalc = null;
        return _equals;
    }

    @Override public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTableDBName() != null) {
            _hashCode += getTableDBName().hashCode();
        }
        if (getTableFieldName() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getTableFieldName()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTableFieldName(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTableTitle() != null) {
            _hashCode += getTableTitle().hashCode();
        }
        if (getWorkflowRequestTableRecords() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowRequestTableRecords()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowRequestTableRecords(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
