/**
 * WorkflowRequestTableField.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;

/**
 * @author weaver
 */
public class WorkflowRequestTableField implements java.io.Serializable {
    /**
     * Type metadata
     */
    private static final org.apache.axis.description.TypeDesc TYPE_DESC =
        new org.apache.axis.description.TypeDesc(WorkflowRequestTableField.class, true);

    static {
        TYPE_DESC.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestTableField"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("browserurl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "browserurl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("edit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "edit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldDBType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldDBType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldFormName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldFormName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldHtmlType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldHtmlType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldOrder");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldOrder"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldShowName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldShowName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldShowValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldShowValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "fieldValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filedHtmlShow");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "filedHtmlShow"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mand");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "mand"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("selectnames");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "selectnames"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "string"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("selectvalues");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "selectvalues"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "string"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("view");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "view"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
    }

    /**
     * -- GETTER --
     *  Gets the browserurl value for this WorkflowRequestTableField.
     *
     * @return browserurl
     */
    @Getter private String browserurl;
    /**
     * -- GETTER --
     *  Gets the edit value for this WorkflowRequestTableField.
     *
     * @return edit
     */
    @Getter private java.lang.Boolean edit;
    /**
     * -- GETTER --
     *  Gets the fieldDBType value for this WorkflowRequestTableField.
     *
     * @return fieldDBType
     */
    @Getter private String fieldDBType;
    /**
     * -- GETTER --
     *  Gets the fieldFormName value for this WorkflowRequestTableField.
     *
     * @return fieldFormName
     */
    @Getter private String fieldFormName;
    /**
     * -- GETTER --
     *  Gets the fieldHtmlType value for this WorkflowRequestTableField.
     *
     * @return fieldHtmlType
     */
    @Getter private String fieldHtmlType;
    /**
     * -- GETTER --
     *  Gets the fieldId value for this WorkflowRequestTableField.
     *
     * @return fieldId
     */
    @Getter private String fieldId;
    /**
     * -- GETTER --
     *  Gets the fieldName value for this WorkflowRequestTableField.
     *
     * @return fieldName
     */
    @Getter private String fieldName;
    /**
     * -- GETTER --
     *  Gets the fieldOrder value for this WorkflowRequestTableField.
     *
     * @return fieldOrder
     */
    @Getter private Integer fieldOrder;
    /**
     * -- GETTER --
     *  Gets the fieldShowName value for this WorkflowRequestTableField.
     *
     * @return fieldShowName
     */
    @Getter private String fieldShowName;
    /**
     * -- GETTER --
     *  Gets the fieldShowValue value for this WorkflowRequestTableField.
     *
     * @return fieldShowValue
     */
    @Getter private String fieldShowValue;
    /**
     * -- GETTER --
     *  Gets the fieldType value for this WorkflowRequestTableField.
     *
     * @return fieldType
     */
    @Getter private String fieldType;
    /**
     * -- GETTER --
     *  Gets the fieldValue value for this WorkflowRequestTableField.
     *
     * @return fieldValue
     */
    @Getter private String fieldValue;
    /**
     * -- GETTER --
     *  Gets the filedHtmlShow value for this WorkflowRequestTableField.
     *
     * @return filedHtmlShow
     */
    @Getter private String filedHtmlShow;
    /**
     * -- GETTER --
     *  Gets the mand value for this WorkflowRequestTableField.
     *
     * @return mand
     */
    @Getter private java.lang.Boolean mand;
    /**
     * -- GETTER --
     *  Gets the selectnames value for this WorkflowRequestTableField.
     *
     * @return selectnames
     */
    @Getter private String[] selectnames;
    /**
     * -- GETTER --
     *  Gets the selectvalues value for this WorkflowRequestTableField.
     *
     * @return selectvalues
     */
    @Getter private String[] selectvalues;
    /**
     * -- GETTER --
     *  Gets the view value for this WorkflowRequestTableField.
     *
     * @return view
     */
    @Getter private java.lang.Boolean view;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowRequestTableField() {
    }

    public WorkflowRequestTableField(String browserurl, java.lang.Boolean edit, String fieldDBType,
        String fieldFormName, String fieldHtmlType, String fieldId, String fieldName, Integer fieldOrder,
        String fieldShowName, String fieldShowValue, String fieldType, String fieldValue, String filedHtmlShow,
        java.lang.Boolean mand, String[] selectnames, String[] selectvalues, java.lang.Boolean view) {
        this.browserurl = browserurl;
        this.edit = edit;
        this.fieldDBType = fieldDBType;
        this.fieldFormName = fieldFormName;
        this.fieldHtmlType = fieldHtmlType;
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.fieldOrder = fieldOrder;
        this.fieldShowName = fieldShowName;
        this.fieldShowValue = fieldShowValue;
        this.fieldType = fieldType;
        this.fieldValue = fieldValue;
        this.filedHtmlShow = filedHtmlShow;
        this.mand = mand;
        this.selectnames = selectnames;
        this.selectvalues = selectvalues;
        this.view = view;
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
     * Sets the browserurl value for this WorkflowRequestTableField.
     *
     * @param browserurl
     */
    public void setBrowserurl(String browserurl) {
        this.browserurl = browserurl;
    }

    /**
     * Sets the edit value for this WorkflowRequestTableField.
     *
     * @param edit
     */
    public void setEdit(java.lang.Boolean edit) {
        this.edit = edit;
    }

    /**
     * Sets the fieldDBType value for this WorkflowRequestTableField.
     *
     * @param fieldDBType
     */
    public void setFieldDBType(String fieldDBType) {
        this.fieldDBType = fieldDBType;
    }

    /**
     * Sets the fieldFormName value for this WorkflowRequestTableField.
     *
     * @param fieldFormName
     */
    public void setFieldFormName(String fieldFormName) {
        this.fieldFormName = fieldFormName;
    }

    /**
     * Sets the fieldHtmlType value for this WorkflowRequestTableField.
     *
     * @param fieldHtmlType
     */
    public void setFieldHtmlType(String fieldHtmlType) {
        this.fieldHtmlType = fieldHtmlType;
    }

    /**
     * Sets the fieldId value for this WorkflowRequestTableField.
     *
     * @param fieldId
     */
    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * Sets the fieldName value for this WorkflowRequestTableField.
     *
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Sets the fieldOrder value for this WorkflowRequestTableField.
     *
     * @param fieldOrder
     */
    public void setFieldOrder(Integer fieldOrder) {
        this.fieldOrder = fieldOrder;
    }

    /**
     * Sets the fieldShowName value for this WorkflowRequestTableField.
     *
     * @param fieldShowName
     */
    public void setFieldShowName(String fieldShowName) {
        this.fieldShowName = fieldShowName;
    }

    /**
     * Sets the fieldShowValue value for this WorkflowRequestTableField.
     *
     * @param fieldShowValue
     */
    public void setFieldShowValue(String fieldShowValue) {
        this.fieldShowValue = fieldShowValue;
    }

    /**
     * Sets the fieldType value for this WorkflowRequestTableField.
     *
     * @param fieldType
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Sets the fieldValue value for this WorkflowRequestTableField.
     *
     * @param fieldValue
     */
    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    /**
     * Sets the filedHtmlShow value for this WorkflowRequestTableField.
     *
     * @param filedHtmlShow
     */
    public void setFiledHtmlShow(String filedHtmlShow) {
        this.filedHtmlShow = filedHtmlShow;
    }

    /**
     * Sets the mand value for this WorkflowRequestTableField.
     *
     * @param mand
     */
    public void setMand(java.lang.Boolean mand) {
        this.mand = mand;
    }

    /**
     * Sets the selectnames value for this WorkflowRequestTableField.
     *
     * @param selectnames
     */
    public void setSelectnames(String[] selectnames) {
        this.selectnames = selectnames;
    }

    /**
     * Sets the selectvalues value for this WorkflowRequestTableField.
     *
     * @param selectvalues
     */
    public void setSelectvalues(String[] selectvalues) {
        this.selectvalues = selectvalues;
    }

    /**
     * Sets the view value for this WorkflowRequestTableField.
     *
     * @param view
     */
    public void setView(java.lang.Boolean view) {
        this.view = view;
    }

    @Override public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowRequestTableField)) {
            return false;
        }
        WorkflowRequestTableField other = (WorkflowRequestTableField)obj;
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals =
            ((this.browserurl == null && other.getBrowserurl() == null) || (this.browserurl != null && this.browserurl.equals(
                other.getBrowserurl()))) && ((this.edit == null && other.getEdit() == null) || (this.edit != null && this.edit.equals(
                other.getEdit()))) && ((this.fieldDBType == null && other.getFieldDBType() == null) || (this.fieldDBType != null && this.fieldDBType.equals(
                other.getFieldDBType()))) && ((this.fieldFormName == null && other.getFieldFormName() == null) || (this.fieldFormName != null && this.fieldFormName.equals(
                other.getFieldFormName()))) && ((this.fieldHtmlType == null && other.getFieldHtmlType() == null) || (this.fieldHtmlType != null && this.fieldHtmlType.equals(
                other.getFieldHtmlType()))) && ((this.fieldId == null && other.getFieldId() == null) || (this.fieldId != null && this.fieldId.equals(
                other.getFieldId()))) && ((this.fieldName == null && other.getFieldName() == null) || (this.fieldName != null && this.fieldName.equals(
                other.getFieldName()))) && ((this.fieldOrder == null && other.getFieldOrder() == null) || (this.fieldOrder != null && this.fieldOrder.equals(
                other.getFieldOrder()))) && ((this.fieldShowName == null && other.getFieldShowName() == null) || (this.fieldShowName != null && this.fieldShowName.equals(
                other.getFieldShowName()))) && ((this.fieldShowValue == null && other.getFieldShowValue() == null) || (this.fieldShowValue != null && this.fieldShowValue.equals(
                other.getFieldShowValue()))) && ((this.fieldType == null && other.getFieldType() == null) || (this.fieldType != null && this.fieldType.equals(
                other.getFieldType()))) && ((this.fieldValue == null && other.getFieldValue() == null) || (this.fieldValue != null && this.fieldValue.equals(
                other.getFieldValue()))) && ((this.filedHtmlShow == null && other.getFiledHtmlShow() == null) || (this.filedHtmlShow != null && this.filedHtmlShow.equals(
                other.getFiledHtmlShow()))) && ((this.mand == null && other.getMand() == null) || (this.mand != null && this.mand.equals(
                other.getMand()))) && ((this.selectnames == null && other.getSelectnames() == null) || (this.selectnames != null && java.util.Arrays.equals(
                this.selectnames,
                other.getSelectnames()))) && ((this.selectvalues == null && other.getSelectvalues() == null) || (this.selectvalues != null && java.util.Arrays.equals(
                this.selectvalues,
                other.getSelectvalues()))) && ((this.view == null && other.getView() == null) || (this.view != null && this.view.equals(
                other.getView())));
        __equalsCalc = null;
        return _equals;
    }

    @Override public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getBrowserurl() != null) {
            _hashCode += getBrowserurl().hashCode();
        }
        if (getEdit() != null) {
            _hashCode += getEdit().hashCode();
        }
        if (getFieldDBType() != null) {
            _hashCode += getFieldDBType().hashCode();
        }
        if (getFieldFormName() != null) {
            _hashCode += getFieldFormName().hashCode();
        }
        if (getFieldHtmlType() != null) {
            _hashCode += getFieldHtmlType().hashCode();
        }
        if (getFieldId() != null) {
            _hashCode += getFieldId().hashCode();
        }
        if (getFieldName() != null) {
            _hashCode += getFieldName().hashCode();
        }
        if (getFieldOrder() != null) {
            _hashCode += getFieldOrder().hashCode();
        }
        if (getFieldShowName() != null) {
            _hashCode += getFieldShowName().hashCode();
        }
        if (getFieldShowValue() != null) {
            _hashCode += getFieldShowValue().hashCode();
        }
        if (getFieldType() != null) {
            _hashCode += getFieldType().hashCode();
        }
        if (getFieldValue() != null) {
            _hashCode += getFieldValue().hashCode();
        }
        if (getFiledHtmlShow() != null) {
            _hashCode += getFiledHtmlShow().hashCode();
        }
        if (getMand() != null) {
            _hashCode += getMand().hashCode();
        }
        if (getSelectnames() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSelectnames()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSelectnames(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getSelectvalues() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSelectvalues()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSelectvalues(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getView() != null) {
            _hashCode += getView().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
