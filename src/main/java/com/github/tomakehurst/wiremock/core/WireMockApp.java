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

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.admin.LimitAndOffsetPaginator;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.jetty9.websockets.Message;
import com.github.tomakehurst.wiremock.jetty9.websockets.WebSocketEndpoint;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.recording.*;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.github.tomakehurst.wiremock.verification.*;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.stubbing.ServeEvent.NOT_MATCHED;
import static com.github.tomakehurst.wiremock.stubbing.ServeEvent.TO_LOGGED_REQUEST;
import static com.google.common.collect.FluentIterable.from;

public class WireMockApp implements StubServer, Admin {

    public static final String FILES_ROOT = "__files";
    public static final String ADMIN_CONTEXT_ROOT = "/__admin";
    public static final String MAPPINGS_ROOT = "mappings";

    private final StubMappings stubMappings;
    private final RequestJournal requestJournal;
    private final GlobalSettingsHolder globalSettingsHolder;
    private final boolean browserProxyingEnabled;
    private final MappingsLoader defaultMappingsLoader;
    private final Container container;
    private final MappingsSaver mappingsSaver;
    private final NearMissCalculator nearMissCalculator;
    private final Recorder recorder;

    private Options options;

    public WireMockApp(final Options options, final Container container) {
        this.options = options;

        final FileSource fileSource = options.filesRoot();

        this.browserProxyingEnabled = options.browserProxyingEnabled();
        this.defaultMappingsLoader = options.mappingsLoader();
        this.mappingsSaver = options.mappingsSaver();
        this.globalSettingsHolder = new GlobalSettingsHolder();
        this.requestJournal = options.requestJournalDisabled() ? new DisabledRequestJournal() : new InMemoryRequestJournal(
                options.maxRequestJournalEntries());
        this.stubMappings = new InMemoryStubMappings(
                options.extensionsOfType(RequestMatcherExtension.class),
                options.extensionsOfType(ResponseDefinitionTransformer.class),
                fileSource);
        this.nearMissCalculator = new NearMissCalculator(this.stubMappings, this.requestJournal);
        this.recorder = new Recorder(this);
        this.container = container;
        this.loadDefaultMappings();
    }

    public WireMockApp(
            final boolean browserProxyingEnabled,
            final MappingsLoader defaultMappingsLoader,
            final MappingsSaver mappingsSaver,
            final boolean requestJournalDisabled,
            final Optional<Integer> maxRequestJournalEntries,
            final Map<String, ResponseDefinitionTransformer> transformers,
            final Map<String, RequestMatcherExtension> requestMatchers,
            final FileSource rootFileSource,
            final Container container) {

        this.browserProxyingEnabled = browserProxyingEnabled;
        this.defaultMappingsLoader = defaultMappingsLoader;
        this.mappingsSaver = mappingsSaver;
        this.globalSettingsHolder = new GlobalSettingsHolder();
        this.requestJournal = requestJournalDisabled ? new DisabledRequestJournal() : new InMemoryRequestJournal(maxRequestJournalEntries);
        this.stubMappings = new InMemoryStubMappings(requestMatchers, transformers, rootFileSource);
        this.container = container;
        this.nearMissCalculator = new NearMissCalculator(this.stubMappings, this.requestJournal);
        this.recorder = new Recorder(this);
        this.loadDefaultMappings();
    }

    public AdminRequestHandler buildAdminRequestHandler() {
        final AdminRoutes adminRoutes = AdminRoutes.defaultsPlus(
                this.options.extensionsOfType(AdminApiExtension.class).values(),
                this.options.getNotMatchedRenderer()
        );
        return new AdminRequestHandler(
                adminRoutes,
                this,
                new BasicResponseRenderer(),
                this.options.getAdminAuthenticator(),
                this.options.getHttpsRequiredForAdminApi()
        );
    }

