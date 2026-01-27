/*
 * Copyright (C) 2012-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.admin.LimitAndOffsetPaginator;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterV2;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.HttpStubServeEventListener;
import com.github.tomakehurst.wiremock.message.MessageChannels;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.MessageStubMappings;
import com.github.tomakehurst.wiremock.message.MessageStubRequestHandler;
import com.github.tomakehurst.wiremock.message.RequestInitiatedMessageChannel;
import com.github.tomakehurst.wiremock.recording.*;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.store.DefaultStores;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.stubbing.*;
import com.github.tomakehurst.wiremock.verification.*;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import com.jayway.jsonpath.spi.cache.NOOPCache;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.wiremock.url.Segment;

public class WireMockApp implements StubServer, Admin {

  public static final String FILES_ROOT = "__files";
  public static final Segment ADMIN_CONTEXT_ROOT_SEGMENT = Segment.parse("__admin");
  public static final String ADMIN_CONTEXT_ROOT = "/" + ADMIN_CONTEXT_ROOT_SEGMENT;
  public static final String MAPPINGS_ROOT = "mappings";
  public static final String MESSAGE_MAPPINGS_ROOT = "message-mappings";
  private static final AtomicBoolean FACTORIES_LOADING_OPTIMIZED = new AtomicBoolean(false);

  private final Stores stores;
  private final Scenarios scenarios;
  private final StubMappings stubMappings;
  private final RequestJournal requestJournal;
  private final MessageJournal messageJournal;
  private final SettingsStore settingsStore;
  private final boolean browserProxyingEnabled;
  private final MappingsLoader defaultMappingsLoader;
  private final Container container;
  private final MappingsSaver mappingsSaver;
  private final NearMissCalculator nearMissCalculator;
  private final Recorder recorder;
  private final List<GlobalSettingsListener> globalSettingsListeners;
  private final Map<String, MappingsLoaderExtension> mappingsLoaderExtensions;
  private final Map<String, ServeEventListener> serveEventListeners;
  private final MessageChannels messageChannels;
  private final MessageStubMappings messageStubMappings;

  private Options options;

  private Extensions extensions;

  public WireMockApp(Options options, Container container) {
    if (!options.getDisableOptimizeXmlFactoriesLoading()
        && Boolean.FALSE.equals(FACTORIES_LOADING_OPTIMIZED.get())) {
      Xml.optimizeFactoriesLoading();
      FACTORIES_LOADING_OPTIMIZED.set(true);
    }

    try {
      // Disabling JsonPath's cache due to
      // https://github.com/json-path/JsonPath/issues/975#issuecomment-1867293053 and the fact that
      // we're now doing our own caching.
      CacheProvider.setCache(new NOOPCache());
    } catch (JsonPathException ignored) {
      // May fail on subsequent runs, but this doesn't matter
    }

    this.options = options;
    this.stores = options.getStores();
    this.stores.start();

    this.browserProxyingEnabled = options.browserProxySettings().enabled();
    this.defaultMappingsLoader = options.mappingsLoader();
    this.mappingsSaver = options.mappingsSaver();

    this.settingsStore = stores.getSettingsStore();

    extensions =
        new Extensions(
            options.getDeclaredExtensions(),
            this,
            options,
            stores,
            options.filesRoot().child(FILES_ROOT));
    extensions.load();

    Map<String, RequestMatcherExtension> customMatchers =
        extensions.ofType(RequestMatcherExtension.class);

    requestJournal =
        options.requestJournalDisabled()
            ? new DisabledRequestJournal()
            : new StoreBackedRequestJournal(
                options.maxRequestJournalEntries().orElse(null),
                customMatchers,
                stores.getRequestJournalStore());

    messageJournal =
        options.requestJournalDisabled()
            ? new DisabledMessageJournal()
            : new StoreBackedMessageJournal(
                options.maxRequestJournalEntries().orElse(null), stores.getMessageJournalStore());

    this.messageChannels = new MessageChannels(stores.getMessageChannelStore());
    this.messageStubMappings = new MessageStubMappings(stores.getMessageStubMappingStore());

    HttpStubServeEventListener httpStubListener =
        new HttpStubServeEventListener(
            messageStubMappings,
            messageChannels,
            stores,
            customMatchers,
            List.copyOf(extensions.ofType(MessageActionTransformer.class).values()));
    Map<String, ServeEventListener> extensionListeners =
        extensions.ofType(ServeEventListener.class);
    Map<String, ServeEventListener> combinedListeners = new HashMap<>(extensionListeners);
    combinedListeners.put(httpStubListener.getName(), httpStubListener);
    serveEventListeners = Collections.unmodifiableMap(combinedListeners);

    scenarios = new InMemoryScenarios(stores.getScenariosStore());
    stubMappings =
        new StoreBackedStubMappings(
            stores.getStubStore(),
            scenarios,
            customMatchers,
            extensions.ofType(ResponseDefinitionTransformer.class),
            extensions.ofType(ResponseDefinitionTransformerV2.class),
            stores.getFilesBlobStore(),
            List.copyOf(extensions.ofType(StubLifecycleListener.class).values()),
            serveEventListeners,
            extensions);
    nearMissCalculator =
        new NearMissCalculator(stubMappings, requestJournal, scenarios, customMatchers, extensions);
    recorder =
        new Recorder(this, extensions, stores.getFilesBlobStore(), stores.getRecorderStateStore());
    globalSettingsListeners = List.copyOf(extensions.ofType(GlobalSettingsListener.class).values());
    this.mappingsLoaderExtensions = extensions.ofType(MappingsLoaderExtension.class);

    this.container = container;
    extensions.startAll();
    loadDefaultMappings();
  }

  public WireMockApp(
      boolean browserProxyingEnabled,
      MappingsLoader defaultMappingsLoader,
      Map<String, MappingsLoaderExtension> mappingsLoaderExtensions,
      MappingsSaver mappingsSaver,
      boolean requestJournalDisabled,
      Integer maxRequestJournalEntries,
      Map<String, ResponseDefinitionTransformer> transformers,
      Map<String, ResponseDefinitionTransformerV2> v2transformers,
      Map<String, RequestMatcherExtension> requestMatchers,
      FileSource rootFileSource,
      Container container) {

    this.stores = new DefaultStores(rootFileSource);

    this.browserProxyingEnabled = browserProxyingEnabled;
    this.defaultMappingsLoader = defaultMappingsLoader;
    this.mappingsLoaderExtensions = mappingsLoaderExtensions;
    this.mappingsSaver = mappingsSaver;
    this.settingsStore = stores.getSettingsStore();
    requestJournal =
        requestJournalDisabled
            ? new DisabledRequestJournal()
            : new StoreBackedRequestJournal(
                maxRequestJournalEntries, requestMatchers, stores.getRequestJournalStore());
    messageJournal =
        requestJournalDisabled
            ? new DisabledMessageJournal()
            : new StoreBackedMessageJournal(
                maxRequestJournalEntries, stores.getMessageJournalStore());
    scenarios = new InMemoryScenarios(stores.getScenariosStore());

    this.messageChannels = new MessageChannels(stores.getMessageChannelStore());
    this.messageStubMappings = new MessageStubMappings(stores.getMessageStubMappingStore());

    HttpStubServeEventListener httpStubListener =
        new HttpStubServeEventListener(
            messageStubMappings,
            messageChannels,
            stores,
            requestMatchers,
            List.copyOf(extensions.ofType(MessageActionTransformer.class).values()));
    serveEventListeners = Map.of(httpStubListener.getName(), httpStubListener);

    stubMappings =
        new StoreBackedStubMappings(
            stores.getStubStore(),
            scenarios,
            requestMatchers,
            transformers,
            v2transformers,
            stores.getFilesBlobStore(),
            Collections.emptyList(),
            serveEventListeners,
            extensions);
    this.container = container;
    nearMissCalculator =
        new NearMissCalculator(
            stubMappings, requestJournal, scenarios, requestMatchers, extensions);
    recorder =
        new Recorder(this, extensions, stores.getFilesBlobStore(), stores.getRecorderStateStore());
    globalSettingsListeners = Collections.emptyList();
    loadDefaultMappings();
  }

  public AdminRequestHandler buildAdminRequestHandler() {
    AdminRoutes adminRoutes =
        AdminRoutes.forServer(extensions.ofType(AdminApiExtension.class).values(), stores);
    return new AdminRequestHandler(
        adminRoutes,
        this,
        new BasicResponseRenderer(),
        options.getAdminAuthenticator(),
        options.getHttpsRequiredForAdminApi(),
        getAdminRequestFilters(),
        getV2AdminRequestFilters(),
        options.getDataTruncationSettings());
  }

  public StubRequestHandler buildStubRequestHandler() {
    Map<String, PostServeAction> postServeActions = extensions.ofType(PostServeAction.class);
    BrowserProxySettings browserProxySettings = options.browserProxySettings();

    final HttpClientFactory httpClientFactory =
        new StaticExtensionLoader<>(HttpClientFactory.class)
            .setSpecificInstance(options.httpClientFactory())
            .setExtensions(extensions)
            .load();

    final HttpClient reverseProxyClient =
        httpClientFactory.buildHttpClient(options, true, Collections.emptyList(), true);
    final HttpClient forwardProxyClient =
        httpClientFactory.buildHttpClient(
            options,
            browserProxySettings.trustAllProxyTargets(),
            browserProxySettings.trustAllProxyTargets()
                ? Collections.emptyList()
                : browserProxySettings.trustedProxyTargets(),
            false);

    return new StubRequestHandler(
        this,
        new StubResponseRenderer(
            options.getStores().getFilesBlobStore(),
            settingsStore,
            new ProxyResponseRenderer(
                options.shouldPreserveHostHeader(),
                options.proxyHostHeader(),
                settingsStore,
                options.getStubCorsEnabled(),
                options.getSupportedProxyEncodings(),
                reverseProxyClient,
                forwardProxyClient),
            List.copyOf(extensions.ofType(ResponseTransformer.class).values()),
            List.copyOf(extensions.ofType(ResponseTransformerV2.class).values())),
        this,
        postServeActions,
        serveEventListeners,
        requestJournal,
        getStubRequestFilters(),
        getV2StubRequestFilters(),
        options.getStubRequestLoggingDisabled(),
        options.getDataTruncationSettings(),
        options.getNotMatchedRendererFactory().apply(extensions));
  }

  public MessageStubRequestHandler buildMessageStubRequestHandler() {
    return new MessageStubRequestHandler(
        messageStubMappings,
        messageChannels,
        messageJournal,
        stores,
        List.copyOf(extensions.ofType(MessageActionTransformer.class).values()));
  }

  private List<RequestFilter> getAdminRequestFilters() {
    return extensions.ofType(RequestFilter.class).values().stream()
        .filter(RequestFilter::applyToAdmin)
        .collect(Collectors.toList());
  }

  private List<RequestFilterV2> getV2AdminRequestFilters() {
    return extensions.ofType(RequestFilterV2.class).values().stream()
        .filter(RequestFilterV2::applyToAdmin)
        .collect(Collectors.toList());
  }

  private List<RequestFilter> getStubRequestFilters() {
    return extensions.ofType(RequestFilter.class).values().stream()
        .filter(RequestFilter::applyToStubs)
        .collect(Collectors.toList());
  }

  private List<RequestFilterV2> getV2StubRequestFilters() {
    return extensions.ofType(RequestFilterV2.class).values().stream()
        .filter(RequestFilterV2::applyToStubs)
        .collect(Collectors.toList());
  }

  private void loadDefaultMappings() {
    loadMappingsUsing(defaultMappingsLoader);
    loadMessageMappingsUsing(defaultMappingsLoader);
    if (mappingsLoaderExtensions != null) {
      mappingsLoaderExtensions.values().forEach(e -> loadMappingsUsing(e));
      mappingsLoaderExtensions.values().forEach(e -> loadMessageMappingsUsing(e));
    }
  }

  public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
    mappingsLoader.loadMappingsInto(stubMappings);
  }

  public void loadMessageMappingsUsing(final MappingsLoader mappingsLoader) {
    mappingsLoader.loadMessageMappingsInto(messageStubMappings);
  }

  @Override
  public ServeEvent serveStubFor(ServeEvent initialServeEvent) {
    ServeEvent serveEvent = stubMappings.serveFor(initialServeEvent);

    if (serveEvent.isNoExactMatch()
        && browserProxyingEnabled
        && serveEvent.getRequest().isBrowserProxyRequest()
        && getGlobalSettings().getSettings().getProxyPassThrough()) {
      return ServeEvent.ofUnmatched(
          serveEvent.getRequest(), ResponseDefinition.browserProxy(serveEvent.getRequest()));
    }

    return serveEvent;
  }

  @Override
  public void addStubMapping(StubMapping stubMapping) {
    addStubMapping(stubMapping, true);
  }

  /**
   * @param persistNow If true, will save persisted stubs. Otherwise, saving of stubs will be left
   *     to the caller.
   */
  private StubMapping addStubMapping(StubMapping stubMapping, boolean persistNow) {
    if (stubMapping.getId() == null) {
      stubMapping = stubMapping.transform(b -> b.setId(UUID.randomUUID()));
    }

    stubMapping = stubMappings.addMapping(stubMapping);
    if (persistNow && stubMapping.shouldBePersisted()) {
      mappingsSaver.save(stubMapping);
    }

    return stubMapping;
  }

  @Override
  public void removeStubMapping(StubMapping stubMapping) {
    removeStubMapping(stubMapping, true);
  }

  /**
   * @param persistNow If true, will save persisted stubs. Otherwise, saving of stubs will be left
   *     to the caller.
   * @return The removed stub, or null if no stub was removed.
   */
  private StubMapping removeStubMapping(StubMapping stubMapping, boolean persistNow) {
    StubMapping matchedStub = findStubMatching(stubMapping);
    if (matchedStub == null) return null;

    stubMappings.removeMapping(matchedStub);

    if (persistNow && matchedStub.shouldBePersisted()) {
      mappingsSaver.remove(matchedStub.getId());
    }
    return matchedStub;
  }

  /**
   * Attempts to retrieve a stub mapping that matches the provided stub. For a stub to "match", it
   * must either share the same ID or the same request pattern. Matching the stub ID is prioritized
   * over matching the request pattern. In other words, stubs are only checked for matching request
   * patterns if no stubs are found that match the provided stub's ID.
   */
  private StubMapping findStubMatching(StubMapping stubMapping) {
    return stubMappings
        .get(stubMapping.getId())
        .orElseGet(
            () ->
                stubMappings.getAll().stream()
                    .filter(stub -> stub.getRequest().equals(stubMapping.getRequest()))
                    .findFirst()
                    .orElse(null));
  }

  @Override
  public void removeStubMapping(UUID id) {
    stubMappings.get(id).ifPresent(this::removeStubMapping);
  }

  @Override
  public void editStubMapping(StubMapping stubMapping) {
    editStubMapping(stubMapping, true);
  }

  /**
   * @param persistNow If true, will save persisted stubs. Otherwise, saving of stubs will be left
   *     to the caller.
   */
  private StubMapping editStubMapping(StubMapping stubMapping, boolean persistNow) {
    stubMapping = stubMappings.editMapping(stubMapping);
    if (persistNow && stubMapping.shouldBePersisted()) {
      mappingsSaver.save(stubMapping);
    }

    return stubMapping;
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
      stubMappings.editMapping(stubMapping.transform(b -> b.setPersistent(true)));
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
              .filter(ServeEvent::isNoExactMatch)
              .map(ServeEvent::getRequest)
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
    List<NearMiss> nearMisses = new ArrayList<>();
    List<ServeEvent> unmatchedServeEvents =
        requestJournal.getAllServeEvents().stream()
            .filter(ServeEvent::isNoExactMatch)
            .collect(Collectors.toList());

    for (ServeEvent serveEvent : unmatchedServeEvents) {
      nearMisses.addAll(nearMissCalculator.findNearestTo(serveEvent.getRequest()));
    }

    return new FindNearMissesResult(nearMisses);
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

  public Extensions getExtensions() {
    return extensions;
  }

  @Override
  public void shutdownServer() {
    extensions.stopAll();
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

  private Set<UUID> findMatchedStubIds() {
    return requestJournal.getAllServeEvents().stream()
        .filter(event -> event.getStubMapping() != null)
        .map(event -> event.getStubMapping().getId())
        .collect(Collectors.toSet());
  }

  @Override
  public ListStubMappingsResult findUnmatchedStubs() {
    // Collect IDs of stub mappings that have matched at least one request in a HashSet for O(1)
    // lookups so this method is O(n + m), where n is the number of stubs and m is the number of
    // requests in the journal.
    // It'd be slightly more efficient to use IdentityHashMap, but that's error-prone.
    Set<UUID> servedStubIds = findMatchedStubIds();
    List<StubMapping> foundMappings =
        stubMappings.getAll().stream()
            .filter(stub -> !servedStubIds.contains(stub.getId()))
            .collect(Collectors.toList());
    return new ListStubMappingsResult(LimitAndOffsetPaginator.none(foundMappings));
  }

  @Override
  public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
    return new ListStubMappingsResult(
        LimitAndOffsetPaginator.none(stubMappings.findByMetadata(pattern)));
  }

  @Override
  public void removeStubsByMetadata(StringValuePattern pattern) {
    removeStubMappings(stubMappings.findByMetadata(pattern));
  }

  @Override
  public void importStubs(StubImport stubImport) {
    List<StubMapping> mappings = stubImport.getMappings();
    StubImport.Options importOptions =
        getFirstNonNull(stubImport.getImportOptions(), StubImport.Options.DEFAULTS);

    List<StubMapping> mappingsToSave = new ArrayList<>();
    for (int i = mappings.size() - 1; i >= 0; i--) {
      StubMapping mapping = mappings.get(i);
      if (mapping.getId() != null && getStubMapping(mapping.getId()).isPresent()) {
        if (importOptions.getDuplicatePolicy() == StubImport.Options.DuplicatePolicy.OVERWRITE) {
          final StubMapping updatedStubMapping = editStubMapping(mapping, false);
          if (updatedStubMapping.shouldBePersisted()) mappingsToSave.add(updatedStubMapping);
        }
      } else {
        final StubMapping createdStubMapping = addStubMapping(mapping, false);
        if (createdStubMapping.shouldBePersisted()) mappingsToSave.add(createdStubMapping);
      }
    }

    if (importOptions.getDeleteAllNotInImport()) {
      List<UUID> ids = mappings.stream().map(StubMapping::getId).collect(Collectors.toList());
      for (StubMapping mapping : listAllStubMappings().getMappings()) {
        if (!ids.contains(mapping.getId())) {
          removeStubMapping(mapping, false);
        }
      }
      mappingsSaver.setAll(mappingsToSave);
    } else {
      if (!mappingsToSave.isEmpty()) mappingsSaver.save(mappingsToSave);
    }
  }

  @Override
  public void removeStubMappings(List<StubMapping> stubMappings) {
    List<UUID> mappingsToDelete = new ArrayList<>();
    for (StubMapping mapping : stubMappings) {
      var removed = removeStubMapping(mapping, false);
      if (removed != null && removed.shouldBePersisted()) mappingsToDelete.add(removed.getId());
    }
    if (!mappingsToDelete.isEmpty()) mappingsSaver.remove(mappingsToDelete);
  }

  public Set<String> getLoadedExtensionNames() {
    return extensions.getAllExtensionNames();
  }

  @Override
  public SendChannelMessageResult sendChannelMessage(
      ChannelType type, RequestPattern requestPattern, MessageDefinition message) {
    Map<String, RequestMatcherExtension> customMatchers =
        extensions.ofType(RequestMatcherExtension.class);
    List<RequestInitiatedMessageChannel> matchedChannels =
        messageChannels.sendMessageToMatchingByType(type, requestPattern, message, customMatchers);
    List<LoggedMessageChannel> loggedChannels =
        matchedChannels.stream().map(LoggedMessageChannel::createFrom).collect(Collectors.toList());
    return new SendChannelMessageResult(loggedChannels);
  }

  @Override
  public ListMessageChannelsResult listAllMessageChannels() {
    List<LoggedMessageChannel> channels =
        messageChannels.getAll().stream()
            .map(LoggedMessageChannel::createFrom)
            .collect(Collectors.toList());
    return new ListMessageChannelsResult(channels);
  }

  @Override
  public void addMessageStubMapping(MessageStubMapping messageStubMapping) {
    messageStubMappings.add(messageStubMapping);
  }

  @Override
  public void removeMessageStubMapping(UUID id) {
    messageStubMappings.remove(id);
  }

  @Override
  public void resetMessageStubMappings() {
    messageStubMappings.clear();
  }

  @Override
  public ListMessageStubMappingsResult findAllMessageStubsByMetadata(StringValuePattern pattern) {
    return new ListMessageStubMappingsResult(
        LimitAndOffsetPaginator.none(messageStubMappings.findByMetadata(pattern)));
  }

  @Override
  public void removeMessageStubsByMetadata(StringValuePattern pattern) {
    List<MessageStubMapping> toRemove = messageStubMappings.findByMetadata(pattern);
    for (MessageStubMapping stub : toRemove) {
      messageStubMappings.remove(stub.getId());
    }
  }

  @Override
  public ListMessageStubMappingsResult listAllMessageStubMappings() {
    return new ListMessageStubMappingsResult(
        LimitAndOffsetPaginator.none(messageStubMappings.getAll()));
  }

  @Override
  public GetMessageServeEventsResult getMessageServeEvents() {
    try {
      return GetMessageServeEventsResult.messageJournalEnabled(
          messageJournal.getAllMessageServeEvents());
    } catch (MessageJournalDisabledException e) {
      return GetMessageServeEventsResult.messageJournalDisabled();
    }
  }

  @Override
  public SingleMessageServeEventResult getMessageServeEvent(UUID id) {
    return SingleMessageServeEventResult.fromOptional(messageJournal.getMessageServeEvent(id));
  }

  @Override
  public int countMessageEventsMatching(MessagePattern pattern) {
    return messageJournal.countEventsMatching(pattern);
  }

  @Override
  public List<MessageServeEvent> findMessageEventsMatching(MessagePattern pattern) {
    return messageJournal.getEventsMatching(pattern);
  }

  @Override
  public void removeMessageServeEvent(UUID eventId) {
    messageJournal.removeEvent(eventId);
  }

  @Override
  public FindMessageServeEventsResult removeMessageServeEventsMatching(MessagePattern pattern) {
    return new FindMessageServeEventsResult(messageJournal.removeEventsMatching(pattern));
  }

  @Override
  public FindMessageServeEventsResult removeMessageServeEventsForStubsMatchingMetadata(
      StringValuePattern pattern) {
    return new FindMessageServeEventsResult(
        messageJournal.removeEventsForStubsMatchingMetadata(pattern));
  }

  @Override
  public void resetMessageJournal() {
    messageJournal.reset();
  }

  @Override
  public Optional<MessageServeEvent> waitForMessageEvent(MessagePattern pattern, Duration maxWait) {
    return messageJournal.waitForEvent(pattern, maxWait);
  }

  @Override
  public List<MessageServeEvent> waitForMessageEvents(
      MessagePattern pattern, int count, Duration maxWait) {
    return messageJournal.waitForEvents(pattern, count, maxWait);
  }
}
