package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public interface ResponseTransformer extends Extension {

    ResponseDefinition transform(Request request, ResponseDefinition responseDefinition);
}
