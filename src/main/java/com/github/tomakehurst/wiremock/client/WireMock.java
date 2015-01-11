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

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.RequestDelaySpec;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.RequestPatternBuilder.allRequests;


public class WireMock {
	
	private static final int DEFAULT_PORT = 8080;
	private static final String DEFAULT_HOST = "localhost";

	private final Admin admin;
	
	private static WireMock defaultInstance = new WireMock();

    public WireMock(Admin admin) {
        this.admin = admin;
    }

    public WireMock(int port) {
        this(DEFAULT_HOST, port);
    }

    public WireMock(String host, int port) {
		admin = new HttpAdminClient(host, port);
	}
	
	public WireMock(String host, int port, String urlPathPrefix) {
		admin = new HttpAdminClient(host, port, urlPathPrefix);
	}
	
	public WireMock() {
		admin = new HttpAdminClient(DEFAULT_HOST, DEFAULT_PORT);
	}
	
	public static void givenThat(MappingBuilder mappingBuilder) {
		defaultInstance.register(mappingBuilder);
	}
	
	public static void stubFor(MappingBuilder mappingBuilder) {
		givenThat(mappingBuilder);
	}

    public static ListStubMappingsResult listAllStubMappings() {
        return defaultInstance.allStubMappings();
    }

    public static void configureFor(int port) {
        defaultInstance = new WireMock(port);
    }

	public static void configureFor(String host, int port) {
		defaultInstance = new WireMock(host, port);
	}
	
	public static void configureFor(String host, int port, String urlPathPrefix) {
		defaultInstance = new WireMock(host, port, urlPathPrefix);
	}
	
	public static void configure() {
		defaultInstance = new WireMock();
	}

    public void saveMappings() {
        admin.saveMappings();
    }

    public static void saveAllMappings() {
        defaultInstance.saveMappings();
    }
	
	public void resetMappings() {
		admin.resetMappings();
	}
	
	public static void reset() {
		defaultInstance.resetMappings();
	}
	
	public void resetScenarios() {
		admin.resetScenarios();
	}
	
	public static void resetAllScenarios() {
		defaultInstance.resetScenarios();
	}

    public void resetToDefaultMappings() {
        admin.resetToDefaultMappings();
    }

    public static void resetToDefault() {
        defaultInstance.resetToDefaultMappings();
    }

	public void register(MappingBuilder mappingBuilder) {
		StubMapping mapping = mappingBuilder.build();
		register(mapping);
	}

    public void register(StubMapping mapping) {
        admin.addStubMapping(mapping);
    }

    public ListStubMappingsResult allStubMappings() {
        return admin.listAllStubMappings();
    }
	
	public static UrlMatchingStrategy urlEqualTo(String url) {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl(url);
		return urlStrategy;
	}
	
	public static UrlMatchingStrategy urlMatching(String url) {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrlPattern(url);
		return urlStrategy;
	}

    public static UrlMatchingStrategy urlPathEqualTo(String urlPath) {
        UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
        urlStrategy.setUrlPath(urlPath);
        return urlStrategy;
    }
	
	public static ValueMatchingStrategy equalTo(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setEqualTo(value);
		return headerStrategy;
	}
	
    public static ValueMatchingStrategy equalToJson(String value) {
        ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
        headerStrategy.setEqualToJson(value);
        return headerStrategy;
    }

    public static ValueMatchingStrategy equalToJson(String value, JSONCompareMode jsonCompareMode) {
        ValueMatchingStrategy valueMatchingStrategy = new ValueMatchingStrategy();
        valueMatchingStrategy.setJsonCompareMode(jsonCompareMode);
        valueMatchingStrategy.setEqualToJson(value);
        return valueMatchingStrategy;
    }

    public static ValueMatchingStrategy equalToXml(String value) {
        ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
        headerStrategy.setEqualToXml(value);
        return headerStrategy;
    }

    public static ValueMatchingStrategy matchingXPath(String value) {
        ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
        headerStrategy.setMatchingXPath(value);
        return headerStrategy;
    }

	public static ValueMatchingStrategy containing(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setContains(value);
		return headerStrategy;
	}
	
	public static ValueMatchingStrategy matching(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setMatches(value);
		return headerStrategy;
	}
	
	public static ValueMatchingStrategy notMatching(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setDoesNotMatch(value);
		return headerStrategy;
	}

