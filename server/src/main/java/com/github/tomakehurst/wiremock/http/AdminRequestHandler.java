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

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.AdminTasks;
import com.github.tomakehurst.wiremock.admin.RequestSpec;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.RequestDelaySpec;
import com.github.tomakehurst.wiremock.http.AbstractRequestHandler;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.http.ResponseRenderer;
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
        AdminTask adminTask = AdminTasks.taskFor(request.getMethod(), withoutAdminRoot(request.getUrl()));
        return adminTask.execute(admin, request);
	}

	private static String withoutAdminRoot(String url) {
	    return url.replace(ADMIN_CONTEXT_ROOT, "");
	}
	
}
