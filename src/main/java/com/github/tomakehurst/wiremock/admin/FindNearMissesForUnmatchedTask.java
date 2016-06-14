package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;

public class FindNearMissesForUnmatchedTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        FindNearMissesResult nearMissesResult = admin.findNearMissesForUnmatchedRequests();
        return ResponseDefinition.okForJson(nearMissesResult);
    }
}
