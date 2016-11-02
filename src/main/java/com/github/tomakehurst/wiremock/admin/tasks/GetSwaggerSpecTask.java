package com.github.tomakehurst.wiremock.admin.tasks;

public class GetSwaggerSpecTask extends AbstractGetDocTask {

    @Override
    protected String getMimeType() {
        return "application/json";
    }

    @Override
    protected String getFilePath() {
        return "swagger/wiremock-admin-api.json";
    }
}
