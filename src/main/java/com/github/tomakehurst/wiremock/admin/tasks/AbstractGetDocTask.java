package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.io.Resources;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

public abstract class AbstractGetDocTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        try {
            byte[] content = toByteArray(Resources.getResource(getFilePath()).openStream());
            return responseDefinition()
                .withStatus(200)
                .withBody(content)
                .withHeader(CONTENT_TYPE, getMimeType())
                .build();
        } catch (IOException e) {
            return responseDefinition().withStatus(500).build();
        }
    }

    protected abstract String getMimeType();
    protected abstract String getFilePath();
}
