package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.buildRequestPatternFrom;
import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.write;
import static java.net.HttpURLConnection.HTTP_OK;

import com.tomakehurst.wiremock.global.GlobalSettings;
import com.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.servlet.ResponseRenderer;
import com.tomakehurst.wiremock.verification.RequestJournal;
import com.tomakehurst.wiremock.verification.VerificationResult;

public class AdminRequestHandler extends AbstractRequestHandler {
	
	private final Mappings mappings;
	private final JsonMappingCreator jsonMappingCreator;
	private final RequestJournal requestJournal;
	private final GlobalSettingsHolder globalSettingsHolder;
	
	public AdminRequestHandler(Mappings mappings, RequestJournal requestJournal,
			GlobalSettingsHolder globalSettingsHolder, ResponseRenderer responseRenderer) {
		super(responseRenderer);
		this.mappings = mappings;
		this.requestJournal = requestJournal;
		this.globalSettingsHolder = globalSettingsHolder;
		jsonMappingCreator = new JsonMappingCreator(mappings);
	}

	@Override
	public ResponseDefinition handleRequest(Request request) {
		if (isNewMappingRequest(request)) {
			jsonMappingCreator.addMappingFrom(request.getBodyAsString());
			return ResponseDefinition.created();
		} else if (isResetRequest(request)) {
			mappings.reset();
			requestJournal.reset();
			return ResponseDefinition.ok();
		} else if (isRequestCountRequest(request)) {
			return getRequestCount(request);
		} else if (isGlobalSettingsUpdateRequest(request)) {
			GlobalSettings newSettings = JsonMappingBinder.read(request.getBodyAsString(), GlobalSettings.class);
			globalSettingsHolder.replaceWith(newSettings);
			return ResponseDefinition.ok();
		} else {
			return ResponseDefinition.notFound();
		}
	}

	private boolean isGlobalSettingsUpdateRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/settings");
	}

	private ResponseDefinition getRequestCount(Request request) {
		RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
		int matchingRequestCount = requestJournal.countRequestsMatching(requestPattern);
		ResponseDefinition response = new ResponseDefinition(HTTP_OK, write(new VerificationResult(matchingRequestCount)));
		response.addHeader("Content-Type", "application/json");
		return response;
	}

	private boolean isResetRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/reset");
	}

	private boolean isNewMappingRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/mappings/new");
	}
	
	private boolean isRequestCountRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && request.getUrl().equals("/requests/count");
	}
	
}
