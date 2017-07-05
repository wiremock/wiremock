package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;

public class SnapshotTask implements AdminTask {
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        SnapshotSpec snapshotSpec = request.getBody().length == 0
            ? new SnapshotSpec() // no request body, so use defaults
            : Json.read(request.getBodyAsString(), SnapshotSpec.class);
        return execute(admin, snapshotSpec);
    }

    private ResponseDefinition execute(Admin admin, SnapshotSpec snapshotSpec) {
        final List<StubMapping> stubMappings = serveEventsToStubMappings(
            admin.getServeEvents(),
            snapshotSpec.getFilters(),
            new SnapshotStubMappingGenerator(snapshotSpec.getCaptureHeaders()),
            getStubMappingPostProcessor(admin.getOptions(), snapshotSpec)
        );

        for (StubMapping stubMapping : stubMappings) {
            if (snapshotSpec.shouldPersist()) {
                stubMapping.setPersistent(true);
                admin.addStubMapping(stubMapping);
            }
        }

        return jsonResponse(
            snapshotSpec.getOutputFormatter().format(stubMappings),
            HTTP_OK
        );
    }

    private List<StubMapping> serveEventsToStubMappings(
        GetServeEventsResult serveEventsResult,
        ProxiedServeEventFilters serveEventFilters,
        SnapshotStubMappingGenerator stubMappingGenerator,
        SnapshotStubMappingPostProcessor stubMappingPostProcessor
    ) {
        final Iterable<StubMapping> stubMappings = from(serveEventsResult.getServeEvents())
            .filter(serveEventFilters)
            .transform(stubMappingGenerator);

        return stubMappingPostProcessor.process(stubMappings);
    }

    private SnapshotStubMappingPostProcessor getStubMappingPostProcessor(Options options, SnapshotSpec snapshotSpec) {
        final SnapshotRepeatedRequestHandler repeatedRequestHandler = new SnapshotRepeatedRequestHandler(
            snapshotSpec.shouldRecordRepeatsAsScenarios()
        );
        final SnapshotStubMappingBodyExtractor bodyExtractor = new SnapshotStubMappingBodyExtractor(
            options.filesRoot()
        );
        final SnapshotStubMappingTransformerRunner transformerRunner = new SnapshotStubMappingTransformerRunner(
            options.extensionsOfType(StubMappingTransformer.class).values(),
            snapshotSpec.getTransformers(),
            snapshotSpec.getTransformerParameters(),
            options.filesRoot()
        );

        return new SnapshotStubMappingPostProcessor(
            repeatedRequestHandler,
            transformerRunner,
            snapshotSpec.getExtractBodyCriteria(),
            bodyExtractor
        );
    }
}
