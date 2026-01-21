/**
 * WorkflowRequestLog.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;

/**
 * @author weaver
 */
public class WorkflowRequestLog implements java.io.Serializable {
    /** Type medata */
    private static final org.apache.axis.description.TypeDesc TYPE_DESC =
        new org.apache.axis.description.TypeDesc(WorkflowRequestLog.class, true);

    static {
        TYPE_DESC.setXmlType(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "WorkflowRequestLog"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("agentor");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "agentor"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("agentorDept");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "agentorDept"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("annexDocHtmls");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "annexDocHtmls"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nodeId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "nodeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nodeName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "nodeName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operateDate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operateDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operateTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operateTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operateType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operateType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorDept");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operatorDept"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operatorId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operatorName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorSign");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "operatorSign"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receivedPersons");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "receivedPersons"));
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
        elemField.setFieldName("remarkSign");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "remarkSign"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signDocHtmls");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "signDocHtmls"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signWorkFlowHtmls");
        elemField.setXmlName(new javax.xml.namespace.QName("http://webservices.workflow.weaver", "signWorkFlowHtmls"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        TYPE_DESC.addFieldDesc(elemField);
    }

    /**
     * -- GETTER --
     *  Gets the agentor value for this WorkflowRequestLog.
     *
     * @return agentor
     */
    @Getter private String agentor;
    /**
     * -- GETTER --
     *  Gets the agentorDept value for this WorkflowRequestLog.
     *
     * @return agentorDept
     */
    @Getter private String agentorDept;
    /**
     * -- GETTER --
     *  Gets the annexDocHtmls value for this WorkflowRequestLog.
     *
     * @return annexDocHtmls
     */
    @Getter private String annexDocHtmls;
    /**
     * -- GETTER --
     *  Gets the id value for this WorkflowRequestLog.
     *
     * @return id
     */
    @Getter private String id;
    /**
     * -- GETTER --
     *  Gets the nodeId value for this WorkflowRequestLog.
     *
     * @return nodeId
     */
    @Getter private String nodeId;
    /**
     * -- GETTER --
     *  Gets the nodeName value for this WorkflowRequestLog.
     *
     * @return nodeName
     */
    @Getter private String nodeName;
    /**
     * -- GETTER --
     *  Gets the operateDate value for this WorkflowRequestLog.
     *
     * @return operateDate
     */
    @Getter private String operateDate;
    /**
     * -- GETTER --
     *  Gets the operateTime value for this WorkflowRequestLog.
     *
     * @return operateTime
     */
    @Getter private String operateTime;
    /**
     * -- GETTER --
     *  Gets the operateType value for this WorkflowRequestLog.
     *
     * @return operateType
     */
    @Getter private String operateType;
    /**
     * -- GETTER --
     *  Gets the operatorDept value for this WorkflowRequestLog.
     *
     * @return operatorDept
     */
    @Getter private String operatorDept;
    /**
     * -- GETTER --
     *  Gets the operatorId value for this WorkflowRequestLog.
     *
     * @return operatorId
     */
    @Getter private String operatorId;
    /**
     * -- GETTER --
     *  Gets the operatorName value for this WorkflowRequestLog.
     *
     * @return operatorName
     */
    @Getter private String operatorName;
    /**
     * -- GETTER --
     *  Gets the operatorSign value for this WorkflowRequestLog.
     *
     * @return operatorSign
     */
    @Getter private String operatorSign;
    /**
     * -- GETTER --
     *  Gets the receivedPersons value for this WorkflowRequestLog.
     *
     * @return receivedPersons
     */
    @Getter private String receivedPersons;
    /**
     * -- GETTER --
     *  Gets the remark value for this WorkflowRequestLog.
     *
     * @return remark
     */
    @Getter private String remark;
    /**
     * -- GETTER --
     *  Gets the remarkSign value for this WorkflowRequestLog.
     *
     * @return remarkSign
     */
    @Getter private String remarkSign;
    /**
     * -- GETTER --
     *  Gets the signDocHtmls value for this WorkflowRequestLog.
     *
     * @return signDocHtmls
     */
    @Getter private String signDocHtmls;
    /**
     * -- GETTER --
     *  Gets the signWorkFlowHtmls value for this WorkflowRequestLog.
     *
     * @return signWorkFlowHtmls
     */
    @Getter private String signWorkFlowHtmls;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowRequestLog() {
    }

    public WorkflowRequestLog(String agentor, String agentorDept, String annexDocHtmls, String id, String nodeId,
        String nodeName, String operateDate, String operateTime, String operateType, String operatorDept,
        String operatorId, String operatorName, String operatorSign, String receivedPersons, String remark,
        String remarkSign, String signDocHtmls, String signWorkFlowHtmls) {
        this.agentor = agentor;
        this.agentorDept = agentorDept;
        this.annexDocHtmls = annexDocHtmls;
        this.id = id;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.operateDate = operateDate;
        this.operateTime = operateTime;
        this.operateType = operateType;
        this.operatorDept = operatorDept;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.operatorSign = operatorSign;
        this.receivedPersons = receivedPersons;
        this.remark = remark;
        this.remarkSign = remarkSign;
        this.signDocHtmls = signDocHtmls;
        this.signWorkFlowHtmls = signWorkFlowHtmls;
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
     * Sets the agentor value for this WorkflowRequestLog.
     *
     * @param agentor
     */
    public void setAgentor(String agentor) {
        this.agentor = agentor;
    }

    /**
     * Sets the agentorDept value for this WorkflowRequestLog.
     *
     * @param agentorDept
     */
    public void setAgentorDept(String agentorDept) {
        this.agentorDept = agentorDept;
    }

    /**
     * Sets the annexDocHtmls value for this WorkflowRequestLog.
     *
     * @param annexDocHtmls
     */
    public void setAnnexDocHtmls(String annexDocHtmls) {
        this.annexDocHtmls = annexDocHtmls;
    }

    /**
     * Sets the id value for this WorkflowRequestLog.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the nodeId value for this WorkflowRequestLog.
     *
     * @param nodeId
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Sets the nodeName value for this WorkflowRequestLog.
     *
     * @param nodeName
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Sets the operateDate value for this WorkflowRequestLog.
     *
     * @param operateDate
     */
    public void setOperateDate(String operateDate) {
        this.operateDate = operateDate;
    }

    /**
     * Sets the operateTime value for this WorkflowRequestLog.
     *
     * @param operateTime
     */
    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    /**
     * Sets the operateType value for this WorkflowRequestLog.
     *
     * @param operateType
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * Sets the operatorDept value for this WorkflowRequestLog.
     *
     * @param operatorDept
     */
    public void setOperatorDept(String operatorDept) {
        this.operatorDept = operatorDept;
    }

    /**
     * Sets the operatorId value for this WorkflowRequestLog.
     *
     * @param operatorId
     */
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * Sets the operatorName value for this WorkflowRequestLog.
     *
     * @param operatorName
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * Sets the operatorSign value for this WorkflowRequestLog.
     *
     * @param operatorSign
     */
    public void setOperatorSign(String operatorSign) {
        this.operatorSign = operatorSign;
    }

    /**
     * Sets the receivedPersons value for this WorkflowRequestLog.
     *
     * @param receivedPersons
     */
    public void setReceivedPersons(String receivedPersons) {
        this.receivedPersons = receivedPersons;
    }

    /**
     * Sets the remark value for this WorkflowRequestLog.
     *
     * @param remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * Sets the remarkSign value for this WorkflowRequestLog.
     *
     * @param remarkSign
     */
    public void setRemarkSign(String remarkSign) {
        this.remarkSign = remarkSign;
    }

    /**
     * Sets the signDocHtmls value for this WorkflowRequestLog.
     *
     * @param signDocHtmls
     */
    public void setSignDocHtmls(String signDocHtmls) {
        this.signDocHtmls = signDocHtmls;
    }

    /**
     * Sets the signWorkFlowHtmls value for this WorkflowRequestLog.
     *
     * @param signWorkFlowHtmls
     */
    public void setSignWorkFlowHtmls(String signWorkFlowHtmls) {
        this.signWorkFlowHtmls = signWorkFlowHtmls;
    }

    @Override public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowRequestLog)) {
            return false;
        }
        WorkflowRequestLog other = (WorkflowRequestLog)obj;
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = ((this.agentor == null && other.getAgentor() == null) || (this.agentor != null && this.agentor.equals(
            other.getAgentor()))) && ((this.agentorDept == null && other.getAgentorDept() == null) || (this.agentorDept != null && this.agentorDept.equals(
            other.getAgentorDept()))) && ((this.annexDocHtmls == null && other.getAnnexDocHtmls() == null) || (this.annexDocHtmls != null && this.annexDocHtmls.equals(
            other.getAnnexDocHtmls()))) && ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(
            other.getId()))) && ((this.nodeId == null && other.getNodeId() == null) || (this.nodeId != null && this.nodeId.equals(
            other.getNodeId()))) && ((this.nodeName == null && other.getNodeName() == null) || (this.nodeName != null && this.nodeName.equals(
            other.getNodeName()))) && ((this.operateDate == null && other.getOperateDate() == null) || (this.operateDate != null && this.operateDate.equals(
            other.getOperateDate()))) && ((this.operateTime == null && other.getOperateTime() == null) || (this.operateTime != null && this.operateTime.equals(
            other.getOperateTime()))) && ((this.operateType == null && other.getOperateType() == null) || (this.operateType != null && this.operateType.equals(
            other.getOperateType()))) && ((this.operatorDept == null && other.getOperatorDept() == null) || (this.operatorDept != null && this.operatorDept.equals(
            other.getOperatorDept()))) && ((this.operatorId == null && other.getOperatorId() == null) || (this.operatorId != null && this.operatorId.equals(
            other.getOperatorId()))) && ((this.operatorName == null && other.getOperatorName() == null) || (this.operatorName != null && this.operatorName.equals(
            other.getOperatorName()))) && ((this.operatorSign == null && other.getOperatorSign() == null) || (this.operatorSign != null && this.operatorSign.equals(
            other.getOperatorSign()))) && ((this.receivedPersons == null && other.getReceivedPersons() == null) || (this.receivedPersons != null && this.receivedPersons.equals(
            other.getReceivedPersons()))) && ((this.remark == null && other.getRemark() == null) || (this.remark != null && this.remark.equals(
            other.getRemark()))) && ((this.remarkSign == null && other.getRemarkSign() == null) || (this.remarkSign != null && this.remarkSign.equals(
            other.getRemarkSign()))) && ((this.signDocHtmls == null && other.getSignDocHtmls() == null) || (this.signDocHtmls != null && this.signDocHtmls.equals(
            other.getSignDocHtmls()))) && ((this.signWorkFlowHtmls == null && other.getSignWorkFlowHtmls() == null) || (this.signWorkFlowHtmls != null && this.signWorkFlowHtmls.equals(
            other.getSignWorkFlowHtmls())));
        __equalsCalc = null;
        return _equals;
    }

    @Override public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getAgentor() != null) {
            _hashCode += getAgentor().hashCode();
        }
        if (getAgentorDept() != null) {
            _hashCode += getAgentorDept().hashCode();
        }
        if (getAnnexDocHtmls() != null) {
            _hashCode += getAnnexDocHtmls().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getNodeId() != null) {
            _hashCode += getNodeId().hashCode();
        }
        if (getNodeName() != null) {
            _hashCode += getNodeName().hashCode();
        }
        if (getOperateDate() != null) {
            _hashCode += getOperateDate().hashCode();
        }
        if (getOperateTime() != null) {
            _hashCode += getOperateTime().hashCode();
        }
        if (getOperateType() != null) {
            _hashCode += getOperateType().hashCode();
        }
        if (getOperatorDept() != null) {
            _hashCode += getOperatorDept().hashCode();
        }
        if (getOperatorId() != null) {
            _hashCode += getOperatorId().hashCode();
        }
        if (getOperatorName() != null) {
            _hashCode += getOperatorName().hashCode();
        }
        if (getOperatorSign() != null) {
            _hashCode += getOperatorSign().hashCode();
        }
        if (getReceivedPersons() != null) {
            _hashCode += getReceivedPersons().hashCode();
        }
        if (getRemark() != null) {
            _hashCode += getRemark().hashCode();
        }
        if (getRemarkSign() != null) {
            _hashCode += getRemarkSign().hashCode();
        }
        if (getSignDocHtmls() != null) {
            _hashCode += getSignDocHtmls().hashCode();
        }
        if (getSignWorkFlowHtmls() != null) {
            _hashCode += getSignWorkFlowHtmls().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
