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
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static com.github.tomakehurst.wiremock.http.MimeType.JSON;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

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
        String json = Json.write(stubMapping);
		int status = postJsonAndReturnStatus(urlFor(NewStubMappingTask.class), json);
		if (status != HTTP_CREATED) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	@Override
	public void resetMappings() {
		int status = postEmptyBodyAndReturnStatus(urlFor(ResetTask.class));
		assertStatusOk(status);
	}
	
	@Override
	public void resetScenarios() {
		int status = postEmptyBodyAndReturnStatus(urlFor(ResetScenariosTask.class));
		assertStatusOk(status);
	}

    @Override
    public void resetToDefaultMappings() {
        int status = postEmptyBodyAndReturnStatus(urlFor(ResetToDefaultMappingsTask.class));
        assertStatusOk(status);
    }

    private void assertStatusOk(int status) {
		if (status != HTTP_OK) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	@Override
	public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
		String json = Json.write(requestPattern);
		String body = postJsonAssertOkAndReturnBody(urlFor(GetRequestCountTask.class), json, HTTP_OK);
		return VerificationResult.from(body);
	}

    @Override
    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
        String json = Json.write(requestPattern);
        String body = postJsonAssertOkAndReturnBody(urlFor(FindRequestsTask.class), json, HTTP_OK);
        return Json.read(body, FindRequestsResult.class);
    }

    @Override
	public void updateGlobalSettings(GlobalSettings settings) {
		String json = Json.write(settings);
		postJsonAssertOkAndReturnBody(urlFor(GlobalSettingsUpdateTask.class), json, HTTP_OK);
	}

    @Override
    public void addSocketAcceptDelay(RequestDelaySpec spec) {
        String json = Json.write(spec);
        postJsonAssertOkAndReturnBody(urlFor(SocketDelayTask.class), json, HTTP_OK);
    }

    private int postJsonAndReturnStatus(String url, String json) {
		HttpPost post = new HttpPost(url);
		try {
			if (json != null) {
				post.setEntity(new StringEntity(json, JSON.toString(), "utf-8"));
			}
			HttpResponse response = httpClient.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			getEntityAsStringAndCloseStream(response);
			
			return statusCode;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String postJsonAssertOkAndReturnBody(String url, String json, int expectedStatus) {
		HttpPost post = new HttpPost(url);
		try {
			if (json != null) {
				post.setEntity(new StringEntity(json, JSON.toString(), "utf-8"));
			}
			HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != expectedStatus) {
				throw new VerificationException(
                        "Expected status " + expectedStatus + " for " + url + " but was " + statusCode);
			}

            return getEntityAsStringAndCloseStream(response);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    private String urlFor(Class<? extends AdminTask> taskClass) {
        RequestSpec requestSpec = AdminTasks.requestSpecForTask(taskClass);
        return String.format(ADMIN_URL_PREFIX + requestSpec.path(), host, port, urlPathPrefix);
    }
	
	private int postEmptyBodyAndReturnStatus(String url) {
		return postJsonAndReturnStatus(url, null);
	}
}
