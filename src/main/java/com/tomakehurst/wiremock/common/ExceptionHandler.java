package com.tomakehurst.wiremock.common;

import com.tomakehurst.wiremock.mapping.Response;

public interface ExceptionHandler {

    Response handle(Exception ex);
}
