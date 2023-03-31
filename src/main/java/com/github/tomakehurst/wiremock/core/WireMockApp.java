/*
 * Copyright (C) 2012-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.stubbing.ServeEvent.NOT_MATCHED;
import static com.github.tomakehurst.wiremock.stubbing.ServeEvent.TO_LOGGED_REQUEST;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Iterables.contains;

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.admin.LimitAndOffsetPaginator;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.*;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.store.DefaultStores;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.stubbing.*;
import com.github.tomakehurst.wiremock.verification.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WireMockApp implements StubServer, Admin {

  public static final String FILES_ROOT = "__files";
  public static final String ADMIN_CONTEXT_ROOT = "/__admin";
  public static final String MAPPINGS_ROOT = "mappings";
  private static final MutableBoolean FACTORIES_LOADING_OPTIMIZED = new MutableBoolean(false);

  private final Stores stores;
  private final Scenarios scenarios;
  private final StubMappings stubMappings;
  private final RequestJournal requestJournal;
  private final SettingsStore settingsStore;
  private final boolean browserProxyingEnabled;
  private final MappingsLoader defaultMappingsLoader;
  private final Container container;
  private final MappingsSaver mappingsSaver;
  private final NearMissCalculator nearMissCalculator;
  private final Recorder recorder;
  private final List<GlobalSettingsListener> globalSettingsListeners;

  private Options options;

  public WireMockApp(Options options, Container container) {
    if (!options.getDisableOptimizeXmlFactoriesLoading() && FACTORIES_LOADING_OPTIMIZED.isFalse()) {
      Xml.optimizeFactoriesLoading();
      FACTORIES_LOADING_OPTIMIZED.setTrue();
    }

    this.options = options;
    this.stores = options.getStores();
    this.stores.start();

    this.browserProxyingEnabled = options.browserProxySettings().enabled();
    this.defaultMappingsLoader = options.mappingsLoader();
    this.mappingsSaver = options.mappingsSaver();

    this.settingsStore = stores.getSettingsStore();

    Map<String, RequestMatcherExtension> customMatchers =
        options.extensionsOfType(RequestMatcherExtension.class);

    requestJournal =
        options.requestJournalDisabled()
            ? new DisabledRequestJournal()
            : new StoreBackedRequestJournal(
                options.maxRequestJournalEntries(),
                customMatchers,
                stores.getRequestJournalStore());

    scenarios = new InMemoryScenarios(stores.getScenariosStore());
    stubMappings =
        new StoreBackedStubMappings(
            stores.getStubStore(),
            scenarios,
            customMatchers,
            options.extensionsOfType(ResponseDefinitionTransformer.class),
            stores.getFilesBlobStore(),
            ImmutableList.copyOf(options.extensionsOfType(StubLifecycleListener.class).values()));
    nearMissCalculator = new NearMissCalculator(stubMappings, requestJournal, scenarios);
    recorder = new Recorder(this, stores.getRecorderStateStore());
    globalSettingsListeners =
        ImmutableList.copyOf(options.extensionsOfType(GlobalSettingsListener.class).values());

    this.container = container;
    loadDefaultMappings();
  }

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

    this.stores = new DefaultStores(rootFileSource);

    this.browserProxyingEnabled = browserProxyingEnabled;
    this.defaultMappingsLoader = defaultMappingsLoader;
    this.mappingsSaver = mappingsSaver;
    this.settingsStore = stores.getSettingsStore();
    requestJournal =
        requestJournalDisabled
            ? new DisabledRequestJournal()
            : new StoreBackedRequestJournal(
                maxRequestJournalEntries, requestMatchers, stores.getRequestJournalStore());
    scenarios = new InMemoryScenarios(stores.getScenariosStore());
    stubMappings =
        new StoreBackedStubMappings(
            stores.getStubStore(),
            scenarios,
            requestMatchers,
            transformers,
            stores.getFilesBlobStore(),
            Collections.<StubLifecycleListener>emptyList());
    this.container = container;
    nearMissCalculator = new NearMissCalculator(stubMappings, requestJournal, scenarios);
    recorder = new Recorder(this, stores.getRecorderStateStore());
    globalSettingsListeners = Collections.emptyList();
    loadDefaultMappings();
  }

  public AdminRequestHandler buildAdminRequestHandler() {
    AdminRoutes adminRoutes =
        AdminRoutes.forServer(
            options.extensionsOfType(AdminApiExtension.class).values(),
            options.getNotMatchedRenderer(),
            stores);
    return new AdminRequestHandler(
        adminRoutes,
        this,
        new BasicResponseRenderer(),
        options.getAdminAuthenticator(),
        options.getHttpsRequiredForAdminApi(),
        getAdminRequestFilters(),
        options.getDataTruncationSettings());
  }

  public StubRequestHandler buildStubRequestHandler() {
    Map<String, PostServeAction> postServeActions = options.extensionsOfType(PostServeAction.class);
    BrowserProxySettings browserProxySettings = options.browserProxySettings();
    return new StubRequestHandler(
        this,
        new StubResponseRenderer(
            options.getStores().getFilesBlobStore(),
            settingsStore,
            new ProxyResponseRenderer(
                options.proxyVia(),
                options.httpsSettings().trustStore(),
                options.shouldPreserveHostHeader(),
                options.proxyHostHeader(),
                settingsStore,
                browserProxySettings.trustAllProxyTargets(),
                browserProxySettings.trustedProxyTargets(),
                options.getStubCorsEnabled(),
                options.getProxyTargetRules(),
                options.proxyTimeout()),
            ImmutableList.copyOf(options.extensionsOfType(ResponseTransformer.class).values())),
        this,
        postServeActions,
        requestJournal,
        getStubRequestFilters(),
        options.getStubRequestLoggingDisabled(),
        options.getDataTruncationSettings());
  }

  private List<RequestFilter> getAdminRequestFilters() {
    return options.extensionsOfType(RequestFilter.class).values().stream()
        .filter(RequestFilter::applyToAdmin)
        .collect(Collectors.toList());
  }

  private List<RequestFilter> getStubRequestFilters() {
    return options.extensionsOfType(RequestFilter.class).values().stream()
        .filter(RequestFilter::applyToStubs)
        .collect(Collectors.toList());
  }

  private void loadDefaultMappings() {
    loadMappingsUsing(defaultMappingsLoader);
  }

  public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
    mappingsLoader.loadMappingsInto(stubMappings);
  }

  @Override
  public ServeEvent serveStubFor(Request request) {
    ServeEvent serveEvent = stubMappings.serveFor(request);

    if (serveEvent.isNoExactMatch()) {
      if (browserProxyingEnabled
          && request.isBrowserProxyRequest()
          && getGlobalSettings().getSettings().getProxyPassThrough()) {
        return ServeEvent.of(serveEvent.getRequest(), ResponseDefinition.browserProxy(request));
      }
    }

    return serveEvent;
  }

  @Override
  public void addStubMapping(StubMapping stubMapping) {
    if (stubMapping.getId() == null) {
      stubMapping.setId(UUID.randomUUID());
    }

    stubMappings.addMapping(stubMapping);
    if (stubMapping.shouldBePersisted()) {
      mappingsSaver.save(stubMapping);
    }
  }

  @Override
  public void removeStubMapping(StubMapping stubMapping) {
    stubMappings
        .get(stubMapping.getId())
        .ifPresent(
            stubToDelete -> {
              if (stubToDelete.shouldBePersisted()) {
                mappingsSaver.remove(stubToDelete);
              }
            });

    stubMappings.removeMapping(stubMapping);
  }

  @Override
  public void removeStubMapping(UUID id) {
    stubMappings.get(id).ifPresent(this::removeStubMapping);
  }

  @Override
  public void editStubMapping(StubMapping stubMapping) {
    stubMappings.editMapping(stubMapping);
    if (stubMapping.shouldBePersisted()) {
      mappingsSaver.save(stubMapping);
    }
  }

  @Override
  public ListStubMappingsResult listAllStubMappings() {
    return new ListStubMappingsResult(LimitAndOffsetPaginator.none(stubMappings.getAll()));
  }

  @Override
  public SingleStubMappingResult getStubMapping(UUID id) {
    return SingleStubMappingResult.fromOptional(stubMappings.get(id));
  }

  @Override
  public void saveMappings() {
    for (StubMapping stubMapping : stubMappings.getAll()) {
      stubMapping.setPersistent(true);
      stubMappings.editMapping(stubMapping);
    }
    mappingsSaver.save(stubMappings.getAll());
  }

  @Override
  public void resetAll() {
    resetToDefaultMappings();
  }

  @Override
  public void resetRequests() {
    requestJournal.reset();
  }

  @Override
  public void resetToDefaultMappings() {
    stubMappings.reset();
    resetRequests();
    loadDefaultMappings();
  }

  @Override
  public void resetScenarios() {
    stubMappings.resetScenarios();
  }

  @Override
  public void resetMappings() {
    mappingsSaver.removeAll();
    stubMappings.reset();
  }

  @Override
  public GetServeEventsResult getServeEvents() {
    return getServeEvents(ServeEventQuery.ALL);
  }

  @Override
  public GetServeEventsResult getServeEvents(ServeEventQuery query) {
    try {
      final List<ServeEvent> serveEvents = query.filter(requestJournal.getAllServeEvents());
      return GetServeEventsResult.requestJournalEnabled(LimitAndOffsetPaginator.none(serveEvents));
    } catch (RequestJournalDisabledException e) {
      return GetServeEventsResult.requestJournalDisabled(
          LimitAndOffsetPaginator.none(requestJournal.getAllServeEvents()));
    }
  }

  @Override
  public SingleServedStubResult getServedStub(UUID id) {
    return SingleServedStubResult.fromOptional(requestJournal.getServeEvent(id));
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
          requestJournal.getAllServeEvents().stream()
              .filter(NOT_MATCHED)
              .map(TO_LOGGED_REQUEST)
              .collect(Collectors.toList());
      return FindRequestsResult.withRequests(requests);
    } catch (RequestJournalDisabledException e) {
      return FindRequestsResult.withRequestJournalDisabled();
    }
  }

  @Override
  public void removeServeEvent(UUID eventId) {
    requestJournal.removeEvent(eventId);
  }

  @Override
  public FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern) {
    return new FindServeEventsResult(requestJournal.removeEventsMatching(requestPattern));
  }

  @Override
  public FindServeEventsResult removeServeEventsForStubsMatchingMetadata(
      StringValuePattern metadataPattern) {
    return new FindServeEventsResult(
        requestJournal.removeServeEventsForStubsMatchingMetadata(metadataPattern));
  }

  @Override
  public FindNearMissesResult findNearMissesForUnmatchedRequests() {
    ImmutableList.Builder<NearMiss> listBuilder = ImmutableList.builder();
    List<ServeEvent> unmatchedServeEvents =
        requestJournal.getAllServeEvents().stream()
            .filter(ServeEvent::isNoExactMatch)
            .collect(Collectors.toList());

    for (ServeEvent serveEvent : unmatchedServeEvents) {
      listBuilder.addAll(nearMissCalculator.findNearestTo(serveEvent.getRequest()));
    }

    return new FindNearMissesResult(listBuilder.build());
  }

  @Override
  public GetScenariosResult getAllScenarios() {
    return new GetScenariosResult(stubMappings.getAllScenarios());
  }

  @Override
  public void resetScenario(String name) {
    scenarios.resetSingle(name);
  }

  @Override
  public void setScenarioState(String name, String state) {
    scenarios.setSingle(name, state);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
    return new FindNearMissesResult(nearMissCalculator.findNearestTo(loggedRequest));
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
    return new FindNearMissesResult(nearMissCalculator.findNearestTo(requestPattern));
  }

  @Override
  public GetGlobalSettingsResult getGlobalSettings() {
    return new GetGlobalSettingsResult(settingsStore.get());
  }

  @Override
  public void updateGlobalSettings(GlobalSettings newSettings) {
    GlobalSettings oldSettings = settingsStore.get();

    for (GlobalSettingsListener listener : globalSettingsListeners) {
      listener.beforeGlobalSettingsUpdated(oldSettings, newSettings);
    }

    settingsStore.set(newSettings);

    for (GlobalSettingsListener listener : globalSettingsListeners) {
      listener.afterGlobalSettingsUpdated(oldSettings, newSettings);
    }
  }

  public int port() {
    return container.port();
  }

  @Override
  public Options getOptions() {
    return options;
  }

  @Override
  public void shutdownServer() {
    stores.stop();
    container.shutdown();
  }

  public SnapshotRecordResult snapshotRecord() {
    return snapshotRecord(RecordSpec.DEFAULTS);
  }

  @Override
  public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
    return snapshotRecord(spec.build());
  }

  public SnapshotRecordResult snapshotRecord(RecordSpec recordSpec) {
    return recorder.takeSnapshot(getServeEvents().getServeEvents(), recordSpec);
  }

  @Override
  public void startRecording(String targetBaseUrl) {
    recorder.startRecording(RecordSpec.forBaseUrl(targetBaseUrl));
  }

  @Override
  public void startRecording(RecordSpec recordSpec) {
    recorder.startRecording(recordSpec);
  }

  @Override
  public void startRecording(RecordSpecBuilder recordSpec) {
    recorder.startRecording(recordSpec.build());
  }

  @Override
  public SnapshotRecordResult stopRecording() {
    return recorder.stopRecording();
  }

  @Override
  public RecordingStatusResult getRecordingStatus() {
    return new RecordingStatusResult(recorder.getStatus().name());
  }

  @Override
  public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
    return new ListStubMappingsResult(
        LimitAndOffsetPaginator.none(stubMappings.findByMetadata(pattern)));
  }

  @Override
  public void removeStubsByMetadata(StringValuePattern pattern) {
    List<StubMapping> foundMappings = stubMappings.findByMetadata(pattern);
    for (StubMapping mapping : foundMappings) {
      removeStubMapping(mapping);
    }
  }

  @Override
  public void importStubs(StubImport stubImport) {
    List<StubMapping> mappings = stubImport.getMappings();
    StubImport.Options importOptions =
        firstNonNull(stubImport.getImportOptions(), StubImport.Options.DEFAULTS);

    for (int i = mappings.size() - 1; i >= 0; i--) {
      StubMapping mapping = mappings.get(i);
      if (mapping.getId() != null && getStubMapping(mapping.getId()).isPresent()) {
        if (importOptions.getDuplicatePolicy() == StubImport.Options.DuplicatePolicy.OVERWRITE) {
          editStubMapping(mapping);
        }
      } else {
        addStubMapping(mapping);
      }
    }

    if (importOptions.getDeleteAllNotInImport()) {
      Iterable<UUID> ids = mappings.stream().map(StubMapping::getId).collect(Collectors.toList());
      for (StubMapping mapping : listAllStubMappings().getMappings()) {
        if (!contains(ids, mapping.getId())) {
          removeStubMapping(mapping);
        }
      }
    }
  }
}
