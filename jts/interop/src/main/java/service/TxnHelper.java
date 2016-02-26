package service;

import org.jboss.narayana.DummyXAResource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

public class TxnHelper {
    static private final String WL_TM = "weblogic.transaction.TransactionManager";
    static private final String EE_TM = "java:/TransactionManager";
    static private final String GF_TM = "java:appserver/TransactionManager";

    static void addResources(boolean isWF) throws NamingException, SystemException, RollbackException {
        TransactionManager tm;
        DummyXAResource resource = new DummyXAResource();

        if (isWF)
            tm = (TransactionManager) new InitialContext().lookup(EE_TM);
        else
            tm = (TransactionManager) new InitialContext().lookup(GF_TM);

        tm.getTransaction().enlistResource(resource);
    }
}
