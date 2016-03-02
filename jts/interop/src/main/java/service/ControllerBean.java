package service;

import service.remote.ISession;
import service.remote.ISessionHome;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import java.rmi.RemoteException;
import java.util.Properties;

@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ControllerBean {

    private boolean isWF;

    @PostConstruct
    public void init() {
        isWF = System.getProperty("jboss.node.name") != null;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local) {
        return getNext(local, null, 0, null);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local, String as, int jndiPort, String failureType) {
        try {
            TxnHelper.addResources(isWF);

            if (as == null)
                as = "";

            return getServiceEJB(local, as, jndiPort).getNext(failureType);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private ISession getGFServiceEJB(int jndiPort, String jndiName) throws RemoteException, NamingException {
        Properties env = new Properties();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        env.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(jndiPort));

//        name = "corbaname:iiop:localhost:3700#java:global/ejbtest/SessionBean"; //"java:global/ejbtest/SessionBean"

        Object oRef = new InitialContext(env).lookup(jndiName);
        ISessionHome home = (ISessionHome) oRef; //PortableRemoteObject.narrow(oRef, ISessionHome.class);

        return home.create();
    }

    private ISession getWFServiceEJB(int jndiPort, String jndiName, String facClass) throws RemoteException, NamingException {
        Properties env = new Properties();

        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
        env.put(Context.INITIAL_CONTEXT_FACTORY, facClass);
        env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        env.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(jndiPort));

//        name = "corbaname:iiop:localhost:3700#java:global/ejbtest/SessionBean"; //"java:global/ejbtest/SessionBean"

        System.out.printf("looking up %s via port %d%n", jndiName, jndiPort);

        Object oRef = new InitialContext(env).lookup(jndiName);

        System.out.printf("look up returned %s%n", oRef);

        ISessionHome home = (ISessionHome) oRef; //PortableRemoteObject.narrow(oRef, ISessionHome.class);

        return home.create();
    }

    private ISession getServiceEJB(boolean local, String as, int jndiPort) {
        // "java:global/[<application-name>]/<module-name>/<bean-name>!<fully-qualified-bean-interface-name>"
        String name = "java:global/ejbtest/SessionBean!service.remote.ISessionHome";
        Context ctx;

        if (local) {
            try {
                ctx = new InitialContext();
                Object obj = ctx.lookup(name);
                ISessionHome home = (ISessionHome) obj; // PortableRemoteObject.narrow(obj, ISessionHome.class);

                return home.create();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            if (isWF) {
                String providerUrl = "corbaloc::localhost:%d/NameService";
                Properties env = new Properties();

                if (jndiPort <= 0)
                    jndiPort = 7001;

                System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");

                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");
                env.put(Context.PROVIDER_URL, String.format(providerUrl, jndiPort));

                try {
                    //return getWFServiceEJB(jndiPort, name, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");

                    Object obj = new InitialContext(env).lookup(name);
                    ISessionHome home = (ISessionHome) obj; // PortableRemoteObject.narrow(obj, ISessionHome.class);

                    return home.create();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            } else {
                if (jndiPort <= 0)
                    jndiPort = 3700;//3528;

                if ("gf".equals(as)) {
                    try {
                        return getGFServiceEJB(jndiPort, name);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else if ("wf".equals(as)) {
                    if (jndiPort <= 0)
                        jndiPort = 3528;

                    try {
                        return getWFServiceEJB(jndiPort, name, "com.sun.jndi.cosnaming.CNCtxFactory");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }

        return null;
    }

/*
                    if (gfgf) {

//        <jndi-name>corbaname:iiop:adc6140215.us.oracle.com:3700#java:global/service-ejb/ServiceBean</jndi-name>

                        jndiPort = 3700;
                        //name = "java:global/ejbtest/SessionBean";
                        providerUrl = "corbaloc::localhost:%d/NameService";

//                        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.impl.SerialInitContextFactory");
//                        env.put(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
//                        env.put(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

                        env.put(Context.PROVIDER_URL, String.format(providerUrl, 3700));
                        name = "corbaname:iiop:localhost:7001#java:global/ejbtest/SessionBean";

                        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
                        env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
                        env.setProperty("org.omg.CORBA.ORBInitialPort", "3700");



                        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
//                        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
//                        env.put(Context.PROVIDER_URL, String.format(providerUrl, 3700));
                        name = "java:global/ejbtest/SessionBean!service.remote.ISessionHome";
//                        name = "corbaname:iiop:localhost:3700#java:global/ejbtest/SessionBean";

                        ISessionHome home = (ISessionHome) PortableRemoteObject.narrow(new InitialContext(env).lookup(name), ISessionHome.class);

                        System.out.printf("GFIIIIIIIIIIIIIIIIISH: looked up object %s%n", home);
                        return home.create();
                    } else {
                        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
                        env.put(Context.PROVIDER_URL, String.format(providerUrl, jndiPort));
                    }
                }

                System.out.printf("Creating context to look up %s using url %s%n", name, env.get(Context.PROVIDER_URL));

                ctx =  new InitialContext(env);
            }

            System.out.printf("Looking up %s%n", name);
            Object obj = ctx.lookup(name);
            ISessionHome home = (ISessionHome) obj; // PortableRemoteObject.narrow(obj, ISessionHome.class);

            System.out.printf("GFIIIIIIIIIIIIIIIIISH: looked up object %s%n", obj);
            return home.create();
        } catch (Exception e) {
            System.out.printf("*** lookup or invocation failed: %s%n", e.getMessage());

            throw new RuntimeException(e);
        }
*/

}
