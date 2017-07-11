package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.admin.model.SnapshotSpec;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static java.net.HttpURLConnection.HTTP_OK;

public class SnapshotTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        SnapshotSpec snapshotSpec = request.getBody().length == 0
            ? SnapshotSpec.DEFAULTS
            : Json.read(request.getBodyAsString(), SnapshotSpec.class);

        SnapshotRecordResult result = admin.takeSnapshotRecording(snapshotSpec);
        return jsonResponse(result, HTTP_OK);
    }
}
