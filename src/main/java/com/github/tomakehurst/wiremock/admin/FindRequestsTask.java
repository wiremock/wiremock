package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.buildRequestPatternFrom;
import static java.net.HttpURLConnection.HTTP_OK;

public class FindRequestsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        RequestPattern requestPattern = buildRequestPatternFrom(request.getBodyAsString());
        FindRequestsResult result = admin.findRequestsMatching(requestPattern);
        ResponseDefinition response = new ResponseDefinition(HTTP_OK, Json.write(result));
        response.setHeaders(new HttpHeaders(httpHeader("Content-Type", "application/json")));
        return response;
    }
}
