package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class GetCaCertTask implements AdminTask {

    private static final ResponseDefinition NOT_SUPPORTED_RESPONSE = new ResponseDefinition(HTTP_NOT_FOUND, "HTTPS Browser Proxying, including CA certificate retrieval, requires wiremock-jre8");

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        return NOT_SUPPORTED_RESPONSE;
    }
}
