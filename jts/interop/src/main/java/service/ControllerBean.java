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

    private ISession getServiceEJB(boolean local, String as, int jndiPort) {
        // "java:global/[<application-name>]/<module-name>/<bean-name>!<fully-qualified-bean-interface-name>"
        String name = "java:global/ejbtest/SessionBean!service.remote.ISessionHome";
        Properties env = new Properties();

        if (!local) {
            if (isWF) {
                // running on a WildFly server

                if (jndiPort <= 0)
                    jndiPort = 7001; // corba name service port for glassfish domain1

                env.put(Context.PROVIDER_URL, String.format("corbaloc::localhost:%d/NameService", jndiPort));
                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");
            } else {
                // running on a glassfish server
                String facClass; // set context factory depending upon whether we are looking up via glassfish or wildfly

                if ("gf".equals(as)) { // lookup an ejb running on a glassfish server

                    if (jndiPort <= 0)
                        jndiPort = 3700; // corba name service port for glassfish domain2

                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
                    env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
                    env.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(jndiPort));

                } else {  // lookup an ejb running on a wildfly server
                    name = "ejbtest/SessionBean";

                    if (jndiPort <= 0)
                        jndiPort = 3528; // default corba name service port for wildfly

                    env.put(Context.PROVIDER_URL, String.format("iiop://localhost:%d", jndiPort));
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
                }
            }

            try {
                System.out.printf("looking up %s via port %d%n", name, jndiPort);

                Object oRef = new InitialContext(env).lookup(name);

                System.out.printf("look up returned %s%n", oRef);

                ISessionHome home = (ISessionHome) oRef; //PortableRemoteObject.narrow(oRef, ISessionHome.class);

                return home.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
