package com.github.tomakehurst.wiremock.extension.requestfilter;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class RequestFilterAction {

    public static RequestFilterAction continueWith(Request request) {
        return new ContinueAction(request);
    }

    public static RequestFilterAction stopWith(ResponseDefinition responseDefinition) {
        return new StopAction(responseDefinition);
    }
}
