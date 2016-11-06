package com.github.tomakehurst.wiremock.admin.tasks;

public class GetDocIndexTask extends AbstractGetDocTask {

    @Override
    protected String getMimeType() {
        return "text/html";
    }

    @Override
    protected String getFilePath() {
        return "doc-index.html";
    }
}
