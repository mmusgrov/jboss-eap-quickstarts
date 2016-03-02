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

import java.util.Enumeration;
import java.util.Hashtable;
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
        return getNext(local, 0, null);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local, int jndiPort, String failureType) {
        try {
            TxnHelper.addResources(isWF);

            return getServiceEJB(local, jndiPort).getNext(failureType);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private ISession getServiceEJB(boolean local, int jndiPort) {
        try {
            // "java:global/[<application-name>]/<module-name>/<bean-name>!<fully-qualified-bean-interface-name>"
            String name = "java:global/ejbtest/SessionBean!service.remote.ISessionHome";
            Context ctx;

            if (local) {
                ctx = new InitialContext();
            } else {
                Properties env = new Properties();
                String providerUrl = "corbaloc::localhost:%d/NameService";

                if (isWF) {
                    if (jndiPort <= 0)
                        jndiPort = 7001;

                    System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");

                    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");
                    env.put(Context.PROVIDER_URL, String.format(providerUrl, jndiPort));
                } else {
                    if (jndiPort <= 0)
                        jndiPort = 3528;


                    // using the standard lookup name from GF gets an error
                    name = "java:global/ejbtest/SessionBean";
                    boolean gfgf = true;
                    if (gfgf) {

//        <jndi-name>corbaname:iiop:adc6140215.us.oracle.com:3700#java:global/service-ejb/ServiceBean</jndi-name>

                        jndiPort = 3700;
                        name = "corbaname:iiop:localhost:3700#java:global/ejbtest/SessionBean";
                        //name = "java:global/ejbtest/SessionBean";
                        providerUrl = "corbaloc::localhost:%d/NameService";

                        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.impl.SerialInitContextFactory");
                        env.put(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
                        env.put(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
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

            return home.create();
        } catch (Exception e) {
            System.out.printf("*** lookup or invocation failed: %s%n", e.getMessage());

            throw new RuntimeException(e);
        }
    }
}
