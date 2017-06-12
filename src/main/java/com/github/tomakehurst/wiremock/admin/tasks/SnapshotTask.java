package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;


public class SnapshotTask implements AdminTask {
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        SnapshotSpec snapshotSpec = request.getBody().length == 0
            ? new SnapshotSpec()
            : Json.read(request.getBodyAsString(), SnapshotSpec.class);
        return execute(admin, snapshotSpec);
    }

    private ResponseDefinition execute(Admin admin, SnapshotSpec snapshotSpec) {
        final Iterable<ServeEvent> serveEvents = filterServeEvents(
            admin.getServeEvents(),
            snapshotSpec.getFilters()
        );

        Iterable<StubMapping> stubMappings = new SnapshotStubMappingGenerator(
            snapshotSpec.getCaptureHeaders()
        ).generateFrom(serveEvents);

        stubMappings = from(stubMappings).filter(noDupes(admin));

        final ArrayList<Object> response = new ArrayList<>();

        for (StubMapping stubMapping : stubMappings) {
            if (snapshotSpec.shouldPersist()) {
                stubMapping.setPersistent(true);
                admin.addStubMapping(stubMapping);
            }
            response.add(snapshotSpec.getOutputFormat().format(stubMapping));
        }

        return jsonResponse(response.toArray(), HTTP_OK);
    }

    private Iterable<ServeEvent> filterServeEvents(
        GetServeEventsResult serveEventsResult,
        ServeEventRequestFilters snapshotFilters
    ) {
        FluentIterable<ServeEvent> serveEvents = from(serveEventsResult.getServeEvents())
            .filter(onlyProxied());

        if (snapshotFilters != null) {
            serveEvents = serveEvents.filter(snapshotFilters);
        }

        return serveEvents;
    }

    private Predicate<StubMapping> noDupes(final Admin admin) {
        return new Predicate<StubMapping>() {
            @Override
            public boolean apply(StubMapping stubMapping) {
                return admin.countRequestsMatching(stubMapping.getRequest()).getCount() == 0;
            }
        };
    }

    private Predicate<ServeEvent> onlyProxied() {
        return new Predicate<ServeEvent>() {
            @Override
            public boolean apply(ServeEvent serveEvent) {
                return serveEvent.getResponseDefinition().isProxyResponse();
            }
        };
    }
}
