package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.GetServedStubsResult;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static java.net.HttpURLConnection.HTTP_OK;

public class GetAllRequestsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        GetServedStubsResult servedStubsResult = admin.getServedStubs();
        GetServedStubsResult result = new GetServedStubsResult(
            LimitAndSinceDatePaginator.fromRequest(
                servedStubsResult.getServedStubs(),
                request
            ),
            servedStubsResult.isRequestJournalDisabled()
        );

        return responseDefinition()
            .withStatus(HTTP_OK)
            .withBody(Json.write(result))
            .withHeader("Content-Type", "application/json")
            .build();
    }
}