    public static ValueMatchingStrategy matchingJsonPath(String jsonPath) {
        ValueMatchingStrategy matchingStrategy = new ValueMatchingStrategy();
        matchingStrategy.setJsonMatchesPath(jsonPath);
        return matchingStrategy;
    }
	
	public static MappingBuilder get(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.GET, urlMatchingStrategy);
	}
	
	public static MappingBuilder post(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.POST, urlMatchingStrategy);
	}
	
	public static MappingBuilder put(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.PUT, urlMatchingStrategy);
	}
	
	public static MappingBuilder delete(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.DELETE, urlMatchingStrategy);
	}
	
	public static MappingBuilder patch(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.PATCH, urlMatchingStrategy);
	}
	
	public static MappingBuilder head(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.HEAD, urlMatchingStrategy);
	}
	
	public static MappingBuilder options(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.OPTIONS, urlMatchingStrategy);
	}
	
	public static MappingBuilder trace(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.TRACE, urlMatchingStrategy);
	}

	public static MappingBuilder any(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.ANY, urlMatchingStrategy);
	}
	
	public static ResponseDefinitionBuilder aResponse() {
		return new ResponseDefinitionBuilder();
	}
	
	public void verifyThat(RequestPatternBuilder requestPatternBuilder) {
		RequestPattern requestPattern = requestPatternBuilder.build();
        VerificationResult result = admin.countRequestsMatching(requestPattern);
        result.assertRequestJournalEnabled();

		if (result.getCount() < 1) {
			throw new VerificationException(requestPattern, find(allRequests()));
		}
	}

	public void verifyThat(int count, RequestPatternBuilder requestPatternBuilder) {
		RequestPattern requestPattern = requestPatternBuilder.build();
        VerificationResult result = admin.countRequestsMatching(requestPattern);
        result.assertRequestJournalEnabled();

		if (result.getCount() != count) {
            throw new VerificationException(requestPattern, count, find(allRequests()));
		}
	}
	
	public static void verify(RequestPatternBuilder requestPatternBuilder) {
		defaultInstance.verifyThat(requestPatternBuilder);
	}
	
	public static void verify(int count, RequestPatternBuilder requestPatternBuilder) {
		defaultInstance.verifyThat(count, requestPatternBuilder);
	}

    public List<LoggedRequest> find(RequestPatternBuilder requestPatternBuilder) {
        FindRequestsResult result = admin.findRequestsMatching(requestPatternBuilder.build());
        result.assertRequestJournalEnabled();
        return result.getRequests();
    }

    public static List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return defaultInstance.find(requestPatternBuilder);
    }
	
	public static RequestPatternBuilder getRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.GET, urlMatchingStrategy);
	}
	
	public static RequestPatternBuilder postRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.POST, urlMatchingStrategy);
	}
	
	public static RequestPatternBuilder putRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.PUT, urlMatchingStrategy);
	}
	
	public static RequestPatternBuilder deleteRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.DELETE, urlMatchingStrategy);
	}

	public static RequestPatternBuilder patchRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.PATCH, urlMatchingStrategy);
	}
	
	public static RequestPatternBuilder headRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.HEAD, urlMatchingStrategy);
	}
	
	public static RequestPatternBuilder optionsRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.OPTIONS, urlMatchingStrategy);
	}
	
	public static RequestPatternBuilder traceRequestedFor(UrlMatchingStrategy urlMatchingStrategy) {
		return new RequestPatternBuilder(RequestMethod.TRACE, urlMatchingStrategy);
	}
	
	public static void setGlobalFixedDelay(int milliseconds) {
		defaultInstance.setGlobalFixedDelayVariable(milliseconds);
	}
	
	public void setGlobalFixedDelayVariable(int milliseconds) {
		GlobalSettings settings = new GlobalSettings();
		settings.setFixedDelay(milliseconds);
		admin.updateGlobalSettings(settings);
	}

    public void addDelayBeforeProcessingRequests(int milliseconds) {
        admin.addSocketAcceptDelay(new RequestDelaySpec(milliseconds));
    }

    public static void addRequestProcessingDelay(int milliseconds) {
        defaultInstance.addDelayBeforeProcessingRequests(milliseconds);
    }

    public void shutdown() {
        admin.shutdownServer();
    }

    public static void shutdownServer() {
        defaultInstance.shutdown();
    }
}
