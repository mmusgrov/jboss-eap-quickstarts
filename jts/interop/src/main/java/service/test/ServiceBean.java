package service.test;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;

public class ServiceBean {
/*   //@Inject
   private ServiceEJB localService;
   private ServiceEJB remoteService;
   private boolean isWF;

   @PostConstruct
   public void init() {
      isWF = System.getProperty("jboss.node.name") != null;

      try {
         localService = (ServiceEJB) new InitialContext().lookup("java:app/ejbtest/ServiceEJB");
      } catch (NamingException ignore) {
         //throw new RuntimeException(e);
      }
   }

   public int getNext(boolean local) {
      return getServiceEJB(local).getNext();
   }

   private ServiceEJB getServiceEJB(boolean local) {
      if (local)
         return localService;

      if (remoteService == null) {
         try {
            Context ctx;
            String name;

            if (local) {
               name = "java:app/ejbtest/ServiceEJB";
               ctx = new InitialContext();
            } else {
               Properties env = new Properties();
               String purl;

               System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");

               name = "java:app/ejbtest/ServiceEJB";
               name = "ejbtest/ServiceEJB";

               if (isWF) {
                  purl = "corbaname:iiop:192.168.0.5:7001/NameService";
                  purl = "corbaloc::localhost:7001/NameService";

                  env.put(Context.PROVIDER_URL, purl);
                  env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");
               } else {
                  purl = "corbaloc::localhost:3528/NameService";

                  env.put(Context.PROVIDER_URL, purl);
                  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
               }

               ctx =  new InitialContext(env);
            }

            remoteService = lookupEjb(ctx, name);
         } catch (NamingException e) {
            throw new RuntimeException(e);
         }
      }

      return remoteService;
   }

   private ServiceEJB lookupEjb(Context ctx, String jndiName) {
      String[] names = new String[] {
              "java:global/ejbtest/ServiceEJB!service.ServiceEJB",
              "java:app/ejbtest/ServiceEJB!service.ServiceEJB",
              "java:module/ServiceEJB!service.ServiceEJB",
              "java:global/ejbtest/ServiceEJB",
              "java:app/ejbtest/ServiceEJB",
              "java:module/ServiceEJB",
      };

      for (String name : names) {
         try {
            Object obj = ctx.lookup(name);

            System.out.printf("lookup %s ok%n", name);

            ServiceEJBHome home = (ServiceEJBHome) PortableRemoteObject.narrow(obj, ServiceEJBHome.class);

            return home.create();//(ServiceEJB) obj;
         } catch (Exception ignore) {
         }
      }

      System.out.printf("nothing worked%n");
      return null;
   }*/
}