    public StubRequestHandler buildStubRequestHandler() {
        final Map<String, PostServeAction> postServeActions = this.options.extensionsOfType(PostServeAction.class);
        return new StubRequestHandler(
                this,
                new StubResponseRenderer(
                        this.options.filesRoot().child(WireMockApp.FILES_ROOT),
                        this.getGlobalSettingsHolder(),
                        new ProxyResponseRenderer(
                                this.options.proxyVia(),
                                this.options.httpsSettings().trustStore(),
                                this.options.shouldPreserveHostHeader(),
                                this.options.proxyHostHeader()
                        ),
                        ImmutableList.copyOf(this.options.extensionsOfType(ResponseTransformer.class).values())
                ),
                this,
                postServeActions,
                this.requestJournal
        );
    }

    public GlobalSettingsHolder getGlobalSettingsHolder() {
        return this.globalSettingsHolder;
    }

    private void loadDefaultMappings() {
        this.loadMappingsUsing(this.defaultMappingsLoader);
    }

    public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
        mappingsLoader.loadMappingsInto(this.stubMappings);
    }

    @Override
    public ServeEvent serveStubFor(final Request request) {
        final ServeEvent serveEvent = this.stubMappings.serveFor(request);

        if (serveEvent.isNoExactMatch()) {
            final LoggedRequest loggedRequest = LoggedRequest.createFrom(request);
            if (request.isBrowserProxyRequest() && this.browserProxyingEnabled) {
                return ServeEvent.of(loggedRequest, ResponseDefinition.browserProxy(request));
            }

            this.logUnmatchedRequest(loggedRequest);
        }

        return serveEvent;
    }

    private void logUnmatchedRequest(final LoggedRequest request) {
        final List<NearMiss> nearest = this.nearMissCalculator.findNearestTo(request);
        String message = "Request was not matched:\n" + request;
        if (!nearest.isEmpty()) {
            message += "\nClosest match:\n" + nearest.get(0).getStubMapping().getRequest();
        }
        notifier().error(message);
    }

    @Override
    public void addStubMapping(final StubMapping stubMapping) {
        this.stubMappings.addMapping(stubMapping);
        if (stubMapping.shouldBePersisted()) {
            this.mappingsSaver.save(stubMapping);
        }

        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    @Override
    public void removeStubMapping(final StubMapping stubMapping) {
        this.stubMappings.removeMapping(stubMapping);
        if (stubMapping.shouldBePersisted()) {
            this.mappingsSaver.remove(stubMapping);
        }

        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    @Override
    public void editStubMapping(final StubMapping stubMapping) {
        this.stubMappings.editMapping(stubMapping);
        if (stubMapping.shouldBePersisted()) {
            this.mappingsSaver.save(stubMapping);
        }

        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    @Override
    public ListStubMappingsResult listAllStubMappings() {
        return new ListStubMappingsResult(LimitAndOffsetPaginator.none(this.stubMappings.getAll()));
    }

    @Override
    public SingleStubMappingResult getStubMapping(final UUID id) {
        return SingleStubMappingResult.fromOptional(this.stubMappings.get(id));
    }

    @Override
    public void saveMappings() {
        this.mappingsSaver.save(this.stubMappings.getAll());
    }

    @Override
    public void resetAll() {
        this.resetToDefaultMappings();
    }

    @Override
    public void resetRequests() {
        this.requestJournal.reset();

        WebSocketEndpoint.broadcast(Message.UNMATCHED);
        WebSocketEndpoint.broadcast(Message.MATCHED);
    }

    @Override
    public void resetToDefaultMappings() {
        this.stubMappings.reset();
        this.resetRequests();
        this.loadDefaultMappings();

        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    @Override
    public void resetScenarios() {
        this.stubMappings.resetScenarios();
    }

    @Override
    public void resetMappings() {
        this.mappingsSaver.removeAll();
        this.stubMappings.reset();

        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    @Override
    public GetServeEventsResult getServeEvents() {
        try {
            return GetServeEventsResult.requestJournalEnabled(
                    LimitAndOffsetPaginator.none(this.requestJournal.getAllServeEvents())
            );
        } catch (final RequestJournalDisabledException e) {
            return GetServeEventsResult.requestJournalDisabled(
                    LimitAndOffsetPaginator.none(this.requestJournal.getAllServeEvents())
            );
        }
    }

    @Override
    public SingleServedStubResult getServedStub(final UUID id) {
        return SingleServedStubResult.fromOptional(this.requestJournal.getServeEvent(id));
    }

    @Override
    public VerificationResult countRequestsMatching(final RequestPattern requestPattern) {
        try {
            return VerificationResult.withCount(this.requestJournal.countRequestsMatching(requestPattern));
        } catch (final RequestJournalDisabledException e) {
            return VerificationResult.withRequestJournalDisabled();
        }
    }

    @Override
    public FindRequestsResult findRequestsMatching(final RequestPattern requestPattern) {
        try {
            final List<LoggedRequest> requests = this.requestJournal.getRequestsMatching(requestPattern);
            return FindRequestsResult.withRequests(requests);
        } catch (final RequestJournalDisabledException e) {
            return FindRequestsResult.withRequestJournalDisabled();
        }
    }

    @Override
    public FindRequestsResult findUnmatchedRequests() {
        try {
            final List<LoggedRequest> requests =
                    from(this.requestJournal.getAllServeEvents())
                            .filter(NOT_MATCHED)
                            .transform(TO_LOGGED_REQUEST)
                            .toList();
            return FindRequestsResult.withRequests(requests);
        } catch (final RequestJournalDisabledException e) {
            return FindRequestsResult.withRequestJournalDisabled();
        }
    }

    @Override
    public FindNearMissesResult findNearMissesForUnmatchedRequests() {
        final ImmutableList.Builder<NearMiss> listBuilder = ImmutableList.builder();
        final Iterable<ServeEvent> unmatchedServeEvents =
                from(this.requestJournal.getAllServeEvents())
                        .filter(new Predicate<ServeEvent>() {
                            @Override
                            public boolean apply(final ServeEvent input) {
                                return input.isNoExactMatch();
                            }
                        });

        for (final ServeEvent serveEvent : unmatchedServeEvents) {
            listBuilder.addAll(this.nearMissCalculator.findNearestTo(serveEvent.getRequest()));
        }

        return new FindNearMissesResult(listBuilder.build());
    }

    @Override
    public GetScenariosResult getAllScenarios() {
        return new GetScenariosResult(
                this.stubMappings.getAllScenarios()
        );
    }

    @Override
    public FindNearMissesResult findTopNearMissesFor(final LoggedRequest loggedRequest) {
        return new FindNearMissesResult(this.nearMissCalculator.findNearestTo(loggedRequest));
    }

    @Override
    public FindNearMissesResult findTopNearMissesFor(final RequestPattern requestPattern) {
        return new FindNearMissesResult(this.nearMissCalculator.findNearestTo(requestPattern));
    }

    @Override
    public void updateGlobalSettings(final GlobalSettings newSettings) {
        this.globalSettingsHolder.replaceWith(newSettings);
    }

    public int port() {
        return this.container.port();
    }

    @Override
    public Options getOptions() {
        return this.options;
    }

    @Override
    public void shutdownServer() {
        this.container.shutdown();
    }

    @Override
    public SnapshotRecordResult snapshotRecord() {
        return this.snapshotRecord(RecordSpec.DEFAULTS);
    }

    @Override
    public SnapshotRecordResult snapshotRecord(final RecordSpecBuilder spec) {
        return this.snapshotRecord(spec.build());
    }

    @Override
    public SnapshotRecordResult snapshotRecord(final RecordSpec recordSpec) {
        return this.recorder.takeSnapshot(this.getServeEvents().getServeEvents(), recordSpec);
    }

    @Override
    public void startRecording(final String targetBaseUrl) {
        this.recorder.startRecording(RecordSpec.forBaseUrl(targetBaseUrl));
    }

    @Override
    public void startRecording(final RecordSpec recordSpec) {
        this.recorder.startRecording(recordSpec);
    }

    @Override
    public void startRecording(final RecordSpecBuilder recordSpec) {
        this.recorder.startRecording(recordSpec.build());
    }

    @Override
    public SnapshotRecordResult stopRecording() {
        return this.recorder.stopRecording();
    }

    @Override
    public RecordingStatusResult getRecordingStatus() {
        return new RecordingStatusResult(this.recorder.getStatus().name());
    }
}
