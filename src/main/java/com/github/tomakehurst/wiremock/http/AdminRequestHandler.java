/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.RequestDelaySpec;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.RequestJournalDisabledException;
import com.github.tomakehurst.wiremock.verification.VerificationResult;

import static com.github.tomakehurst.wiremock.common.Json.write;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.buildRequestPatternFrom;
import static java.net.HttpURLConnection.HTTP_OK;

public class AdminRequestHandler extends AbstractRequestHandler {

    private final Admin admin;

	public AdminRequestHandler(Admin admin, ResponseRenderer responseRenderer) {
		super(responseRenderer);
        this.admin = admin;
	}

	@Override
	public ResponseDefinition handleRequest(Request request) {
        notifier().info("Received request to " + request.getUrl() + " with body " + request.getBodyAsString());

		if (isNewMappingRequest(request)) {
            StubMapping newMapping = StubMapping.buildFrom(request.getBodyAsString());
            admin.addStubMapping(newMapping);
			return ResponseDefinition.created();
		} else if (isResetRequest(request)) {
			admin.resetMappings();
			return ResponseDefinition.ok();
		} else if (isResetScenariosRequest(request)) {
			admin.resetScenarios();
			return ResponseDefinition.ok();
		} else if (isResetToDefaultMappingsRequest(request)) {
            admin.resetToDefaultMappings();
            return ResponseDefinition.ok();
        } else if (isRequestCountRequest(request)) {
			return getRequestCount(request);
        } else if (isFindRequestsRequest(request)) {
            return findRequests(request);
		} else if (isGlobalSettingsUpdateRequest(request)) {
			GlobalSettings newSettings = Json.read(request.getBodyAsString(), GlobalSettings.class);
            admin.updateGlobalSettings(newSettings);
			return ResponseDefinition.ok();
        } else if (isSocketDelayRequest(request)) {
            RequestDelaySpec delaySpec = Json.read(request.getBodyAsString(), RequestDelaySpec.class);
            admin.addSocketAcceptDelay(delaySpec);
            return ResponseDefinition.ok();
		} else {
			return ResponseDefinition.notFound();
		}
	}

	private boolean isGlobalSettingsUpdateRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/settings");
	}

	private ResponseDefinition getRequestCount(Request request) {
		RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
        VerificationResult result = admin.countRequestsMatching(requestPattern);
		ResponseDefinition response = new ResponseDefinition(HTTP_OK, write(result));
		response.setHeaders(new HttpHeaders(httpHeader("Content-Type", "application/json")));
		return response;
	}

    private ResponseDefinition findRequests(Request request) {
        RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
        FindRequestsResult result = admin.findRequestsMatching(requestPattern);
        ResponseDefinition response = new ResponseDefinition(HTTP_OK, Json.write(result));
        response.setHeaders(new HttpHeaders(httpHeader("Content-Type", "application/json")));
        return response;
    }

	private boolean isResetRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/reset");
	}
	
	private boolean isResetScenariosRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/scenarios/reset");
	}

    private boolean isResetToDefaultMappingsRequest(Request request) {
        return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/mappings/reset");
    }

	private boolean isNewMappingRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/mappings/new");
	}
	
	private boolean isRequestCountRequest(Request request) {
		return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/requests/count");
	}

    private boolean isFindRequestsRequest(Request request) {
        return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/requests/find");
    }

    private boolean isSocketDelayRequest(Request request) {
        return request.getMethod() == RequestMethod.POST && withoutAdminRoot(request.getUrl()).equals("/socket-delay");
    }

	private static String withoutAdminRoot(String url) {
	    return url.replace(ADMIN_CONTEXT_ROOT, "");
	}
	
}
