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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.RemoveException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RemoteHome(InvoiceManagerEJBHome.class)
@Stateless

//@Remote(InvoiceManagerEJB.class)

public class InvoiceManagerEJBImpl { //} implements InvoiceManagerEJB {
    private List<String> invoices;

    @PostConstruct
    private void init() {
        invoices = new ArrayList<>();
        invoices.add("initial invoice");
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
        LocalDateTime time = LocalDateTime.now();
        invoice = invoice + "\t" + time.toString();
        invoices.add(invoice);

        System.out.printf("InvoiceManagerEJBImpl: created invoice: %s%n", invoice);
        return invoice;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    @SuppressWarnings("unchecked")
    public List<String> listInvoices() {
        System.out.printf("InvoiceManagerEJBImpl: returning %d invoices%n", invoices.size());
        return invoices;
    }
}
