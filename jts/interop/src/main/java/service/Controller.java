package service;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

@Path("/")
public class Controller {
	@Inject
	ControllerBean service;

	@GET
	@Path("/local")
	public String getLocalNextCount() {
		return "Next: " + service.getNext(true);
	}

	@GET
	@Path("/remote")
	public String getRemoteNextCount() {
		return "Next: " + service.getNext(false);
	}

	private static String stackTraceToString(Exception e) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bytes, true);
		Throwable cause = e.getCause();

		e.printStackTrace(writer);

		while (cause != null) {
			writer.write("Caused By:");
			writer.println();
			cause.printStackTrace(writer);
			cause = cause.getCause();
		}

		return bytes.toString();
	}
}
