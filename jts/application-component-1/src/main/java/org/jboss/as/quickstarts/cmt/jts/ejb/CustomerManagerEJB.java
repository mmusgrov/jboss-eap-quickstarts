/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.cmt.jts.ejb;

import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.cmt.model.Customer;

@Stateless
public class CustomerManagerEJB {
    private static String WL_JNDI_NAME = "jboss-jts-application-component-2jboss-jts-application-component-2_jarInvoiceManagerEJBImpl_EO";
    private static String WF_JNDI_NAME =  "jts-quickstart/InvoiceManagerEJBImpl";

    //    private static String WL_PROVIDER_URL = "corbaname:iiop:127.0.0.1:7001/NameService"; 192.168.0.5
    private static String WL_PROVIDER_URL = "corbaname:iiop:192.168.0.5:7001/NameService";

    private static String WF_CONTEXT_FACTORY = "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory";
    private static String WL_CONTEXT_FACTORY = "com.sun.jndi.cosnaming.CNCtxFactory";

    private boolean isJBoss;
    private String invoiceMgrJndiName;
    private int serverPort;
    private String jndiContextFactory;
    private String jndiProviderUrl;
    private Properties jndiProps;

    private List<Customer> customers;
    private List<String> errors;

    //@EJB(lookup = "corbaname:iiop:localhost:3628#jts-quickstart/InvoiceManagerEJBImpl")
    //private InvoiceManagerEJBHome invoiceManagerHome;

    //    @EJB(lookup = "corbaname:iiop:localhost:7001/NameService#jboss-jts-application-component-2jboss-jts-application-component-2_jarInvoiceManagerEJBImpl_EO")
    private InvoiceManagerEJB invoiceManagerEJB;

    @PostConstruct
    private void init() {
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        System.setProperty("com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl"); // a guess

        jndiProps = new Properties();
        customers = new ArrayList<>();
        errors = new ArrayList<>();
        isJBoss = System.getProperty("jboss.node.name") != null; // or jboss.server.name

        if (System.getProperty("jboss.node.name") == null) {
            isJBoss = false;
            invoiceMgrJndiName =  WF_JNDI_NAME;
            serverPort = 7001;
            jndiContextFactory = WL_CONTEXT_FACTORY;
            jndiProviderUrl = "corbaloc:iiop:127.0.0.1:3528"; // "corbaname:iiop:localhost:3628
        } else {
            isJBoss = true;
            invoiceMgrJndiName =  WL_JNDI_NAME;
            serverPort = 8080;
            jndiContextFactory = WF_CONTEXT_FACTORY;
            jndiProviderUrl = "corbaloc:iiop:127.0.0.1:7001";
        }

        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, jndiContextFactory);
        jndiProps.put(Context.PROVIDER_URL, jndiProviderUrl);
    }

    private Context getRemoteJndiContext() throws NamingException {
        return new InitialContext(jndiProps);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String createCustomer(String name) throws RemoteException, JMSException, CreateException {
        Customer c1 = new Customer();
        c1.setName(name);
        customers.add(c1);

        try {
            errors.add("getInvoiceManager");
            InvoiceManagerEJB im = getInvoiceManager();
            errors.add("calling getInvoiceManager");
            return im.createInvoiceInTxn(name);
        } catch (NamingException e) {
            errors.add(e.getMessage());
            throw new RemoteException(e.getMessage());
        }
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    @SuppressWarnings("unchecked")
    public List<String> listInvoices() throws RemoteException, CreateException {
        try {
            InvoiceManagerEJB im = getInvoiceManager();
            return im.listInvoices();
        } catch (NamingException e) {
            errors.add(e.getMessage());
            return errors;
        }
    }

    /**
     * List all the customers.
     */
    @TransactionAttribute(TransactionAttributeType.NEVER)
    @SuppressWarnings("unchecked")
    public List<Customer> listCustomers() {
        return customers;
    }

    private InvoiceManagerEJB getInvoiceManager() throws NamingException, RemoteException, CreateException {
        Context context;
        Object oRef;

        try {
            context = getRemoteJndiContext();
            errors.add("looking up " + invoiceMgrJndiName);
            oRef = context.lookup(invoiceMgrJndiName);
            errors.add("lookup ok");
        } catch (Exception e) {
            errors.add(e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return (InvoiceManagerEJB) oRef;
    }

    private Context oldGetRemoteJndiContext() throws NamingException {
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        System.setProperty("com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl"); // a guess


        Properties jndiProps = new Properties();

        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, jndiContextFactory);

        String[] purls = {
                "corbaname:iiop:localhost:7001",
                "corbaname:iiop:localhost:7001/NameService",
                "corbaname:iiop:localhost:7001/NameServiceServerRoot",
        };

        jndiProps.put(Context.PROVIDER_URL, purls[1]);

        System.out.printf("creating context ..%n");

//        jndiProps.put(Context.URL_PKG_PREFIXES, "org.wildfly.iiop.openjdk.naming.jndi");
        jndiProps.put(Context.PROVIDER_URL, "corbaloc:iiop:127.0.0.1:7001");

//        jndiProps.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
//        jndiProps.put("org.omg.CORBA.ORBInitialPort", "7001");
//        jndiProps.put(Context.PROVIDER_URL, WL_PROVIDER_URL);


        return new InitialContext(jndiProps);
    }

    private Context getCLContext() throws NamingException {
        Properties jndiProps = new Properties();

//        jndiProps.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
//        jndiProps.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
//        jndiProps.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

        jndiProps.put(Context.URL_PKG_PREFIXES, "org.wildfly.iiop.openjdk.naming.jndi");
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");
//        jndiProps.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
//        jndiProps.put("org.omg.CORBA.ORBInitialPort", "7001");
//        jndiProps.put(Context.PROVIDER_URL, WL_PROVIDER_URL);

        jndiProps.put(Context.PROVIDER_URL, "corbaloc:iiop:127.0.0.1:7001");

        return new InitialContext(jndiProps);
    }

    private Context getCLContext2() throws NamingException {
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
//        System.setProperty("com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl"); // a guess

        Properties jndiProps = new Properties();

//        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory"); // use the one in rt.jar for standalone usage

        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory"); // for using in wildfly
//        jndiProps.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.iiop.naming:org.jboss.naming.client");

        jndiProps.put(Context.PROVIDER_URL, WL_PROVIDER_URL);

        return new InitialContext(jndiProps);
    }
}
