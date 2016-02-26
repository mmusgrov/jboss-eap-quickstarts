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

import org.jboss.as.quickstarts.cmt.jts.ejb.ASFailureMode;
import org.jboss.as.quickstarts.cmt.jts.ejb.ASFailureSpec;
import org.jboss.as.quickstarts.cmt.jts.ejb.ASFailureType;
import org.jboss.as.quickstarts.cmt.jts.ejb.DummyXAResource;
import org.jboss.as.quickstarts.cmt.jts.ejb.XARCallback;

import javax.annotation.PostConstruct;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.Xid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RemoteHome(InvoiceManagerEJBHome.class)
@Stateless

//@Remote(InvoiceManagerEJB.class)

public class InvoiceManagerEJBImpl implements XARCallback { //} implements InvoiceManagerEJB {
    private List<String> invoices;
    private String serverType;
    private TransactionManager transactionManager;

    @PostConstruct
    private void init() {
        if ( System.getProperty("jboss.node.name") == null)
            serverType = "weblogic";
        else
            serverType = "wildfly";
        invoices = new ArrayList<>();
        invoices.add("initial invoice");

        transactionManager = getTransactionManager();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String createInvoice(String name) {
        return addInvoice("Invoice " + name);
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String createInvoiceInTxn(String name) {
        return addInvoice("Invoice in txn " + name);
    }

    private String addInvoice(String invoice) {
        invoice = String.format("%s: %s (%s)", serverType, invoice, LocalDateTime.now());
        invoices.add(invoice);

        if (invoice.contains("halt"))
            injectFault(ASFailureType.XARES_COMMIT, ASFailureMode.HALT, "");

        injectFault(ASFailureType.NONE, ASFailureMode.NONE, "");

        System.out.printf("InvoiceManagerEJBImpl: created invoice: %s%n", invoice);

        return invoice;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    @SuppressWarnings("unchecked")
    public List<String> listInvoices() {
        System.out.printf("InvoiceManagerEJBImpl: returning %d invoices%n", invoices.size());
        return invoices;
    }

    private void injectFault(ASFailureType type, ASFailureMode mode, String modeArg) {
        if (transactionManager != null) {
            ASFailureSpec fault = new ASFailureSpec("fault", mode, modeArg, type);
            DummyXAResource res = new DummyXAResource(fault, this);
            System.out.printf("enlisting dummy resource%n");
            try {
                transactionManager.getTransaction().enlistResource(res);
            } catch (Exception e) {
                System.out.printf("transaction enlistment not available: %s%n", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private TransactionManager getTransactionManager() {
        String[] names = {
                "java:jboss/TransactionManager", // wildfly
                "javax.transaction.TransactionManager", // weblogic

                "java:/TransactionManager",
                "java:comp/TransactionManager",
                "java:appserver/TransactionManager",
                "java:pm/TransactionManager",
                "java:comp/UserTransaction"
        };

        InitialContext context;

        try {
            context = new InitialContext();
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }

        for (String name : names) {
            try {
                TransactionManager tm = (TransactionManager) context.lookup(name);

                System.out.printf("TransactionManager JNDI name: %s%n", name);
                return tm;
            } catch (NamingException e) {
            }
        }

        return null;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) {
        System.out.printf("commit called%n");
    }

    @Override
    public int prepare(Xid xid) {
        System.out.printf("prepare called%n");

        return 0;
    }

    @Override
    public int rollback(Xid xid) {
        System.out.printf("rollback called%n");

        return 0;
    }
}
