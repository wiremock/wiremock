package com.github.tomakehurst.wiremock.extension.requestfilter;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.http.Request;

public interface RequestFilter extends Extension {

    RequestFilterAction filter(Request request);

    boolean applyToAdmin();
    boolean applyToStubs();

}
