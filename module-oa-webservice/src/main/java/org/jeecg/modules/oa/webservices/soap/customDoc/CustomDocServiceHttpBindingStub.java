/**
 * CustomDocServiceHttpBindingStub.java
 * <p>
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.jeecg.modules.oa.webservices.soap.customDoc;

/**
 * @author weaver
 */
public class CustomDocServiceHttpBindingStub extends org.apache.axis.client.Stub
    implements org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType {
    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[3];
        _initOperationDesc1();
    }

    private final java.util.Vector cachedSerClasses = new java.util.Vector();
    private final java.util.Vector cachedSerQNames = new java.util.Vector();
    private final java.util.Vector cachedSerFactories = new java.util.Vector();
    private final java.util.Vector cachedDeserFactories = new java.util.Vector();

    public CustomDocServiceHttpBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public CustomDocServiceHttpBindingStub(java.net.URL endpointUrl, javax.xml.rpc.Service service)
        throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointUrl;
    }

    public CustomDocServiceHttpBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.1");
        java.lang.Class cls;
        javax.xml.namespace.QName qName;
        javax.xml.namespace.QName qName2;
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
        java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        qName = new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "ArrayOfInt");
        cachedSerQNames.add(qName);
        cls = int[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int");
        qName2 = new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "int");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "ArrayOfString");
        cachedSerQNames.add(qName);
        cls = String[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
        qName2 = new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "string");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "ArrayOfDocAttachment");
        cachedSerQNames.add(qName);
        cls = org.jeecg.modules.oa.webservices.soap.customDoc.DocAttachment[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocAttachment");
        qName2 = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocAttachment");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "ArrayOfDocCustomField");
        cachedSerQNames.add(qName);
        cls = org.jeecg.modules.oa.webservices.soap.customDoc.DocCustomField[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocCustomField");
        qName2 = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocCustomField");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocAttachment");
        cachedSerQNames.add(qName);
        cls = org.jeecg.modules.oa.webservices.soap.customDoc.DocAttachment.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocCustomField");
        cachedSerQNames.add(qName);
        cls = org.jeecg.modules.oa.webservices.soap.customDoc.DocCustomField.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocInfo");
        cachedSerQNames.add(qName);
        cls = org.jeecg.modules.oa.webservices.soap.customDoc.DocInfo.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDoc");
        param = new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        param.setNillable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://webservices.docs.weaver", "DocInfo"));
        oper.setReturnClass(org.jeecg.modules.oa.webservices.soap.customDoc.DocInfo.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "out"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("loginByLoginId");
        param = new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        param.setNillable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "out"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("loginByOaId");
        param = new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        param.setNillable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "out"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class)cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName = (javax.xml.namespace.QName)cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        } else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf =
                                (org.apache.axis.encoding.SerializerFactory)cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df =
                                (org.apache.axis.encoding.DeserializerFactory)cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        } catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    @Override public org.jeecg.modules.oa.webservices.soap.customDoc.DocInfo getDoc(int in0, String in1)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "getDoc"));

        setRequestHeaders(_call);
        setAttachments(_call);
        Object _resp = _call.invoke(new Object[] {new Integer(in0), in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        } else {
            extractAttachments(_call);
            try {
                return (DocInfo)_resp;
            } catch (Exception _exception) {
                return (DocInfo)org.apache.axis.utils.JavaUtils.convert(_resp, DocInfo.class);
            }
        }
    }

    @Override public String loginByLoginId(String in0) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
            new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "loginByLoginId"));

        setRequestHeaders(_call);
        setAttachments(_call);
        Object _resp = _call.invoke(new Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        } else {
            extractAttachments(_call);
            try {
                return (String)_resp;
            } catch (Exception _exception) {
                return (String)org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
            }
        }
    }

    @Override public String loginByOaId(int in0) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
            new javax.xml.namespace.QName("http://localhost/services/CustomDocService", "loginByOaId"));

        setRequestHeaders(_call);
        setAttachments(_call);
        Object _resp = _call.invoke(new Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        } else {
            extractAttachments(_call);
            try {
                return (String)_resp;
            } catch (Exception _exception) {
                return (String)org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
            }
        }
    }

}
