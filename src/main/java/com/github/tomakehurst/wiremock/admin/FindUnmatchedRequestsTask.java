package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static java.net.HttpURLConnection.HTTP_OK;

public class FindUnmatchedRequestsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        FindRequestsResult unmatchedRequests = admin.findUnmatchedRequests();
        return responseDefinition()
            .withStatus(HTTP_OK)
            .withBody(Json.write(unmatchedRequests))
            .withHeader("Content-Type", "application/json")
            .build();
    }
}
