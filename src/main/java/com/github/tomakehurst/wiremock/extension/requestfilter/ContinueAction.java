package com.github.tomakehurst.wiremock.extension.requestfilter;

import com.github.tomakehurst.wiremock.http.Request;

public class ContinueAction extends RequestFilterAction {

    private final Request request;

    ContinueAction(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
