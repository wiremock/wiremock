package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;

public class MissingMultipart implements Request.Part {

    @Override
    public String getName() {
        return "[request is not multipart]";
    }

    @Override
    public HttpHeader getHeader(String name) {
        return HttpHeader.absent(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders();
    }

    @Override
    public Body getBody() {
        return null;
    }
}
