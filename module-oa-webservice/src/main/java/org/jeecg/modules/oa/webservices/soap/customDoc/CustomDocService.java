/**
 * CustomDocService.java
 * <p>
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.customDoc;

public interface CustomDocService extends javax.xml.rpc.Service {
    /**
     * 获取webservice地址
     *
     * @return
     */
    String getCustomDocServiceHttpPortAddress();

    /**
     * 获取webservice地址
     *
     * @return
     * @throws javax.xml.rpc.ServiceException
     */
    org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType getCustomDocServiceHttpPort()
        throws javax.xml.rpc.ServiceException;

    /**
     * 获取webservice地址
     *
     * @param portAddress
     * @return
     * @throws javax.xml.rpc.ServiceException
     */
    org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType getCustomDocServiceHttpPort(
        java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
