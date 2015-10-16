package org.jboss.narayana;

import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBHome;
import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

public class HelloClient {
    private static String WL_JNDI_NAME = "jboss-jts-application-component-2jboss-jts-application-component-2_jarInvoiceManagerEJBImpl_EO";
    //    private static String WF_JNDI_NAME = "corbaname:iiop:localhost:8080#jts-quickstart/InvoiceManagerEJBImpl";
//    private static String WF_JNDI_NAME = "jts-quickstart/InvoiceManagerEJBImpl";
    private static String WF_JNDI_NAME = "InvoiceManagerEJBImpl";

    private static int WL_LOOKUP_PORT = 7001;
    private static int WF_LOOKUP_PORT = 3528; //8080;

    private static String JNDI_NAME;
    private static int LOOKUP_PORT;
    private static boolean useWL = !true;
    private static boolean useTxn = false;

    private List<String> errors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HelloClient client = new HelloClient();

        JNDI_NAME = useWL? WL_JNDI_NAME : WF_JNDI_NAME;
        LOOKUP_PORT = useWL?  WL_LOOKUP_PORT : WF_LOOKUP_PORT;

        client.testInvoiceManager(JNDI_NAME);
    }


    private void testIIOPNamingInvocation(String beanName) throws NamingException, RemoteException, CreateException {
        final Context context = getCLContext();
        final Object iiopObj = context.lookup(beanName);

        System.out.printf("looked up %s%n", iiopObj.toString());
        System.out.printf("type is %s%n", iiopObj.getClass().getCanonicalName());

        InvoiceManagerEJBHome imHome = (InvoiceManagerEJBHome) PortableRemoteObject.narrow(iiopObj, InvoiceManagerEJBHome.class);
        InvoiceManagerEJB ejb = imHome.create();

        String res = ejb.createInvoice("standalone client");

        System.out.printf("res: %s%n", res);

    }

    private void oktestIIOPNamingInvocation(String beanName) throws NamingException, RemoteException, CreateException {
        // this is important otherwise the PortableRemoteObject.narrow returns a null
        // make sure the CORBA stubs for the ejb proxy are available
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        System.setProperty("com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl"); // a guess

        final Properties prope = new Properties();
        prope.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        prope.put(Context.PROVIDER_URL, "corbaloc::localhost:3528/NameService");
        final InitialContext context = new InitialContext(prope);
        final Object iiopObj = context.lookup(beanName);

        System.out.printf("looked up %s%n", iiopObj.toString());
        System.out.printf("type is %s%n", iiopObj.getClass().getCanonicalName());

        InvoiceManagerEJBHome imHome = (InvoiceManagerEJBHome) PortableRemoteObject.narrow(iiopObj, InvoiceManagerEJBHome.class);
        InvoiceManagerEJB ejb = imHome.create();

        String res = ejb.createInvoice("standalone client");

        System.out.printf("res: %s%n", res);

    }

    private void testInvoiceManager(String name) throws Exception {
        InvoiceManagerEJB ejb = getInvoiceManager(name);
        String res = useTxn ? ejb.createInvoiceInTxn("i1") : ejb.createInvoice("i1");

        System.out.printf("createInvoice returned: %s%nInvoices:%n", res);

        for (String invoice : ejb.listInvoices())
            System.out.printf("\t%s%n", invoice);

        System.out.printf("Messages:%n");

        for (String msg : errors)
            System.err.printf("%s%n", msg);
    }

    private InvoiceManagerEJB getInvoiceManager(String name) throws Exception {
        Context context;
        Object oRef;

        try {
            context = getCLContext();
            oRef = context.lookup(name);
        } catch (Exception e) {
            errors.add(e.getMessage());
            e.printStackTrace();
            throw e;
        }

        if (useWL) {
            return (InvoiceManagerEJB) oRef;
        }

        InvoiceManagerEJBHome imHome = (InvoiceManagerEJBHome) PortableRemoteObject.narrow(oRef, InvoiceManagerEJBHome.class);
        return imHome.create();
    }

    private Context getCLContext() throws NamingException {
        // this is important otherwise the PortableRemoteObject.narrow returns a null
        // make sure the CORBA stubs for the ejb proxy are available
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
//        System.setProperty("com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl"); // a guess
        String purl;

        if (useWL)
            purl = "corbaname:iiop:localhost:" + LOOKUP_PORT + "/NameService";
        else
            purl = "corbaloc::localhost:" + LOOKUP_PORT + "/NameService";


        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        jndiProps.put(Context.PROVIDER_URL, purl);

        return new InitialContext(jndiProps); // this will create a connection to PROVIDER_URL
    }

    private Context getWLContext() throws NamingException {
        Properties jndiProps = new Properties();

        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        jndiProps.put(Context.PROVIDER_URL, "t3://localhost:7001");

        System.out.printf("creating context ..%n");

        return new InitialContext(jndiProps);
    }

    private Properties getNamingProperties(String host, int port) throws NamingException//, IOException
    {
        Properties properties = new Properties();
        String url = "corbaloc::HOST:PORT/NameService";

        url = url.replace("HOST", host).replace("PORT", Integer.toString(port));

        properties.setProperty(Context.PROVIDER_URL, url);

//        org.omg.CORBA.ORB norb = org.jboss.iiop.naming.ORBInitialContextFactory.getORB();
        // if norb is not null then we are running inside the AS so make sure that its root name context
        // is used in preferenance to the one defined by Context.PROVIDER_URL
//        if (norb != null)
//            properties.put("java.naming.corba.orb", norb);

        properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.iiop.naming:org.jboss.naming.client:org.jnp.interfaces");
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        properties.put(Context.OBJECT_FACTORIES, "org.jboss.tm.iiop.client.IIOPClientUserTransactionObjectFactory");

        return properties;
    }
}
