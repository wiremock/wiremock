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
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.global.RequestDelaySpec;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.github.tomakehurst.wiremock.verification.*;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public class WireMockApp implements StubServer, Admin {
    
    public static final String FILES_ROOT = "__files";
    public static final String ADMIN_CONTEXT_ROOT = "/__admin";

    private final StubMappings stubMappings;
    private final RequestJournal requestJournal;
    private final GlobalSettingsHolder globalSettingsHolder;
    private final RequestDelayControl requestDelayControl;
    private final boolean browserProxyingEnabled;
    private final MappingsLoader defaultMappingsLoader;
    private final Container container;
    private final MappingsSaver mappingsSaver;
    private final Map<String, ResponseTransformer> transformers;
    private final FileSource rootFileSource;

    public WireMockApp(
            RequestDelayControl requestDelayControl,
            boolean browserProxyingEnabled,
            MappingsLoader defaultMappingsLoader,
            MappingsSaver mappingsSaver,
            boolean requestJournalDisabled,
            Map<String, ResponseTransformer> transformers,
            FileSource rootFileSource,
            Container container) {
        this.requestDelayControl = requestDelayControl;
        this.browserProxyingEnabled = browserProxyingEnabled;
        this.defaultMappingsLoader = defaultMappingsLoader;
        this.mappingsSaver = mappingsSaver;
        globalSettingsHolder = new GlobalSettingsHolder();
        stubMappings = new InMemoryStubMappings();
        requestJournal = requestJournalDisabled ? new DisabledRequestJournal() : new InMemoryRequestJournal();
        this.transformers = transformers;
        this.rootFileSource = rootFileSource;
        this.container = container;
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
    public ResponseDefinition serveStubFor(Request request) {
        ResponseDefinition baseResponseDefinition = stubMappings.serveFor(request);
        requestJournal.requestReceived(request);

        ResponseDefinition responseDefinition = applyTransformations(request,
                                                                     baseResponseDefinition,
                                                                     ImmutableList.copyOf(transformers.values()));

        if (!responseDefinition.wasConfigured() && request.isBrowserProxyRequest() && browserProxyingEnabled) {
            return ResponseDefinition.browserProxy(request);
        }

        return responseDefinition;
    }

    private ResponseDefinition applyTransformations(Request request,
                                                    ResponseDefinition responseDefinition,
                                                    List<ResponseTransformer> transformers) {
        if (transformers.isEmpty()) {
            return responseDefinition;
        }

        ResponseTransformer transformer = transformers.get(0);
        ResponseDefinition newResponseDef =
                transformer.applyGlobally() || responseDefinition.hasTransformer(transformer) ?
                transformer.transform(request, responseDefinition, rootFileSource.child(FILES_ROOT)) :
                responseDefinition;

        return applyTransformations(request, newResponseDef, transformers.subList(1, transformers.size()));
    }

    @Override
    public void addStubMapping(StubMapping stubMapping) {
        stubMappings.addMapping(stubMapping);
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
        requestDelayControl.clearDelay();
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
    public void updateGlobalSettings(GlobalSettings newSettings) {
        globalSettingsHolder.replaceWith(newSettings);
    }

    @Override
    public void addSocketAcceptDelay(RequestDelaySpec delaySpec) {
        requestDelayControl.setDelay(delaySpec.milliseconds());
    }

    @Override
    public void shutdownServer() {
        container.shutdown();
    }

}
