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
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.http.DelayDistribution;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
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
	private final GlobalSettingsHolder globalSettingsHolder = new GlobalSettingsHolder();

	private static ThreadLocal<WireMock> defaultInstance = new ThreadLocal<WireMock>(){
            @Override 
            protected WireMock initialValue() {
            	return new WireMock();
            }
	};

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

	public WireMock(String scheme, String host, int port) {
		admin = new HttpAdminClient(scheme, host, port);
	}

	public WireMock(String scheme, String host, int port, String urlPathPrefix) {
		admin = new HttpAdminClient(scheme, host, port, urlPathPrefix);
	}

	public WireMock() {
		admin = new HttpAdminClient(DEFAULT_HOST, DEFAULT_PORT);
	}

	public static void givenThat(RemoteMappingBuilder mappingBuilder) {
		defaultInstance.get().register(mappingBuilder);
	}

	public static void stubFor(RemoteMappingBuilder mappingBuilder) {
		givenThat(mappingBuilder);
	}

	public static void editStub(MappingBuilder mappingBuilder) {
		defaultInstance.get().editStubMapping(mappingBuilder);
	}

    public static ListStubMappingsResult listAllStubMappings() {
        return defaultInstance.get().allStubMappings();
    }

    public static void configureFor(int port) {
        defaultInstance.set(new WireMock(port));
    }

	public static void configureFor(String host, int port) {
		defaultInstance.set(new WireMock(host, port));
	}

	public static void configureFor(String host, int port, String urlPathPrefix) {
		defaultInstance.set(new WireMock(host, port, urlPathPrefix));
	}

	public static void configureFor(String scheme, String host, int port, String urlPathPrefix) {
		defaultInstance.set(new WireMock(scheme, host, port, urlPathPrefix));
	}

	public static void configureFor(String scheme, String host, int port) {
		defaultInstance.set(new WireMock(scheme, host, port));
	}

	public static void configure() {
		defaultInstance.set(new WireMock());
	}

    public void saveMappings() {
        admin.saveMappings();
    }

    public static void saveAllMappings() {
        defaultInstance.get().saveMappings();
    }

	public void resetMappings() {
		admin.resetMappings();
	}

	public static void reset() {
		defaultInstance.get().resetMappings();
	}

	public static void resetAllRequests() {
		defaultInstance.get().resetRequests();
	}

	public void resetRequests() {
		admin.resetRequests();
	}

	public void resetScenarios() {
		admin.resetScenarios();
	}

	public static void resetAllScenarios() {
		defaultInstance.get().resetScenarios();
	}

    public void resetToDefaultMappings() {
        admin.resetToDefaultMappings();
    }

    public static void resetToDefault() {
        defaultInstance.get().resetToDefaultMappings();
    }

	public void register(RemoteMappingBuilder mappingBuilder) {
		StubMapping mapping = mappingBuilder.build();
		register(mapping);
	}

    public void register(StubMapping mapping) {
        admin.addStubMapping(mapping);
    }

	public void editStubMapping(RemoteMappingBuilder mappingBuilder) {
		admin.editStubMapping(mappingBuilder.build());
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

	public static UrlMatchingStrategy urlPathMatching(String urlPath) {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrlPathPattern(urlPath);
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

	public static CountMatchingStrategy lessThan(int expected) {
		return new CountMatchingStrategy(CountMatchingStrategy.LESS_THAN, expected);
	}

	public static CountMatchingStrategy lessThanOrExactly(int expected) {
		return new CountMatchingStrategy(CountMatchingStrategy.LESS_THAN_OR_EQUAL, expected);
	}

	public static CountMatchingStrategy exactly(int expected) {
		return new CountMatchingStrategy(CountMatchingStrategy.EQUAL_TO, expected);
	}

	public static CountMatchingStrategy moreThanOrExactly(int expected) {
		return new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, expected);
	}

	public static CountMatchingStrategy moreThan(int expected) {
		return new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN, expected);
	}

	public static RemoteMappingBuilder get(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.GET, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder post(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.POST, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder put(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.PUT, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder delete(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.DELETE, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder patch(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.PATCH, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder head(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.HEAD, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder options(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.OPTIONS, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder trace(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.TRACE, urlMatchingStrategy);
	}

	public static RemoteMappingBuilder any(UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.ANY, urlMatchingStrategy);
	}

	public static MappingBuilder requestMatching(String customRequestMatcherName) {
		return new MappingBuilder(customRequestMatcherName, Parameters.empty());
	}

	public static RemoteMappingBuilder requestMatching(String customRequestMatcherName, Parameters parameters) {
		return new MappingBuilder(customRequestMatcherName, parameters);
	}

	public static LocalMappingBuilder requestMatching(RequestMatcher requestMatcher) {
		return new MappingBuilder(requestMatcher);
	}

	public static RemoteMappingBuilder request(String method, UrlMatchingStrategy urlMatchingStrategy) {
		return new MappingBuilder(RequestMethod.fromString(method), urlMatchingStrategy);
	}

	public static ResponseDefinitionBuilder aResponse() {
		return new ResponseDefinitionBuilder();
	}

	public void verifyThat(RequestPatternBuilder requestPatternBuilder) {
		RequestPattern requestPattern = requestPatternBuilder.build();
        VerificationResult result = admin.countRequestsMatching(requestPattern.toNewRequestPattern());
        result.assertRequestJournalEnabled();

		if (result.getCount() < 1) {
			throw new VerificationException(requestPattern, find(allRequests()));
		}
	}

	public void verifyThat(int count, RequestPatternBuilder requestPatternBuilder) {
		RequestPattern requestPattern = requestPatternBuilder.build();
        VerificationResult result = admin.countRequestsMatching(requestPattern.toNewRequestPattern());
        result.assertRequestJournalEnabled();

		if (result.getCount() != count) {
            throw new VerificationException(requestPattern, count, find(allRequests()));
		}
	}

	public void verifyThat(CountMatchingStrategy count, RequestPatternBuilder requestPatternBuilder) {
		RequestPattern requestPattern = requestPatternBuilder.build();
		VerificationResult result = admin.countRequestsMatching(requestPattern.toNewRequestPattern());
		result.assertRequestJournalEnabled();

		if (!count.match(result.getCount())) {
			throw new VerificationException(requestPattern, count, find(allRequests()));
		}
	}

	public static void verify(RequestPatternBuilder requestPatternBuilder) {
		defaultInstance.get().verifyThat(requestPatternBuilder);
	}

	public static void verify(int count, RequestPatternBuilder requestPatternBuilder) {
		defaultInstance.get().verifyThat(count, requestPatternBuilder);
	}

	public static void verify(CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder) {
		defaultInstance.get().verifyThat(countMatchingStrategy, requestPatternBuilder);
	}

    public List<LoggedRequest> find(RequestPatternBuilder requestPatternBuilder) {
        FindRequestsResult result = admin.findRequestsMatching(requestPatternBuilder.build());
        result.assertRequestJournalEnabled();
        return result.getRequests();
    }

    public static List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return defaultInstance.get().find(requestPatternBuilder);
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

	public static LocalRequestPatternBuilder requestMadeFor(RequestMatcher requestMatcher) {
		return LocalRequestPatternBuilder.forCustomMatcher(requestMatcher);
	}

	public static RequestPatternBuilder requestMadeFor(String customMatcherName, Parameters parameters) {
		return RequestPatternBuilder.forCustomMatcher(customMatcherName, parameters);
	}

	public static void setGlobalFixedDelay(int milliseconds) {
		defaultInstance.get().setGlobalFixedDelayVariable(milliseconds);
	}

	public void setGlobalFixedDelayVariable(int milliseconds) {
		GlobalSettings settings = globalSettingsHolder.get().copy();
		settings.setFixedDelay(milliseconds);
		updateGlobalSettings(settings);
	}

	public static void setGlobalRandomDelay(DelayDistribution distribution) {
		defaultInstance.get().setGlobalRandomDelayVariable(distribution);
	}

	public void setGlobalRandomDelayVariable(DelayDistribution distribution) {
		GlobalSettings settings = globalSettingsHolder.get().copy();
		settings.setDelayDistribution(distribution);
		updateGlobalSettings(settings);
	}

	private void updateGlobalSettings(GlobalSettings settings) {
		globalSettingsHolder.replaceWith(settings);
		admin.updateGlobalSettings(settings);
	}

    public void shutdown() {
        admin.shutdownServer();
    }

    public static void shutdownServer() {
        defaultInstance.get().shutdown();
    }

    public static List<NearMiss> findAllNearMisses() {
        return defaultInstance.get().findNearMissesForAllUnmatched();
    }

    public List<NearMiss> findNearMissesForAllUnmatched() {
        FindNearMissesResult nearMissesResult = admin.findNearMissesForUnmatchedRequests();
        return nearMissesResult.getNearMisses();
    }
}
