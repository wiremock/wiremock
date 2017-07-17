package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.indexOf;
import static com.google.common.collect.Lists.newLinkedList;

public class Recorder {

    private final Admin admin;
    private State state;

    public Recorder(Admin admin) {
        this.admin = admin;
        state = State.initial();
    }

    public synchronized void startRecording(String targetBaseUrl) {
        StubMapping proxyMapping = proxyAllTo(targetBaseUrl).build();
        admin.addStubMapping(proxyMapping);

        List<ServeEvent> serveEvents = admin.getServeEvents().getServeEvents();
        UUID initialId = serveEvents.isEmpty() ? null : serveEvents.get(0).getId();
        state = state.start(initialId, proxyMapping);
    }

    public synchronized SnapshotRecordResult stopRecording() {
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

        return takeSnapshot(eventsToSnapshot, SnapshotSpec.DEFAULTS);
    }

    private static Predicate<ServeEvent> withId(final UUID id) {
        return new Predicate<ServeEvent>() {
            @Override
            public boolean apply(ServeEvent input) {
                return input.getId().equals(id);
            }
        };
    }

    public SnapshotRecordResult takeSnapshot(List<ServeEvent> serveEvents, SnapshotSpec snapshotSpec) {
        final List<StubMapping> stubMappings = serveEventsToStubMappings(
            Lists.reverse(serveEvents),
            snapshotSpec.getFilters(),
            new SnapshotStubMappingGenerator(snapshotSpec.getCaptureHeaders(), snapshotSpec.getJsonMatchingFlags()),
            getStubMappingPostProcessor(admin.getOptions(), snapshotSpec)
        );

        for (StubMapping stubMapping : stubMappings) {
            if (snapshotSpec.shouldPersist()) {
                stubMapping.setPersistent(true);
                admin.addStubMapping(stubMapping);
            }
        }

        return snapshotSpec.getOutputFormat().format(stubMappings);
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

    public SnapshotStubMappingPostProcessor getStubMappingPostProcessor(Options options, SnapshotSpec snapshotSpec) {
        final SnapshotStubMappingTransformerRunner transformerRunner = new SnapshotStubMappingTransformerRunner(
            options.extensionsOfType(StubMappingTransformer.class).values(),
            snapshotSpec.getTransformers(),
            snapshotSpec.getTransformerParameters(),
            options.filesRoot()
        );

        return new SnapshotStubMappingPostProcessor(
            snapshotSpec.shouldRecordRepeatsAsScenarios(),
            transformerRunner,
            snapshotSpec.getExtractBodyCriteria(),
            new SnapshotStubMappingBodyExtractor(options.filesRoot())
        );
    }

    private static class State {

        enum Status { NeverStarted, Recording, Stopped }

        private final Status status;
        private final StubMapping proxyMapping;
        private final UUID startingServeEventId;
        private final UUID finishingServeEventId;

        public State(Status status, StubMapping proxyMapping, UUID startingServeEventId, UUID finishingServeEventId) {
            this.status = status;
            this.proxyMapping = proxyMapping;
            this.startingServeEventId = startingServeEventId;
            this.finishingServeEventId = finishingServeEventId;
        }

        public static State initial() {
            return new State(Status.NeverStarted, null, null, null);
        }

        public State start(UUID startingServeEventId, StubMapping proxyMapping) {
            return new State(Status.Recording, proxyMapping, startingServeEventId, null);
        }

        public State stop(UUID finishingServeEventId) {
            return new State(Status.Stopped, proxyMapping, startingServeEventId, finishingServeEventId);
        }

        public Status getStatus() {
            return status;
        }

        public StubMapping getProxyMapping() {
            return proxyMapping;
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
