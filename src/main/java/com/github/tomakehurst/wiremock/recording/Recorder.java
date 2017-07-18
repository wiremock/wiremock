package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.indexOf;

public class Recorder {

    private final Admin admin;
    private State state;

    public Recorder(Admin admin) {
        this.admin = admin;
        state = State.initial();
    }

    public synchronized void startRecording(RecordSpec spec) {
        if (state.getStatus() == RecordingStatus.Recording) {
            return;
        }

        StubMapping proxyMapping = proxyAllTo(spec.getTargetBaseUrl()).build();
        admin.addStubMapping(proxyMapping);

        List<ServeEvent> serveEvents = admin.getServeEvents().getServeEvents();
        UUID initialId = serveEvents.isEmpty() ? null : serveEvents.get(0).getId();
        state = state.start(initialId, proxyMapping, spec);

        notifier().info("Started recording with record spec:\n" + Json.write(spec));
    }

    public synchronized SnapshotRecordResult stopRecording() {
        if (state.getStatus() != RecordingStatus.Recording) {
            throw new NotRecordingException();
        }

        List<ServeEvent> serveEvents = admin.getServeEvents().getServeEvents();

        UUID lastId = serveEvents.isEmpty() ? null : serveEvents.get(0).getId();
        state = state.stop(lastId);
        admin.removeStubMapping(state.getProxyMapping());

        if (serveEvents.isEmpty()) {
            return SnapshotRecordResult.empty();
        }

        int startIndex = state.getStartingServeEventId() == null ?
            serveEvents.size() :
            indexOf(serveEvents, withId(state.getStartingServeEventId()));
        int endIndex = indexOf(serveEvents, withId(state.getFinishingServeEventId()));
        List<ServeEvent> eventsToSnapshot = serveEvents.subList(endIndex, startIndex);

        SnapshotRecordResult result = takeSnapshot(eventsToSnapshot, state.getSpec());

        notifier().info("Stopped recording. Stubs captured:\n" + Json.write(result.getStubMappings()));
        return result;
    }

    private static Predicate<ServeEvent> withId(final UUID id) {
        return new Predicate<ServeEvent>() {
            @Override
            public boolean apply(ServeEvent input) {
                return input.getId().equals(id);
            }
        };
    }

    public SnapshotRecordResult takeSnapshot(List<ServeEvent> serveEvents, RecordSpec recordSpec) {
        final List<StubMapping> stubMappings = serveEventsToStubMappings(
            Lists.reverse(serveEvents),
            recordSpec.getFilters(),
            new SnapshotStubMappingGenerator(recordSpec.getCaptureHeaders(), recordSpec.getJsonMatchingFlags()),
            getStubMappingPostProcessor(admin.getOptions(), recordSpec)
        );

        for (StubMapping stubMapping : stubMappings) {
            if (recordSpec.shouldPersist()) {
                stubMapping.setPersistent(true);
            }
            admin.addStubMapping(stubMapping);
        }

        return recordSpec.getOutputFormat().format(stubMappings);
    }

    public List<StubMapping> serveEventsToStubMappings(
        List<ServeEvent> serveEventsResult,
        ProxiedServeEventFilters serveEventFilters,
        SnapshotStubMappingGenerator stubMappingGenerator,
        SnapshotStubMappingPostProcessor stubMappingPostProcessor
    ) {
        final Iterable<StubMapping> stubMappings = from(serveEventsResult)
            .filter(serveEventFilters)
            .transform(stubMappingGenerator);

        return stubMappingPostProcessor.process(stubMappings);
    }

    public SnapshotStubMappingPostProcessor getStubMappingPostProcessor(Options options, RecordSpec recordSpec) {
        final SnapshotStubMappingTransformerRunner transformerRunner = new SnapshotStubMappingTransformerRunner(
            options.extensionsOfType(StubMappingTransformer.class).values(),
            recordSpec.getTransformers(),
            recordSpec.getTransformerParameters(),
            options.filesRoot()
        );

        return new SnapshotStubMappingPostProcessor(
            recordSpec.shouldRecordRepeatsAsScenarios(),
            transformerRunner,
            recordSpec.getExtractBodyCriteria(),
            new SnapshotStubMappingBodyExtractor(options.filesRoot())
        );
    }

    public RecordingStatus getStatus() {
        return state.getStatus();
    }

    private static class State {

        private final RecordingStatus status;
        private final StubMapping proxyMapping;
        private final RecordSpec spec;
        private final UUID startingServeEventId;
        private final UUID finishingServeEventId;

        public State(RecordingStatus status, StubMapping proxyMapping, RecordSpec spec, UUID startingServeEventId, UUID finishingServeEventId) {
            this.status = status;
            this.proxyMapping = proxyMapping;
            this.spec = spec;
            this.startingServeEventId = startingServeEventId;
            this.finishingServeEventId = finishingServeEventId;
        }

        public static State initial() {
            return new State(RecordingStatus.NeverStarted, null, null, null, null);
        }

        public State start(UUID startingServeEventId, StubMapping proxyMapping, RecordSpec spec) {
            return new State(RecordingStatus.Recording, proxyMapping, spec, startingServeEventId, null);
        }

        public State stop(UUID finishingServeEventId) {
            return new State(RecordingStatus.Stopped, proxyMapping, spec, startingServeEventId, finishingServeEventId);
        }

        public RecordingStatus getStatus() {
            return status;
        }

        public StubMapping getProxyMapping() {
            return proxyMapping;
        }

        public RecordSpec getSpec() {
            return spec;
        }

        public UUID getStartingServeEventId() {
            return startingServeEventId;
        }

        public UUID getFinishingServeEventId() {
            return finishingServeEventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return status == state.status &&
                Objects.equals(proxyMapping, state.proxyMapping) &&
                Objects.equals(startingServeEventId, state.startingServeEventId) &&
                Objects.equals(finishingServeEventId, state.finishingServeEventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, proxyMapping, startingServeEventId, finishingServeEventId);
        }
    }
}
