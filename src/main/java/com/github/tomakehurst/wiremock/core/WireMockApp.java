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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.*;
import com.github.tomakehurst.wiremock.verification.*;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.stubbing.ServedStub.NOT_MATCHED;
import static com.github.tomakehurst.wiremock.stubbing.ServedStub.TO_LOGGED_REQUEST;
import static com.google.common.collect.FluentIterable.from;

public class WireMockApp implements StubServer, Admin {
    
    public static final String FILES_ROOT = "__files";
    public static final String ADMIN_CONTEXT_ROOT = "/__admin";

    private final StubMappings stubMappings;
    private final RequestJournal requestJournal;
    private final GlobalSettingsHolder globalSettingsHolder;
    private final boolean browserProxyingEnabled;
    private final MappingsLoader defaultMappingsLoader;
    private final Container container;
    private final MappingsSaver mappingsSaver;
    private final NearMissCalculator nearMissCalculator;


    public WireMockApp(
            boolean browserProxyingEnabled,
            MappingsLoader defaultMappingsLoader,
            MappingsSaver mappingsSaver,
            boolean requestJournalDisabled,
            Optional<Integer> maxRequestJournalEntries,
            Map<String, ResponseDefinitionTransformer> transformers,
            Map<String, RequestMatcherExtension> requestMatchers,
            FileSource rootFileSource,
            Container container) {
        this.browserProxyingEnabled = browserProxyingEnabled;
        this.defaultMappingsLoader = defaultMappingsLoader;
        this.mappingsSaver = mappingsSaver;
        globalSettingsHolder = new GlobalSettingsHolder();
        requestJournal = requestJournalDisabled ? new DisabledRequestJournal() : new InMemoryRequestJournal(maxRequestJournalEntries);
        stubMappings = new InMemoryStubMappings(requestMatchers, requestJournal, transformers, rootFileSource);
        this.container = container;
        nearMissCalculator = new NearMissCalculator(stubMappings, requestJournal);
        loadDefaultMappings();
    }

    public GlobalSettingsHolder getGlobalSettingsHolder() {
        return globalSettingsHolder;
    }

    private void loadDefaultMappings() {
        loadMappingsUsing(defaultMappingsLoader);
    }

    public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
        mappingsLoader.loadMappingsInto(stubMappings);
    }
    
    @Override
    public ServedStub serveStubFor(Request request) {
        ServedStub servedStub = stubMappings.serveFor(request);

        if (servedStub.isNoExactMatch() && request.isBrowserProxyRequest() && browserProxyingEnabled) {
            return ServedStub.exactMatch(LoggedRequest.createFrom(request), ResponseDefinition.browserProxy(request));
        }

        return servedStub;
    }

    @Override
    public void addStubMapping(StubMapping stubMapping) {
        stubMappings.addMapping(stubMapping);
    }

    @Override
    public void editStubMapping(StubMapping stubMapping) {
        stubMappings.editMapping(stubMapping);
    }

    @Override
    public ListStubMappingsResult listAllStubMappings() {
        return new ListStubMappingsResult(stubMappings.getAll());
    }

    @Override
    public void saveMappings() {
        mappingsSaver.saveMappings(stubMappings);
    }

    @Override
    public void resetMappings() {
        stubMappings.reset();
        requestJournal.reset();
    }

    @Override
    public void resetRequests() {
        requestJournal.reset();
    }

    @Override
    public void resetToDefaultMappings() {
        resetMappings();
        loadDefaultMappings();
    }

    @Override
    public void resetScenarios() {
        stubMappings.resetScenarios();
    }

    @Override
    public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
        try {
            return VerificationResult.withCount(requestJournal.countRequestsMatching(requestPattern));
        } catch (RequestJournalDisabledException e) {
            return VerificationResult.withRequestJournalDisabled();
        }
    }
    
    @Override
    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
        try {
            List<LoggedRequest> requests = requestJournal.getRequestsMatching(requestPattern);
            return FindRequestsResult.withRequests(requests);
        } catch (RequestJournalDisabledException e) {
            return FindRequestsResult.withRequestJournalDisabled();
        }
    }

    @Override
    public FindRequestsResult findUnmatchedRequests() {
        try {
            List<LoggedRequest> requests =
                from(requestJournal.getAllServedStubs())
                .filter(NOT_MATCHED)
                .transform(TO_LOGGED_REQUEST)
                .toList();
            return FindRequestsResult.withRequests(requests);
        } catch (RequestJournalDisabledException e) {
            return FindRequestsResult.withRequestJournalDisabled();
        }
    }

    @Override
    public FindNearMissesResult findNearMissesForUnmatchedRequests() {
        ImmutableList.Builder<NearMiss> listBuilder = ImmutableList.builder();
        Iterable<ServedStub> unmatchedServedStubs =
            from(requestJournal.getAllServedStubs())
            .filter(new Predicate<ServedStub>() {
                @Override
                public boolean apply(ServedStub input) {
                    return input.isNoExactMatch();
                }
            });

        for (ServedStub servedStub: unmatchedServedStubs) {
            listBuilder.addAll(nearMissCalculator.findNearestTo(servedStub.getRequest()));
        }

        return new FindNearMissesResult(listBuilder.build());
    }

    @Override
    public FindNearMissesResult findNearMissesFor(LoggedRequest loggedRequest) {
        List<NearMiss> nearMisses = nearMissCalculator.findNearestTo(loggedRequest);
        return new FindNearMissesResult(nearMisses);
    }

    @Override
    public void updateGlobalSettings(GlobalSettings newSettings) {
        globalSettingsHolder.replaceWith(newSettings);
    }

    public int port() {
        return container.port();
    }

    @Override
    public void shutdownServer() {
        container.shutdown();
    }

}
