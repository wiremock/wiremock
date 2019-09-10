package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.verification.FindServeEventsResult;

public class RemoveServeEventsByStubMetadataTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        StringValuePattern metadataPattern = Json.read(request.getBodyAsString(), StringValuePattern.class);
        FindServeEventsResult findServeEventsResult = admin.removeServeEventsForStubsMatchingMetadata(metadataPattern);
        return ResponseDefinition.okForJson(findServeEventsResult);
    }
}
