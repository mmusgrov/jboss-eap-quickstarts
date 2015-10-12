package org.jboss.narayana;

import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBHome;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

public class HelloClient {
    private static String WL_JNDI_NAME = "jboss-jts-application-component-2jboss-jts-application-component-2_jarInvoiceManagerEJBImpl_EO";

    private List<String> errors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HelloClient client = new HelloClient();

        String name = "testear-wls-0.0.1-SNAPSHOT/testwar-wls-0.0.1-SNAPSHOT/MessageService!testejb.MessageServiceRemote";
        // or mappedName#qualified_name_of_businessInterface
        name = "MessageServiceRemote#testejb.MessageServiceRemote";
        name = "java:comp/env/ejb/MyEJBBean";
        name = "java:comp/env/jts-quickstart/InvoiceManagerEJBImpl";
        name = "corbaname:iiop:localhost:7001#jts-quickstart/InvoiceManagerEJBImpl";
        name = "InvoiceManagerEJB#org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJB";
        name = "jboss-jts-application-component-2jboss-jts-application-component-2_jarInvoiceManagerEJBImpl_EO";

        client.testInvoiceManager(WL_JNDI_NAME);
    }

    private void testInvoiceManager(String name) throws NamingException, RemoteException, CreateException {
        InvoiceManagerEJB ejb = getInvoiceManager(name);
//        String res = ejb.createInvoiceInTxn("i1");
        String res = ejb.createInvoice("i1");

        System.out.printf("createInvoice returned: %s%nInvoices:%n", res);

        for (String invoice : ejb.listInvoices())
            System.out.printf("\t%s%n", invoice);

        System.out.printf("Messages:%n");

        for (String msg : errors)
            System.err.printf("%s%n", msg);
    }

    private InvoiceManagerEJB getInvoiceManager(String name) throws NamingException, RemoteException, CreateException {
        Context context;
        Object oRef;

        try {
            context = getCLContext();
            errors.add("looking up " + name);
            oRef = context.lookup(name);
            errors.add("lookup ok");
        } catch (Exception e) {
            errors.add(e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return (InvoiceManagerEJB) oRef;
    }

    private Context getCLContext() throws NamingException {
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        System.setProperty("com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl"); // a guess


        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");

        //  env.put(Context.PROVIDER_URL, "corbaloc:iiop:myhost.mycompany.com:2809");
        // "corbaname:iiop:myhost.mycompany.com:9810/NameServiceServerRoot");
        String[] purls = {
                "corbaname:iiop:localhost:7001",
                "corbaname:iiop:localhost:7001/NameService",
                "corbaname:iiop:localhost:7001/NameServiceServerRoot",
        };

        jndiProps.put(Context.PROVIDER_URL, purls[1]);

        System.out.printf("creating context ..%n");

        return new InitialContext(jndiProps);
    }

    private Context getWLContext() throws NamingException {
        Properties jndiProps = new Properties();

        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        jndiProps.put(Context.PROVIDER_URL, "t3://localhost:7001");

        System.out.printf("creating context ..%n");

        return new InitialContext(jndiProps);
    }
}
