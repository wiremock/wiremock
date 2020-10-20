package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.FindServeEventsResult;

public class RemoveServeEventsByRequestPatternTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        RequestPattern requestPattern = Json.read(request.getBodyAsString(), RequestPattern.class);
        FindServeEventsResult findServeEventsResult = admin.removeServeEventsMatching(requestPattern);
        return ResponseDefinition.okForJson(findServeEventsResult);
    }
}
