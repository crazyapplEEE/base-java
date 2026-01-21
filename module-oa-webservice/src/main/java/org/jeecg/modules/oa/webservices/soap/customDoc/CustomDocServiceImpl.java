/**
 * CustomDocServiceImpl.java
 * <p>
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.customDoc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service public class CustomDocServiceImpl extends org.apache.axis.client.Service
    implements org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocService {

    /**
     * The WSDD service name defaults to the port name.
     */
    private String CustomDocServiceHttpPortWSDDServiceName = "CustomDocServiceHttpPort";
    // Use to get a proxy class for CustomDocServiceHttpPort
    @Value("${oa-webservice.CustomDocServiceHttpPort_address}") private String CustomDocServiceHttpPort_address;
    private java.util.HashSet ports = null;

    public CustomDocServiceImpl() {
    }

    public CustomDocServiceImpl(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public CustomDocServiceImpl(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    @Override public String getCustomDocServiceHttpPortAddress() {
        return CustomDocServiceHttpPort_address;
    }

    public String getCustomDocServiceHttpPortWSDDServiceName() {
        return CustomDocServiceHttpPortWSDDServiceName;
    }

    public void setCustomDocServiceHttpPortWSDDServiceName(String name) {
        CustomDocServiceHttpPortWSDDServiceName = name;
    }

    @Override
    public org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType getCustomDocServiceHttpPort()
        throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(CustomDocServiceHttpPort_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getCustomDocServiceHttpPort(endpoint);
    }

    @Override
    public org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType getCustomDocServiceHttpPort(
        java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServiceHttpBindingStub _stub =
                new org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServiceHttpBindingStub(portAddress, this);
            _stub.setPortName(getCustomDocServiceHttpPortWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setCustomDocServiceHttpPortEndpointAddress(String address) {
        CustomDocServiceHttpPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType.class.isAssignableFrom(
                serviceEndpointInterface)) {
                org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServiceHttpBindingStub stub =
                    new org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServiceHttpBindingStub(
                        new java.net.URL(CustomDocServiceHttpPort_address), this);
                stub.setPortName(getCustomDocServiceHttpPortWSDDServiceName());
                return stub;
            }
        } catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException(
            "There is no stub implementation for the interface:  " + serviceEndpointInterface.getName());
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
        if ("CustomDocServiceHttpPort".equals(inputPortName)) {
            return getCustomDocServiceHttpPort();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub)_stub).setPortName(portName);
            return _stub;
        }
    }

    @Override public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "CustomDocService");
    }

    @Override public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://localhost/services/CustomDocService",
                "CustomDocServiceHttpPort"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {

        if ("CustomDocServiceHttpPort".equals(portName)) {
            setCustomDocServiceHttpPortEndpointAddress(address);
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
