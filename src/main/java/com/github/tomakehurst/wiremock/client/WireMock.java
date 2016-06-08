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
import com.github.tomakehurst.wiremock.matching.*;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.matching.NewRequestPatternBuilder.allRequests;


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

    public static StringValuePattern equalTo(String value) {
        return new EqualToPattern(value);
    }

    public static StringValuePattern equalToJson(String value) {
        return new EqualToJsonPattern(value, null, null);
    }

    public static StringValuePattern equalToJson(String value, boolean ignoreArrayOrder, boolean ignoreExtraElements) {
        return new EqualToJsonPattern(value, ignoreArrayOrder, ignoreExtraElements);
    }

    public static StringValuePattern matchingJsonPath(String value) {
        return new MatchesJsonPathPattern(value);
    }

    public static StringValuePattern equalToXml(String value) {
        return new EqualToXmlPattern(value);
    }

    public static MatchesXPathPattern matchingXPath(String value) {
        return new MatchesXPathPattern(value, Collections.<String, String>emptyMap());
    }

    public static StringValuePattern matchingXPath(String value, Map<String, String> namespaces) {
        return new MatchesXPathPattern(value, namespaces);
    }

    public static StringValuePattern containing(String value) {
        return new ContainsPattern(value);
    }

    public static StringValuePattern matching(String regex) {
        return new RegexPattern(regex);
    }

    public static StringValuePattern notMatching(String regex) {
        return new NegativeRegexPattern(regex);
    }

    public static StringValuePattern absent() {
        return StringValuePattern.ABSENT;
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

    public static UrlPattern urlEqualTo(String testUrl) {
        return new UrlPattern(equalTo(testUrl), false);
    }

    public static UrlPattern urlMatching(String urlRegex) {
        return new UrlPattern(matching(urlRegex), true);
    }

    public static UrlPathPattern urlPathEqualTo(String testUrl) {
        return new UrlPathPattern(equalTo(testUrl), false);
    }

    public static UrlPathPattern urlPathMatching(String urlRegex) {
        return new UrlPathPattern(matching(urlRegex), true);
    }

    public static UrlPattern anyUrl() {
        return new UrlPattern(new AnythingPattern(), false);
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

	public static RemoteMappingBuilder get(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.GET, urlPattern);
	}

	public static RemoteMappingBuilder post(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.POST, urlPattern);
	}

	public static RemoteMappingBuilder put(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.PUT, urlPattern);
	}

	public static RemoteMappingBuilder delete(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.DELETE, urlPattern);
	}

	public static RemoteMappingBuilder patch(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.PATCH, urlPattern);
	}

	public static RemoteMappingBuilder head(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.HEAD, urlPattern);
	}

	public static RemoteMappingBuilder options(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.OPTIONS, urlPattern);
	}

	public static RemoteMappingBuilder trace(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.TRACE, urlPattern);
	}

	public static RemoteMappingBuilder any(UrlPattern urlPattern) {
		return new MappingBuilder(RequestMethod.ANY, urlPattern);
	}

    public static RemoteMappingBuilder request(String method, UrlPattern urlPattern) {
        return new MappingBuilder(RequestMethod.fromString(method), urlPattern);
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

	public static ResponseDefinitionBuilder aResponse() {
		return new ResponseDefinitionBuilder();
	}

	public void verifyThat(NewRequestPatternBuilder requestPatternBuilder) {
		NewRequestPattern requestPattern = requestPatternBuilder.build();
        VerificationResult result = admin.countRequestsMatching(requestPattern);
        result.assertRequestJournalEnabled();

		if (result.getCount() < 1) {
			throw new VerificationException(requestPattern, find(allRequests()));
		}
	}

	public void verifyThat(int count, NewRequestPatternBuilder requestPatternBuilder) {
		NewRequestPattern requestPattern = requestPatternBuilder.build();
        VerificationResult result = admin.countRequestsMatching(requestPattern);
        result.assertRequestJournalEnabled();

		if (result.getCount() != count) {
            throw new VerificationException(requestPattern, count, find(allRequests()));
		}
	}

	public void verifyThat(CountMatchingStrategy count, NewRequestPatternBuilder requestPatternBuilder) {
		NewRequestPattern requestPattern = requestPatternBuilder.build();
		VerificationResult result = admin.countRequestsMatching(requestPattern);
		result.assertRequestJournalEnabled();

		if (!count.match(result.getCount())) {
			throw new VerificationException(requestPattern, count, find(allRequests()));
		}
	}

	public static void verify(NewRequestPatternBuilder requestPatternBuilder) {
		defaultInstance.get().verifyThat(requestPatternBuilder);
	}

	public static void verify(int count, NewRequestPatternBuilder requestPatternBuilder) {
		defaultInstance.get().verifyThat(count, requestPatternBuilder);
	}

	public static void verify(CountMatchingStrategy countMatchingStrategy, NewRequestPatternBuilder requestPatternBuilder) {
		defaultInstance.get().verifyThat(countMatchingStrategy, requestPatternBuilder);
	}

    public List<LoggedRequest> find(NewRequestPatternBuilder requestPatternBuilder) {
        FindRequestsResult result = admin.findRequestsMatching(requestPatternBuilder.build());
        result.assertRequestJournalEnabled();
        return result.getRequests();
    }

    public static List<LoggedRequest> findAll(NewRequestPatternBuilder requestPatternBuilder) {
        return defaultInstance.get().find(requestPatternBuilder);
    }

	public static NewRequestPatternBuilder getRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.GET, urlPattern);
	}

	public static NewRequestPatternBuilder postRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.POST, urlPattern);
	}

	public static NewRequestPatternBuilder putRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.PUT, urlPattern);
	}

	public static NewRequestPatternBuilder deleteRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.DELETE, urlPattern);
	}

	public static NewRequestPatternBuilder patchRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.PATCH, urlPattern);
	}

	public static NewRequestPatternBuilder headRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.HEAD, urlPattern);
	}

	public static NewRequestPatternBuilder optionsRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.OPTIONS, urlPattern);
	}

	public static NewRequestPatternBuilder traceRequestedFor(UrlPattern urlPattern) {
		return new NewRequestPatternBuilder(RequestMethod.TRACE, urlPattern);
	}

    public static NewRequestPatternBuilder requestMadeFor(String customMatcherName, Parameters parameters) {
        return NewRequestPatternBuilder.forCustomMatcher(customMatcherName, parameters);
    }

	public static NewLocalRequestPatternBuilder requestMadeFor(RequestMatcher requestMatcher) {
		return NewLocalRequestPatternBuilder.forCustomMatcher(requestMatcher);
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
