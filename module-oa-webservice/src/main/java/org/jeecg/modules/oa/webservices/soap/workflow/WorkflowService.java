/**
 * WorkflowService.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

/**
 * @author weaver
 */
public interface WorkflowService extends javax.xml.rpc.Service {
    String getWorkflowServiceHttpPortAddress();

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServicePortType getWorkflowServiceHttpPort()
        throws javax.xml.rpc.ServiceException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServicePortType getWorkflowServiceHttpPort(
        java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
