package com.tomakehurst.wiremock.common;

import com.tomakehurst.wiremock.mapping.Response;

public class LoggingExceptionHandler implements ExceptionHandler {
    
    private final Notifier notifier;
    
    public LoggingExceptionHandler(final Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public Response handle(final Exception ex) {
        notifier.error(ex);
        return Response.error(ex);
    }

}
