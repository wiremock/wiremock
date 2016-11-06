package com.github.tomakehurst.wiremock.admin.tasks;

public class GetRamlSpecTask extends AbstractGetDocTask {

    @Override
    protected String getMimeType() {
        return "application/raml+yaml";
    }

    @Override
    protected String getFilePath() {
        return "raml/wiremock-admin-api.raml";
    }
}
