package service;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
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
	@Path("/remote/{jndiPort}")
	public Response getRemoteNextCount(@DefaultValue("0") @PathParam("jndiPort") int jndiPort) {
		return getRemoteNextCountWithASAndError(jndiPort, null, null);
	}

	@GET
	@Path("/remote/{jndiPort}/{as}/{failureType}")
	public Response getRemoteNextCountWithASAndError(
			@DefaultValue("0") @PathParam("jndiPort") int jndiPort,
			@DefaultValue("") @PathParam("as") String as,
			@PathParam("failureType") String failureType) {
		return Response.status(200)
				.entity("Next: " + service.getNext(false, as, jndiPort, failureType))
				.build();
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
