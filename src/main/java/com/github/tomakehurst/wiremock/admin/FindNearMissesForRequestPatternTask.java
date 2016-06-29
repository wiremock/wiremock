package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;

public class FindNearMissesForRequestPatternTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        RequestPattern requestPattern = Json.read(request.getBodyAsString(), RequestPattern.class);
        FindNearMissesResult nearMissesResult = admin.findTopNearMissesFor(requestPattern);
        return ResponseDefinition.okForJson(nearMissesResult);
    }
}
