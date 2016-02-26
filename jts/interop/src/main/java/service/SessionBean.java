package service;

import service.remote.ISessionHome;

import javax.annotation.PostConstruct;
import javax.ejb.RemoteHome;

import javax.ejb.Stateless;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.concurrent.atomic.AtomicInteger;

@Stateless
//@Local(service.local.ISession.class)
//@Remote(service.remote.ISession.class)
@RemoteHome(ISessionHome.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBean {
	static AtomicInteger counter = new AtomicInteger(0);

	@PostConstruct
	public void init() {
		boolean isWF = System.getProperty("jboss.node.name") != null;

		counter.set(isWF ? 8000 : 7000);
	}

	public String getNext() {
		return String.valueOf(counter.getAndIncrement());
	}
}
