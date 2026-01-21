/**
 * WorkflowServicePortType.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

/**
 * @author weaver
 */
public interface WorkflowServicePortType extends java.rmi.Remote {
    int convertLoginidToUserid(String in0) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo[] getAllWorkflowRequestList(int in0, int in1,
        int in2, int in3, String[] in4) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo getWorkflowRequest(int in0, int in1, int in2)
        throws java.rmi.RemoteException;

    int convertWorkcodeToUserid(String in0) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo[] getHendledWorkflowRequestList(int in0, int in1,
        int in2, int in3, String[] in4) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo[] getToDoWorkflowRequestList(int in0, int in1,
        int in2, int in3, String[] in4) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo getWorkflowRequest4Split(int in0, int in1,
        int in2, int in3) throws java.rmi.RemoteException;

    String submitWorkflowRequest(org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo in0, int in1,
        int in2, String in3, String in4) throws java.rmi.RemoteException;

    int getHendledWorkflowRequestCount(int in0, String[] in1) throws java.rmi.RemoteException;

    String getLeaveDays(String in0, String in1, String in2, String in3, String in4) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowBaseInfo[] getCreateWorkflowList(int in0, int in1, int in2,
        int in3, int in4, String[] in5) throws java.rmi.RemoteException;

    int getCreateWorkflowCount(int in0, int in1, String[] in2) throws java.rmi.RemoteException;

    int getProcessedWorkflowRequestCount(int in0, String[] in1) throws java.rmi.RemoteException;

    String forwardWorkflowRequest(int in0, String in1, String in2, int in3, String in4) throws java.rmi.RemoteException;

    int convertfbid(String in0) throws java.rmi.RemoteException;

    String doCreateWorkflowRequest(org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo in0, int in1)
        throws java.rmi.RemoteException;

    int getCCWorkflowRequestCount(int in0, String[] in1) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo[] getProcessedWorkflowRequestList(int in0,
        int in1, int in2, int in3, String[] in4) throws java.rmi.RemoteException;

    int getAllWorkflowRequestCount(int in0, String[] in1) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo getCreateWorkflowRequestInfo(int in0, int in1)
        throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo[] getMyWorkflowRequestList(int in0, int in1,
        int in2, int in3, String[] in4) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowBaseInfo[] getCreateWorkflowTypeList(int in0, int in1,
        int in2, int in3, String[] in4) throws java.rmi.RemoteException;

    int getMyWorkflowRequestCount(int in0, String[] in1) throws java.rmi.RemoteException;

    int convertbmid(String in0) throws java.rmi.RemoteException;

    String[] getWorkflowNewFlag(String[] in0, String in1) throws java.rmi.RemoteException;

    void writeWorkflowReadFlag(String in0, String in1) throws java.rmi.RemoteException;

    int getToDoWorkflowRequestCount(int in0, String[] in1) throws java.rmi.RemoteException;

    int getCreateWorkflowTypeCount(int in0, String[] in1) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestLog[] getWorkflowRequestLogs(String in0, String in1,
        int in2, int in3, int in4) throws java.rmi.RemoteException;

    boolean deleteRequest(int in0, int in1) throws java.rmi.RemoteException;

    org.jeecg.modules.oa.webservices.soap.workflow.WorkflowRequestInfo[] getCCWorkflowRequestList(int in0, int in1,
        int in2, int in3, String[] in4) throws java.rmi.RemoteException;
}
