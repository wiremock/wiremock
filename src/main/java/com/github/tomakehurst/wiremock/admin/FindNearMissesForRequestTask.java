package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class FindNearMissesForRequestTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        LoggedRequest loggedRequest = Json.read(request.getBodyAsString(), LoggedRequest.class);
        FindNearMissesResult nearMissesResult = admin.findTopNearMissesFor(loggedRequest);
        return ResponseDefinition.okForJson(nearMissesResult);
    }
}
