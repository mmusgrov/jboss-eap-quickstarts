package service;

import service.remote.ISession;
import service.remote.ISessionHome;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.rmi.RemoteException;
import java.util.Properties;

@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ControllerBean {

    private boolean isWF;

    @PostConstruct
    public void init() {
        isWF = System.getProperty("jboss.node.name") != null;
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local) {
        return getNext(local, null, 0, null);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local, String as, int jndiPort, String failureType) {
        try {
            TxnHelper.addResources(isWF);

            return getServiceEJB(local, as, jndiPort).getNext(failureType);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private ISession xxxgetServiceEJB(int jndiPort, String jndiName, String facClass, String providerUrl) throws RemoteException, NamingException {
        Properties env = new Properties();

        if (providerUrl != null) {
            env.put(Context.PROVIDER_URL, providerUrl);
        }
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

    private ISession getServiceEJB(int jndiPort, String jndiName, String facClass, String providerUrl) throws RemoteException, NamingException {
        Properties env = new Properties();

        if (providerUrl != null)
            env.put(Context.PROVIDER_URL, providerUrl);

        env.put(Context.INITIAL_CONTEXT_FACTORY, facClass);
        //env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        //env.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(jndiPort));

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
            }
        } else {
            Properties env = new Properties();
            String providerUrl;

            if (isWF) {
                // running on a WildFly server
                providerUrl = "corbaloc::localhost:%d/NameService";

                if (jndiPort <= 0)
                    jndiPort = 7001; // corba name service port for glassfish domain1


                try {
                    return getServiceEJB(jndiPort, name, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory", String.format(providerUrl, jndiPort));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                // running on a glassfish server
                String facClass; // set context factory depending upon whether we are looking up via glassfish or wildfly

                if ("gf".equals(as)) { // lookup an ejb running on a glassfish server
                    facClass = "com.sun.enterprise.naming.SerialInitContextFactory";
                    providerUrl = null;

                    if (jndiPort <= 0)
                        jndiPort = 3700; // corba name service port for glassfish domain2
                } else {  // lookup an ejb running on a wildfly server
                    facClass = "com.sun.jndi.cosnaming.CNCtxFactory";
                    providerUrl = "iiop://localhost:3528";
                    name = "ejbtest/SessionBean";

                    if (jndiPort <= 0)
                        jndiPort = 3528; // default corba name service port for wildfly
                }

                try {
                    return getServiceEJB(jndiPort, name, facClass, providerUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

}
