/**
 * WorkflowRequestInfo.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;

/**
 * @author weaver
 */
public class WorkflowRequestInfo implements java.io.Serializable {
    /** Type medata */
    private static final org.apache.axis.description.TypeDesc TYPE_DESC =
        new org.apache.axis.description.TypeDesc(WorkflowRequestInfo.class, true);

    static {
        TYPE_DESC.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("canEdit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "canEdit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("canView");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "canView"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("createTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "createTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creatorId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "creatorId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creatorName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "creatorName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentNodeId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "currentNodeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentNodeName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "currentNodeName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("forwardButtonName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "forwardButtonName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isnextflow");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "isnextflow"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastOperateTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "lastOperateTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastOperatorName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "lastOperatorName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("messageType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "messageType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mustInputRemark");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "mustInputRemark"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("needAffirmance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "needAffirmance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receiveTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "receiveTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rejectButtonName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "rejectButtonName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remark");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "remark"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "requestId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestLevel");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "requestLevel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "requestName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subbackButtonName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "subbackButtonName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("submitButtonName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "submitButtonName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subnobackButtonName");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "subnobackButtonName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowBaseInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowBaseInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowBaseInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowDetailTableInfos");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowDetailTableInfos"));
        elemField.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowDetailTableInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowDetailTableInfo"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowHtmlShow");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowHtmlShow"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "string"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowHtmlTemplete");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowHtmlTemplete"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "string"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowMainTableInfo");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowMainTableInfo"));
        elemField.setXmlType(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowMainTableInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowPhrases");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowPhrases"));
        elemField.setXmlType(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "ArrayOfString"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "ArrayOfString"));
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowRequestLogs");
        elemField.setXmlName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "workflowRequestLogs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestLog"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(
            new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestLog"));
        TYPE_DESC.addFieldDesc(elemField);
    }

    /**
     * -- GETTER --
     *  Gets the canEdit value for this WorkflowRequestInfo.
     *
     * @return canEdit
     */
    @Getter private java.lang.Boolean canEdit;
    /**
     * -- GETTER --
     *  Gets the canView value for this WorkflowRequestInfo.
     *
     * @return canView
     */
    @Getter private java.lang.Boolean canView;
    /**
     * -- GETTER --
     *  Gets the createTime value for this WorkflowRequestInfo.
     *
     * @return createTime
     */
    @Getter private String createTime;
    /**
     * -- GETTER --
     *  Gets the creatorId value for this WorkflowRequestInfo.
     *
     * @return creatorId
     */
    @Getter private String creatorId;
    /**
     * -- GETTER --
     *  Gets the creatorName value for this WorkflowRequestInfo.
     *
     * @return creatorName
     */
    @Getter private String creatorName;
    /**
     * -- GETTER --
     *  Gets the currentNodeId value for this WorkflowRequestInfo.
     *
     * @return currentNodeId
     */
    @Getter private String currentNodeId;
    /**
     * -- GETTER --
     *  Gets the currentNodeName value for this WorkflowRequestInfo.
     *
     * @return currentNodeName
     */
    @Getter private String currentNodeName;
    /**
     * -- GETTER --
     *  Gets the forwardButtonName value for this WorkflowRequestInfo.
     *
     * @return forwardButtonName
     */
    @Getter private String forwardButtonName;
    /**
     * -- GETTER --
     *  Gets the isnextflow value for this WorkflowRequestInfo.
     *
     * @return isnextflow
     */
    @Getter private String isnextflow;
    /**
     * -- GETTER --
     *  Gets the lastOperateTime value for this WorkflowRequestInfo.
     *
     * @return lastOperateTime
     */
    @Getter private String lastOperateTime;
    /**
     * -- GETTER --
     *  Gets the lastOperatorName value for this WorkflowRequestInfo.
     *
     * @return lastOperatorName
     */
    @Getter private String lastOperatorName;
    /**
     * -- GETTER --
     *  Gets the messageType value for this WorkflowRequestInfo.
     *
     * @return messageType
     */
    @Getter private String messageType;
    /**
     * -- GETTER --
     *  Gets the mustInputRemark value for this WorkflowRequestInfo.
     *
     * @return mustInputRemark
     */
    @Getter private java.lang.Boolean mustInputRemark;
    /**
     * -- GETTER --
     *  Gets the needAffirmance value for this WorkflowRequestInfo.
     *
     * @return needAffirmance
     */
    @Getter private java.lang.Boolean needAffirmance;
    /**
     * -- GETTER --
     *  Gets the receiveTime value for this WorkflowRequestInfo.
     *
     * @return receiveTime
     */
    @Getter private String receiveTime;
    /**
     * -- GETTER --
     *  Gets the rejectButtonName value for this WorkflowRequestInfo.
     *
     * @return rejectButtonName
     */
    @Getter private String rejectButtonName;
    /**
     * -- GETTER --
     *  Gets the remark value for this WorkflowRequestInfo.
     *
     * @return remark
     */
    @Getter private String remark;
    /**
     * -- GETTER --
     *  Gets the requestId value for this WorkflowRequestInfo.
     *
     * @return requestId
     */
    @Getter private String requestId;
    /**
     * -- GETTER --
     *  Gets the requestLevel value for this WorkflowRequestInfo.
     *
     * @return requestLevel
     */
    @Getter private String requestLevel;
    /**
     * -- GETTER --
     *  Gets the requestName value for this WorkflowRequestInfo.
     *
     * @return requestName
     */
    @Getter private String requestName;
    /**
     * -- GETTER --
     *  Gets the status value for this WorkflowRequestInfo.
     *
     * @return status
     */
    @Getter private String status;
    /**
     * -- GETTER --
     *  Gets the subbackButtonName value for this WorkflowRequestInfo.
     *
     * @return subbackButtonName
     */
    @Getter private String subbackButtonName;
    /**
     * -- GETTER --
     *  Gets the submitButtonName value for this WorkflowRequestInfo.
     *
     * @return submitButtonName
     */
    @Getter private String submitButtonName;
    /**
     * -- GETTER --
     *  Gets the subnobackButtonName value for this WorkflowRequestInfo.
     *
     * @return subnobackButtonName
     */
    @Getter private String subnobackButtonName;
    /**
     * -- GETTER --
     *  Gets the workflowBaseInfo value for this WorkflowRequestInfo.
     *
     * @return workflowBaseInfo
     */
    @Getter private org.jeecg.modules.oa.webservices.soap.workflow.WorkflowBaseInfo workflowBaseInfo;
    /**
     * -- GETTER --
     *  Gets the workflowDetailTableInfos value for this WorkflowRequestInfo.
     *
     * @return workflowDetailTableInfos
     */
    @Getter private org.jeecg.modules.oa.webservices.soap.workflow.WorkflowDetailTableInfo[] workflowDetailTableInfos;
    /**
     * -- GETTER --
     *  Gets the workflowHtmlShow value for this WorkflowRequestInfo.
     *
     * @return workflowHtmlShow
     */
    @Getter private String[] workflowHtmlShow;
    /**
     * -- GETTER --
     *  Gets the workflowHtmlTemplete value for this WorkflowRequestInfo.
     *
     * @return workflowHtmlTemplete
     */
    @Getter private String[] workflowHtmlTemplete;
    /**
     * -- GETTER --
     *  Gets the workflowMainTableInfo value for this WorkflowRequestInfo.
     *
     * @return workflowMainTableInfo
     */
    @Getter private org.jeecg.modules.oa.webservices.soap.workflow.WorkflowMainTableInfo workflowMainTableInfo;
    /**
     * -- GETTER --
     *  Gets the workflowPhrases value for this WorkflowRequestInfo.
     *
     * @return workflowPhrases
     */
    @Getter private String[][] workflowPhrases;
    /**
     * -- GETTER --
     *  Gets the workflowRequestLogs value for this WorkflowRequestInfo.
     *
     * @return workflowRequestLogs
     */
    @Getter private org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestLog[] workflowRequestLogs;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowRequestInfo() {
    }

    public WorkflowRequestInfo(java.lang.Boolean canEdit, java.lang.Boolean canView, String createTime,
        String creatorId, String creatorName, String currentNodeId, String currentNodeName, String forwardButtonName,
        String isnextflow, String lastOperateTime, String lastOperatorName, String messageType,
        java.lang.Boolean mustInputRemark, java.lang.Boolean needAffirmance, String receiveTime,
        String rejectButtonName, String remark, String requestId, String requestLevel, String requestName,
        String status, String subbackButtonName, String submitButtonName, String subnobackButtonName,
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowBaseInfo workflowBaseInfo,
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowDetailTableInfo[] workflowDetailTableInfos,
        String[] workflowHtmlShow, String[] workflowHtmlTemplete,
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowMainTableInfo workflowMainTableInfo,
        String[][] workflowPhrases,
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestLog[] workflowRequestLogs) {
        this.canEdit = canEdit;
        this.canView = canView;
        this.createTime = createTime;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.currentNodeId = currentNodeId;
        this.currentNodeName = currentNodeName;
        this.forwardButtonName = forwardButtonName;
        this.isnextflow = isnextflow;
        this.lastOperateTime = lastOperateTime;
        this.lastOperatorName = lastOperatorName;
        this.messageType = messageType;
        this.mustInputRemark = mustInputRemark;
        this.needAffirmance = needAffirmance;
        this.receiveTime = receiveTime;
        this.rejectButtonName = rejectButtonName;
        this.remark = remark;
        this.requestId = requestId;
        this.requestLevel = requestLevel;
        this.requestName = requestName;
        this.status = status;
        this.subbackButtonName = subbackButtonName;
        this.submitButtonName = submitButtonName;
        this.subnobackButtonName = subnobackButtonName;
        this.workflowBaseInfo = workflowBaseInfo;
        this.workflowDetailTableInfos = workflowDetailTableInfos;
        this.workflowHtmlShow = workflowHtmlShow;
        this.workflowHtmlTemplete = workflowHtmlTemplete;
        this.workflowMainTableInfo = workflowMainTableInfo;
        this.workflowPhrases = workflowPhrases;
        this.workflowRequestLogs = workflowRequestLogs;
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
     * Sets the canEdit value for this WorkflowRequestInfo.
     *
     * @param canEdit
     */
    public void setCanEdit(java.lang.Boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * Sets the canView value for this WorkflowRequestInfo.
     *
     * @param canView
     */
    public void setCanView(java.lang.Boolean canView) {
        this.canView = canView;
    }

    /**
     * Sets the createTime value for this WorkflowRequestInfo.
     *
     * @param createTime
     */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * Sets the creatorId value for this WorkflowRequestInfo.
     *
     * @param creatorId
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Sets the creatorName value for this WorkflowRequestInfo.
     *
     * @param creatorName
     */
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    /**
     * Sets the currentNodeId value for this WorkflowRequestInfo.
     *
     * @param currentNodeId
     */
    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    /**
     * Sets the currentNodeName value for this WorkflowRequestInfo.
     *
     * @param currentNodeName
     */
    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    /**
     * Sets the forwardButtonName value for this WorkflowRequestInfo.
     *
     * @param forwardButtonName
     */
    public void setForwardButtonName(String forwardButtonName) {
        this.forwardButtonName = forwardButtonName;
    }

    /**
     * Sets the isnextflow value for this WorkflowRequestInfo.
     *
     * @param isnextflow
     */
    public void setIsnextflow(String isnextflow) {
        this.isnextflow = isnextflow;
    }

    /**
     * Sets the lastOperateTime value for this WorkflowRequestInfo.
     *
     * @param lastOperateTime
     */
    public void setLastOperateTime(String lastOperateTime) {
        this.lastOperateTime = lastOperateTime;
    }

    /**
     * Sets the lastOperatorName value for this WorkflowRequestInfo.
     *
     * @param lastOperatorName
     */
    public void setLastOperatorName(String lastOperatorName) {
        this.lastOperatorName = lastOperatorName;
    }

    /**
     * Sets the messageType value for this WorkflowRequestInfo.
     *
     * @param messageType
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Sets the mustInputRemark value for this WorkflowRequestInfo.
     *
     * @param mustInputRemark
     */
    public void setMustInputRemark(java.lang.Boolean mustInputRemark) {
        this.mustInputRemark = mustInputRemark;
    }

    /**
     * Sets the needAffirmance value for this WorkflowRequestInfo.
     *
     * @param needAffirmance
     */
    public void setNeedAffirmance(java.lang.Boolean needAffirmance) {
        this.needAffirmance = needAffirmance;
    }

    /**
     * Sets the receiveTime value for this WorkflowRequestInfo.
     *
     * @param receiveTime
     */
    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * Sets the rejectButtonName value for this WorkflowRequestInfo.
     *
     * @param rejectButtonName
     */
    public void setRejectButtonName(String rejectButtonName) {
        this.rejectButtonName = rejectButtonName;
    }

    /**
     * Sets the remark value for this WorkflowRequestInfo.
     *
     * @param remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * Sets the requestId value for this WorkflowRequestInfo.
     *
     * @param requestId
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Sets the requestLevel value for this WorkflowRequestInfo.
     *
     * @param requestLevel
     */
    public void setRequestLevel(String requestLevel) {
        this.requestLevel = requestLevel;
    }

    /**
     * Sets the requestName value for this WorkflowRequestInfo.
     *
     * @param requestName
     */
    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * Sets the status value for this WorkflowRequestInfo.
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the subbackButtonName value for this WorkflowRequestInfo.
     *
     * @param subbackButtonName
     */
    public void setSubbackButtonName(String subbackButtonName) {
        this.subbackButtonName = subbackButtonName;
    }

    /**
     * Sets the submitButtonName value for this WorkflowRequestInfo.
     *
     * @param submitButtonName
     */
    public void setSubmitButtonName(String submitButtonName) {
        this.submitButtonName = submitButtonName;
    }

    /**
     * Sets the subnobackButtonName value for this WorkflowRequestInfo.
     *
     * @param subnobackButtonName
     */
    public void setSubnobackButtonName(String subnobackButtonName) {
        this.subnobackButtonName = subnobackButtonName;
    }

    /**
     * Sets the workflowBaseInfo value for this WorkflowRequestInfo.
     *
     * @param workflowBaseInfo
     */
    public void setWorkflowBaseInfo(org.jeecg.modules.oa.webservices.soap.workflow.WorkflowBaseInfo workflowBaseInfo) {
        this.workflowBaseInfo = workflowBaseInfo;
    }

    /**
     * Sets the workflowDetailTableInfos value for this WorkflowRequestInfo.
     *
     * @param workflowDetailTableInfos
     */
    public void setWorkflowDetailTableInfos(
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowDetailTableInfo[] workflowDetailTableInfos) {
        this.workflowDetailTableInfos = workflowDetailTableInfos;
    }

    /**
     * Sets the workflowHtmlShow value for this WorkflowRequestInfo.
     *
     * @param workflowHtmlShow
     */
    public void setWorkflowHtmlShow(String[] workflowHtmlShow) {
        this.workflowHtmlShow = workflowHtmlShow;
    }

    /**
     * Sets the workflowHtmlTemplete value for this WorkflowRequestInfo.
     *
     * @param workflowHtmlTemplete
     */
    public void setWorkflowHtmlTemplete(String[] workflowHtmlTemplete) {
        this.workflowHtmlTemplete = workflowHtmlTemplete;
    }

    /**
     * Sets the workflowMainTableInfo value for this WorkflowRequestInfo.
     *
     * @param workflowMainTableInfo
     */
    public void setWorkflowMainTableInfo(
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowMainTableInfo workflowMainTableInfo) {
        this.workflowMainTableInfo = workflowMainTableInfo;
    }

    /**
     * Sets the workflowPhrases value for this WorkflowRequestInfo.
     *
     * @param workflowPhrases
     */
    public void setWorkflowPhrases(String[][] workflowPhrases) {
        this.workflowPhrases = workflowPhrases;
    }

    /**
     * Sets the workflowRequestLogs value for this WorkflowRequestInfo.
     *
     * @param workflowRequestLogs
     */
    public void setWorkflowRequestLogs(
        org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestLog[] workflowRequestLogs) {
        this.workflowRequestLogs = workflowRequestLogs;
    }

    @Override public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowRequestInfo)) {
            return false;
        }
        WorkflowRequestInfo other = (WorkflowRequestInfo)obj;
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = ((this.canEdit == null && other.getCanEdit() == null) || (this.canEdit != null && this.canEdit.equals(
            other.getCanEdit()))) && ((this.canView == null && other.getCanView() == null) || (this.canView != null && this.canView.equals(
            other.getCanView()))) && ((this.createTime == null && other.getCreateTime() == null) || (this.createTime != null && this.createTime.equals(
            other.getCreateTime()))) && ((this.creatorId == null && other.getCreatorId() == null) || (this.creatorId != null && this.creatorId.equals(
            other.getCreatorId()))) && ((this.creatorName == null && other.getCreatorName() == null) || (this.creatorName != null && this.creatorName.equals(
            other.getCreatorName()))) && ((this.currentNodeId == null && other.getCurrentNodeId() == null) || (this.currentNodeId != null && this.currentNodeId.equals(
            other.getCurrentNodeId()))) && ((this.currentNodeName == null && other.getCurrentNodeName() == null) || (this.currentNodeName != null && this.currentNodeName.equals(
            other.getCurrentNodeName()))) && ((this.forwardButtonName == null && other.getForwardButtonName() == null) || (this.forwardButtonName != null && this.forwardButtonName.equals(
            other.getForwardButtonName()))) && ((this.isnextflow == null && other.getIsnextflow() == null) || (this.isnextflow != null && this.isnextflow.equals(
            other.getIsnextflow()))) && ((this.lastOperateTime == null && other.getLastOperateTime() == null) || (this.lastOperateTime != null && this.lastOperateTime.equals(
            other.getLastOperateTime()))) && ((this.lastOperatorName == null && other.getLastOperatorName() == null) || (this.lastOperatorName != null && this.lastOperatorName.equals(
            other.getLastOperatorName()))) && ((this.messageType == null && other.getMessageType() == null) || (this.messageType != null && this.messageType.equals(
            other.getMessageType()))) && ((this.mustInputRemark == null && other.getMustInputRemark() == null) || (this.mustInputRemark != null && this.mustInputRemark.equals(
            other.getMustInputRemark()))) && ((this.needAffirmance == null && other.getNeedAffirmance() == null) || (this.needAffirmance != null && this.needAffirmance.equals(
            other.getNeedAffirmance()))) && ((this.receiveTime == null && other.getReceiveTime() == null) || (this.receiveTime != null && this.receiveTime.equals(
            other.getReceiveTime()))) && ((this.rejectButtonName == null && other.getRejectButtonName() == null) || (this.rejectButtonName != null && this.rejectButtonName.equals(
            other.getRejectButtonName()))) && ((this.remark == null && other.getRemark() == null) || (this.remark != null && this.remark.equals(
            other.getRemark()))) && ((this.requestId == null && other.getRequestId() == null) || (this.requestId != null && this.requestId.equals(
            other.getRequestId()))) && ((this.requestLevel == null && other.getRequestLevel() == null) || (this.requestLevel != null && this.requestLevel.equals(
            other.getRequestLevel()))) && ((this.requestName == null && other.getRequestName() == null) || (this.requestName != null && this.requestName.equals(
            other.getRequestName()))) && ((this.status == null && other.getStatus() == null) || (this.status != null && this.status.equals(
            other.getStatus()))) && ((this.subbackButtonName == null && other.getSubbackButtonName() == null) || (this.subbackButtonName != null && this.subbackButtonName.equals(
            other.getSubbackButtonName()))) && ((this.submitButtonName == null && other.getSubmitButtonName() == null) || (this.submitButtonName != null && this.submitButtonName.equals(
            other.getSubmitButtonName()))) && ((this.subnobackButtonName == null && other.getSubnobackButtonName() == null) || (this.subnobackButtonName != null && this.subnobackButtonName.equals(
            other.getSubnobackButtonName()))) && ((this.workflowBaseInfo == null && other.getWorkflowBaseInfo() == null) || (this.workflowBaseInfo != null && this.workflowBaseInfo.equals(
            other.getWorkflowBaseInfo()))) && ((this.workflowDetailTableInfos == null && other.getWorkflowDetailTableInfos() == null) || (this.workflowDetailTableInfos != null && java.util.Arrays.equals(
            this.workflowDetailTableInfos,
            other.getWorkflowDetailTableInfos()))) && ((this.workflowHtmlShow == null && other.getWorkflowHtmlShow() == null) || (this.workflowHtmlShow != null && java.util.Arrays.equals(
            this.workflowHtmlShow,
            other.getWorkflowHtmlShow()))) && ((this.workflowHtmlTemplete == null && other.getWorkflowHtmlTemplete() == null) || (this.workflowHtmlTemplete != null && java.util.Arrays.equals(
            this.workflowHtmlTemplete,
            other.getWorkflowHtmlTemplete()))) && ((this.workflowMainTableInfo == null && other.getWorkflowMainTableInfo() == null) || (this.workflowMainTableInfo != null && this.workflowMainTableInfo.equals(
            other.getWorkflowMainTableInfo()))) && ((this.workflowPhrases == null && other.getWorkflowPhrases() == null) || (this.workflowPhrases != null && java.util.Arrays.equals(
            this.workflowPhrases,
            other.getWorkflowPhrases()))) && ((this.workflowRequestLogs == null && other.getWorkflowRequestLogs() == null) || (this.workflowRequestLogs != null && java.util.Arrays.equals(
            this.workflowRequestLogs, other.getWorkflowRequestLogs())));
        __equalsCalc = null;
        return _equals;
    }

    @Override public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCanEdit() != null) {
            _hashCode += getCanEdit().hashCode();
        }
        if (getCanView() != null) {
            _hashCode += getCanView().hashCode();
        }
        if (getCreateTime() != null) {
            _hashCode += getCreateTime().hashCode();
        }
        if (getCreatorId() != null) {
            _hashCode += getCreatorId().hashCode();
        }
        if (getCreatorName() != null) {
            _hashCode += getCreatorName().hashCode();
        }
        if (getCurrentNodeId() != null) {
            _hashCode += getCurrentNodeId().hashCode();
        }
        if (getCurrentNodeName() != null) {
            _hashCode += getCurrentNodeName().hashCode();
        }
        if (getForwardButtonName() != null) {
            _hashCode += getForwardButtonName().hashCode();
        }
        if (getIsnextflow() != null) {
            _hashCode += getIsnextflow().hashCode();
        }
        if (getLastOperateTime() != null) {
            _hashCode += getLastOperateTime().hashCode();
        }
        if (getLastOperatorName() != null) {
            _hashCode += getLastOperatorName().hashCode();
        }
        if (getMessageType() != null) {
            _hashCode += getMessageType().hashCode();
        }
        if (getMustInputRemark() != null) {
            _hashCode += getMustInputRemark().hashCode();
        }
        if (getNeedAffirmance() != null) {
            _hashCode += getNeedAffirmance().hashCode();
        }
        if (getReceiveTime() != null) {
            _hashCode += getReceiveTime().hashCode();
        }
        if (getRejectButtonName() != null) {
            _hashCode += getRejectButtonName().hashCode();
        }
        if (getRemark() != null) {
            _hashCode += getRemark().hashCode();
        }
        if (getRequestId() != null) {
            _hashCode += getRequestId().hashCode();
        }
        if (getRequestLevel() != null) {
            _hashCode += getRequestLevel().hashCode();
        }
        if (getRequestName() != null) {
            _hashCode += getRequestName().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getSubbackButtonName() != null) {
            _hashCode += getSubbackButtonName().hashCode();
        }
        if (getSubmitButtonName() != null) {
            _hashCode += getSubmitButtonName().hashCode();
        }
        if (getSubnobackButtonName() != null) {
            _hashCode += getSubnobackButtonName().hashCode();
        }
        if (getWorkflowBaseInfo() != null) {
            _hashCode += getWorkflowBaseInfo().hashCode();
        }
        if (getWorkflowDetailTableInfos() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowDetailTableInfos()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowDetailTableInfos(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWorkflowHtmlShow() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowHtmlShow()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowHtmlShow(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWorkflowHtmlTemplete() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowHtmlTemplete()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowHtmlTemplete(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWorkflowMainTableInfo() != null) {
            _hashCode += getWorkflowMainTableInfo().hashCode();
        }
        if (getWorkflowPhrases() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowPhrases()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowPhrases(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWorkflowRequestLogs() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getWorkflowRequestLogs()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkflowRequestLogs(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
