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
import com.github.tomakehurst.wiremock.verification.VerificationResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.tomakehurst.wiremock.common.Json.write;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.buildRequestPatternFrom;
import static java.net.HttpURLConnection.HTTP_OK;

public class AdminRequestHandler extends AbstractRequestHandler {

    private final Admin admin;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private static @interface AdminOperation {

        String value();
    }

	public AdminRequestHandler(Admin admin, ResponseRenderer responseRenderer) {
		super(responseRenderer);
        this.admin = admin;
	}

	@Override
	public ResponseDefinition handleRequest(Request request) {
        notifier().info("Received request to " + request.getUrl() + " with body " + request.getBodyAsString());

        if (request.getMethod() == RequestMethod.POST) {
            Method m = getMethodFromPath(withoutAdminRoot(request.getUrl()));
            if (m != null) {
                return invoke(m, request);
            }
        }
		return ResponseDefinition.notFound();
	}

    private Method getMethodFromPath(String path) {
        for (Method m : getClass().getDeclaredMethods()) {
            if (isMethodForPath(m, path)) {
                return m;
            }
        }
        return null;
    }

    private boolean isMethodForPath(Method m, String path) {
        AdminOperation a = m.getAnnotation(AdminOperation.class);
        return a!=null && a.value().equals(path);
    }

    private ResponseDefinition invoke(Method m, Request request) {
        try {
            if (m.getParameterTypes().length == 0) {
                return (ResponseDefinition) m.invoke(this);
            } else {
                return (ResponseDefinition) m.invoke(this, request);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException) e.getTargetException();
        }
    }

    private static String withoutAdminRoot(String url) {
        return url.replace(ADMIN_CONTEXT_ROOT, "");
    }

    @AdminOperation("/reset")
    private ResponseDefinition reset() {
        admin.resetMappings();
        return ResponseDefinition.ok();
    }

    @AdminOperation("/scenarios/reset")
    private ResponseDefinition resetScenarios() {
        admin.resetScenarios();
        return ResponseDefinition.ok();
    }

    @AdminOperation("/mappings/reset")
    private ResponseDefinition resetToDefaultMappings() {
        admin.resetToDefaultMappings();
        return ResponseDefinition.ok();
    }

    @AdminOperation("/mappings/new")
    private ResponseDefinition newMapping(Request request) {
        StubMapping newMapping = StubMapping.buildFrom(request.getBodyAsString());
        admin.addStubMapping(newMapping);
        return ResponseDefinition.created();
    }

    @AdminOperation("/requests/count")
    private ResponseDefinition getRequestCount(Request request) {
        RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
        int matchingRequestCount = admin.countRequestsMatching(requestPattern);
        ResponseDefinition response = new ResponseDefinition(HTTP_OK, write(new VerificationResult(matchingRequestCount)));
        response.setHeaders(new HttpHeaders(httpHeader("Content-Type", "application/json")));
        return response;
    }

    @AdminOperation("/requests/find")
    private ResponseDefinition findRequests(Request request) {
        RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
        FindRequestsResult result = admin.findRequestsMatching(requestPattern);
        ResponseDefinition response = new ResponseDefinition(HTTP_OK, Json.write(result));
        response.setHeaders(new HttpHeaders(httpHeader("Content-Type", "application/json")));
        return response;
    }

    @AdminOperation("/settings")
    private ResponseDefinition globalSettingsUpdate(Request request) {
        GlobalSettings newSettings = Json.read(request.getBodyAsString(), GlobalSettings.class);
        admin.updateGlobalSettings(newSettings);
        return ResponseDefinition.ok();
    }

    @AdminOperation("/socket-delay")
    private ResponseDefinition socketDelay(Request request) {
        RequestDelaySpec delaySpec = Json.read(request.getBodyAsString(), RequestDelaySpec.class);
        admin.addSocketAcceptDelay(delaySpec);
        return ResponseDefinition.ok();
    }
	
}
