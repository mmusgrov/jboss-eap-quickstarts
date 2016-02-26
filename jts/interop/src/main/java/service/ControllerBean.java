package service;

import service.remote.ISession;
import service.remote.ISessionHome;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;

import java.rmi.RemoteException;
import java.util.Properties;

@TransactionManagement(TransactionManagementType.CONTAINER)
public class ControllerBean {
    private boolean isWF;

    @PostConstruct
    public void init() {
        isWF = System.getProperty("jboss.node.name") != null;
    }

    public String getNext(boolean local) {
        try {
            return getServiceEJB(local).getNext();
        } catch (RemoteException e) {
            return e.getMessage();
        }
    }

    private ISession getServiceEJB(boolean local) {
        try {
            // "java:global/[<application-name>]/<module-name>/<bean-name>!<fully-qualified-bean-interface-name>"
            String name = "java:global/ejbtest/SessionBean!service.remote.ISessionHome";
            Context ctx;

            if (local) {
                ctx = new InitialContext();
            } else {
                Properties env = new Properties();

                System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");

                if (isWF) {
                    env.put(Context.PROVIDER_URL, "corbaloc::localhost:7001/NameService");
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");
                } else {
                    env.put(Context.PROVIDER_URL, "corbaloc::localhost:3528/NameService");
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");

                    // using the standard lookup name from GF gets an error
                    name = "java:global/ejbtest/SessionBean";
                }

                ctx =  new InitialContext(env);
            }

            Object obj = ctx.lookup(name);
            ISessionHome home = (ISessionHome) obj; // PortableRemoteObject.narrow(obj, ISessionHome.class);

            return home.create();
        } catch (Exception e) {
            System.out.printf("*** lookup or invocation failed: %s%n", e.getMessage());

            throw new RuntimeException(e);
        }
    }
}
