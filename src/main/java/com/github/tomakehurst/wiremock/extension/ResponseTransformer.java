package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

public abstract class ResponseTransformer extends AbstractTransformer<Response> {

    @Override
    public abstract Response transform(Request request, Response response, FileSource files, Parameters parameters);

}
