package com.tomakehurst.wiremock.common;

import com.tomakehurst.wiremock.mapping.Response;

public class DoNothingExceptionHandler implements ExceptionHandler {

    @Override
    public Response handle(final Exception ex) {
        return Response.error(ex);
    }

}
