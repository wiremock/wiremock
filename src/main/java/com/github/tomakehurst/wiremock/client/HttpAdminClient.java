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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.admin.*;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.RequestDelaySpec;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static com.github.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class HttpAdminClient implements Admin {
	
	private static final String ADMIN_URL_PREFIX = "http://%s:%d%s/__admin";

	private final String host;
	private final int port;
	private final String urlPathPrefix;
	
	private final HttpClient httpClient;
	
	public HttpAdminClient(String host, int port, String urlPathPrefix) {
		this.host = host;
		this.port = port;
		this.urlPathPrefix = urlPathPrefix;
		
		httpClient = HttpClientFactory.createClient();
	}
	
	public HttpAdminClient(String host, int port) {
		this(host, port, "");
	}

	@Override
	public void addStubMapping(StubMapping stubMapping) {
        postJsonAssertOkAndReturnBody(
                urlFor(NewStubMappingTask.class),
                Json.write(stubMapping),
                HTTP_CREATED);
	}

    @Override
    public ListStubMappingsResult listAllStubMappings() {
        String body = getJsonAssertOkAndReturnBody(
                urlFor(RootTask.class),
                HTTP_OK);
        return Json.read(body, ListStubMappingsResult.class);
    }

    @Override
    public void saveMappings() {
        postJsonAssertOkAndReturnBody(urlFor(SaveMappingsTask.class), null, HTTP_OK);
    }

    @Override
	public void resetMappings() {
		postJsonAssertOkAndReturnBody(urlFor(ResetTask.class), null, HTTP_OK);
	}
	
	@Override
	public void resetScenarios() {
        postJsonAssertOkAndReturnBody(urlFor(ResetScenariosTask.class), null, HTTP_OK);
	}

    @Override
    public void resetToDefaultMappings() {
        postJsonAssertOkAndReturnBody(urlFor(ResetToDefaultMappingsTask.class), null, HTTP_OK);
    }

	@Override
	public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
		String body = postJsonAssertOkAndReturnBody(
                urlFor(GetRequestCountTask.class),
                Json.write(requestPattern),
                HTTP_OK);
		return VerificationResult.from(body);
	}

    @Override
    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
        String body = postJsonAssertOkAndReturnBody(
                urlFor(FindRequestsTask.class),
                Json.write(requestPattern),
                HTTP_OK);
        return Json.read(body, FindRequestsResult.class);
    }

    @Override
	public void updateGlobalSettings(GlobalSettings settings) {
        postJsonAssertOkAndReturnBody(
                urlFor(GlobalSettingsUpdateTask.class),
                Json.write(settings),
                HTTP_OK);
	}

    @Override
    public void addSocketAcceptDelay(RequestDelaySpec spec) {
        postJsonAssertOkAndReturnBody(
                urlFor(SocketDelayTask.class),
                Json.write(spec),
                HTTP_OK);
    }

    @Override
    public void shutdownServer() {
        postJsonAssertOkAndReturnBody(urlFor(ShutdownServerTask.class), null, HTTP_OK);
    }

    private String postJsonAssertOkAndReturnBody(String url, String json, int expectedStatus) {
		HttpPost post = new HttpPost(url);
		try {
			if (json != null) {
				post.setEntity(new StringEntity(json, APPLICATION_JSON));
			}
			HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != expectedStatus) {
				throw new VerificationException(
                        "Expected status " + expectedStatus + " for " + url + " but was " + statusCode);
			}

            return getEntityAsStringAndCloseStream(response);
        } catch (Exception e) {
            return throwUnchecked(e, String.class);
        }
    }

    private String getJsonAssertOkAndReturnBody(String url, int expectedStatus) {
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != expectedStatus) {
                throw new VerificationException(
                        "Expected status " + expectedStatus + " for " + url + " but was " + statusCode);
            }

            return getEntityAsStringAndCloseStream(response);
        } catch (Exception e) {
            return throwUnchecked(e, String.class);
        }
    }

    private String urlFor(Class<? extends AdminTask> taskClass) {
        RequestSpec requestSpec = AdminTasks.requestSpecForTask(taskClass);
        return String.format(ADMIN_URL_PREFIX + requestSpec.path(), host, port, urlPathPrefix);
    }
}
