package com.github.tomakehurst.wiremock.admin.tasks;

public class GetRecordingsIndexTask extends AbstractGetDocTask {

    @Override
    protected String getMimeType() {
        return "text/html";
    }

    @Override
    protected String getFilePath() {
        return "recorder-index.html";
    }
}
