package com.github.tomakehurst.wiremock.extension.requestfilter;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class StopAction extends RequestFilterAction {

    private final ResponseDefinition responseDefinition;

    StopAction(ResponseDefinition responseDefinition) {
        this.responseDefinition = responseDefinition;
    }

    public ResponseDefinition getResponseDefinition() {
        return responseDefinition;
    }
}
