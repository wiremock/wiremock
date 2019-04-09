package com.github.tomakehurst.wiremock.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface TestResouce {

	@Path("/stuff")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TestResourceDTO getStuff();

	@Path("/stuff")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void postStuff(TestResourceDTO data);
}
