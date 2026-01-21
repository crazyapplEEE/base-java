/**
 * CustomDocServicePortType.java
 * <p>
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.customDoc;

public interface CustomDocServicePortType extends java.rmi.Remote {
    /**
     * 获取文档
     *
     * @param docId
     * @param session
     * @return
     * @throws java.rmi.RemoteException
     */
    DocInfo getDoc(int docId, String session) throws java.rmi.RemoteException;

    /**
     * 通过loginid登录
     *
     * @param loginId
     * @return
     * @throws java.rmi.RemoteException
     */
    String loginByLoginId(String loginId) throws java.rmi.RemoteException;

    /**
     * 通过oa id登录
     *
     * @param oaId
     * @return
     * @throws java.rmi.RemoteException
     */
    String loginByOaId(int oaId) throws java.rmi.RemoteException;
}
