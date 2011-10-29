package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.buildRequestPatternFrom;
import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.write;
import static java.net.HttpURLConnection.HTTP_OK;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.verification.RequestJournal;
import com.tomakehurst.wiremock.verification.VerificationResult;

public class AdminRequestHandler extends AbstractRequestHandler {
	
	public static final String CONTEXT_KEY = "MappingRequestHandler";
	
	private Mappings mappings;
	private JsonMappingCreator jsonMappingCreator;
	private RequestJournal requestJournal;
	
	public AdminRequestHandler(Mappings mappings, RequestJournal requestJournal) {
		this.mappings = mappings;
		this.requestJournal = requestJournal;
		jsonMappingCreator = new JsonMappingCreator(mappings);
	}

	@Override
	public Response handleRequest(Request request) {
		if (isNewMappingRequest(request)) {
			jsonMappingCreator.addMappingFrom(request.getBodyAsString());
			return Response.created();
		} else if (isResetMappingsRequest(request)) {
			mappings.reset();
			return Response.ok();
		} else if (isRequestCountRequest(request)) {
			return getRequestCount(request);
		} else {
			return Response.notFound();
		}
	}

	private Response getRequestCount(Request request) {
		RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
		int matchingRequestCount = requestJournal.countRequestsMatching(requestPattern);
		Response response = new Response(HTTP_OK, write(new VerificationResult(matchingRequestCount)));
		response.addHeader("Content-Type", "application/json");
		return response;
	}

	private boolean isResetMappingsRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/mappings/reset");
	}

	private boolean isNewMappingRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/mappings/new");
	}
	
	private boolean isRequestCountRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/requests/count");
	}

}
