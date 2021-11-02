/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.direct;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An implementation of the {@link HttpServer} that doesn't actually run an HTTP server.
 *
 * <p>This is to allow the use of Wiremock through direct method calls, which is then suitable for i.e. running in Serverless applications.
 */
public class DirectCallHttpServer implements HttpServer {
    private static final long DEFAULT_TIMEOUT = 1000;

    private final AdminRequestHandler adminRequestHandler;
    private final StubRequestHandler stubRequestHandler;
    private final long timeout;
    private final SleepFacade sleepFacade;

    /**
     * Construct the Direct Call HTTP Server.
     *
     * @param options             the {@link Options} used to configure this server
     * @param adminRequestHandler the {@link AdminRequestHandler}
     * @param stubRequestHandler  the {@link StubRequestHandler}
     */
    public DirectCallHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        this.sleepFacade = new SleepFacade();
        this.timeout = getTimeout(options);
        this.adminRequestHandler = adminRequestHandler;
        this.stubRequestHandler = stubRequestHandler;
    }

    DirectCallHttpServer(SleepFacade sleepFacade, Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        this.sleepFacade = sleepFacade;
        this.timeout = getTimeout(options);
        this.adminRequestHandler = adminRequestHandler;
        this.stubRequestHandler = stubRequestHandler;
    }

    /**
     * Retrieve the admin response that is mapped for a given request to the server.
     *
     * @param request the incoming admin {@link Request}
     * @return the admin {@link Response}
     */
    public Response adminRequest(Request request) {
        return handleRequest(request, adminRequestHandler);
    }

    /**
     * Retrieve the stub response that is mapped for a given request to the server.
     *
     * @param request the incoming stub {@link Request}
     * @return the stub {@link Response} that best matches the mappings
     */
    public Response stubRequest(Request request) {
        return handleRequest(request, stubRequestHandler);
    }

    @Override
    public void start() {
        // no implementation, as this is a stub
    }

    @Override
    public void stop() {
        // no implementation, as this is a stub
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public int port() {
        return -1;
    }

    @Override
    public int httpsPort() {
        return -2;
    }

    private Response handleRequest(Request request, AbstractRequestHandler handler) {
        CompletableFuture<Response> responseFuture = new CompletableFuture<>();
        handler.handle(request, (ignored, response) -> responseFuture.complete(response));

        try {
            Response response = responseFuture.get(timeout, TimeUnit.MILLISECONDS);
            if (response.getInitialDelay() != 0) {
                sleepFacade.sleep(response.getInitialDelay());
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Could not retrieve response from the Stub Handler", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Could not retrieve response from the Stub Handler", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException(String.format("The request was not handled within the timeout of %dms", timeout), e);
        }
    }

    private static long getTimeout(Options options) {
        if (options.jettySettings().getStopTimeout().isPresent()) {
            return options.jettySettings().getStopTimeout().get();
        } else {
            return DEFAULT_TIMEOUT;
        }
    }
}
