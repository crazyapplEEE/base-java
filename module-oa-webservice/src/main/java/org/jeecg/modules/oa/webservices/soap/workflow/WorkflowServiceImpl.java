/**
 * WorkflowServiceImpl.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.workflow;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author weaver
 */
@Service public class WorkflowServiceImpl extends org.apache.axis.client.Service
    implements org.jeecg.modules.oa.webservices.soap.workflow.WorkflowService {

    // Use to get a proxy class for WorkflowServiceHttpPort
    // test
    @Value("${oa-webservice.WorkflowServiceHttpPort_address}") private String WorkflowServiceHttpPort_address;
    // prod
    // private String WorkflowServiceHttpPort_address = "http://oa.bii.com.cn:89//services/WorkflowService";

    // The WSDD service name defaults to the port name.
    @Getter private String WorkflowServiceHttpPortWSDDServiceName = "WorkflowServiceHttpPort";
    private java.util.HashSet ports = null;

    public WorkflowServiceImpl() {
    }

    public WorkflowServiceImpl(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WorkflowServiceImpl(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    @Override public String getWorkflowServiceHttpPortAddress() {
        return WorkflowServiceHttpPort_address;
    }

    public void setWorkflowServiceHttpPortWSDDServiceName(String name) {
        WorkflowServiceHttpPortWSDDServiceName = name;
    }

    @Override public org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServicePortType getWorkflowServiceHttpPort()
        throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WorkflowServiceHttpPort_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWorkflowServiceHttpPort(endpoint);
    }

    @Override public org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServicePortType getWorkflowServiceHttpPort(
        java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServiceHttpBindingStub _stub =
                new org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServiceHttpBindingStub(portAddress, this);
            _stub.setPortName(getWorkflowServiceHttpPortWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWorkflowServiceHttpPortEndpointAddress(String address) {
        WorkflowServiceHttpPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServicePortType.class.isAssignableFrom(
                serviceEndpointInterface)) {
                org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServiceHttpBindingStub _stub =
                    new org.jeecg.modules.oa.webservices.soap.workflow.WorkflowServiceHttpBindingStub(
                        new java.net.URL(WorkflowServiceHttpPort_address), this);
                _stub.setPortName(getWorkflowServiceHttpPortWSDDServiceName());
                return _stub;
            }
        } catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException(
            "There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null"
                : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
        throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("WorkflowServiceHttpPort".equals(inputPortName)) {
            return getWorkflowServiceHttpPort();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub)_stub).setPortName(portName);
            return _stub;
        }
    }

    @Override public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "WorkflowService");
    }

    @Override public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("webservices.services.weaver.com.cn", "WorkflowServiceHttpPort"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {

        if ("WorkflowServiceHttpPort".equals(portName)) {
            setWorkflowServiceHttpPortEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address)
        throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
