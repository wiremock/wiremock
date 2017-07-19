package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.InvalidRequestException;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.recording.RecordSpec;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;

public class StartRecordingTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        RecordSpec recordSpec = Json.read(request.getBodyAsString(), RecordSpec.class);
        try {
            admin.startRecording(recordSpec);
            return ResponseDefinition.okEmptyJson();
        } catch (InvalidRequestException e) {
            return jsonResponse(e.getErrors(), 422);
        }
    }
}
