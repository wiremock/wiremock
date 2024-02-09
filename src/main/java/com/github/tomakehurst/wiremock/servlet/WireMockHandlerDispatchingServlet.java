/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.servlet;

import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_LENGTH;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy.BODY_FILE;
import static com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy.NEVER;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.servlet.WireMockHttpServletRequestAdapter.ORIGINAL_REQUEST_KEY;
import static com.github.tomakehurst.wiremock.stubbing.ServeEvent.ORIGINAL_SERVE_EVENT_KEY;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.FaultInjector;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.jetty.JettyUtils;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class WireMockHandlerDispatchingServlet extends HttpServlet {

  public static final String SHOULD_FORWARD_TO_FILES_CONTEXT = "shouldForwardToFilesContext";
  public static final String ASYNCHRONOUS_RESPONSE_EXECUTOR =
      WireMockHandlerDispatchingServlet.class.getSimpleName() + ".asynchronousResponseExecutor";
  public static final String MAPPED_UNDER_KEY = "mappedUnder";

  private static final long serialVersionUID = -6602042274260495538L;

  private ScheduledExecutorService scheduledExecutorService;

  private RequestHandler requestHandler;
  private FaultInjectorFactory faultHandlerFactory;
  private String mappedUnder;
  private Notifier notifier;
  private String wiremockFileSourceRoot = "/";
  private boolean shouldForwardToFilesContext;
  private MultipartRequestConfigurer multipartRequestConfigurer;
  private Options.ChunkedEncodingPolicy chunkedEncodingPolicy;
  private boolean browserProxyingEnabled;

  @Override
  public void init(ServletConfig config) {
    ServletContext context = config.getServletContext();
    shouldForwardToFilesContext = getFileContextForwardingFlagFrom(config);

    if (context.getInitParameter("WireMockFileSourceRoot") != null) {
      wiremockFileSourceRoot = context.getInitParameter("WireMockFileSourceRoot");
    }

    scheduledExecutorService =
        (ScheduledExecutorService) context.getAttribute(ASYNCHRONOUS_RESPONSE_EXECUTOR);

    String handlerClassName = config.getInitParameter(RequestHandler.HANDLER_CLASS_KEY);
    String faultInjectorFactoryClassName =
        config.getInitParameter(FaultInjectorFactory.INJECTOR_CLASS_KEY);
    mappedUnder = getNormalizedMappedUnder(config);
    context.log(
        RequestHandler.HANDLER_CLASS_KEY
            + " from context returned "
            + handlerClassName
            + ". Normalized mapped under returned '"
            + mappedUnder
            + "'");
    requestHandler = (RequestHandler) context.getAttribute(handlerClassName);

    faultHandlerFactory =
        faultInjectorFactoryClassName != null
            ? (FaultInjectorFactory) context.getAttribute(faultInjectorFactoryClassName)
            : new NoFaultInjectorFactory();

    notifier = (Notifier) context.getAttribute(Notifier.KEY);

    multipartRequestConfigurer =
        (MultipartRequestConfigurer) context.getAttribute(MultipartRequestConfigurer.KEY);

    Object chunkedEncodingPolicyAttr =
        context.getAttribute(Options.ChunkedEncodingPolicy.class.getName());
    chunkedEncodingPolicy =
        chunkedEncodingPolicyAttr != null
            ? (Options.ChunkedEncodingPolicy) chunkedEncodingPolicyAttr
            : Options.ChunkedEncodingPolicy.ALWAYS;

    browserProxyingEnabled =
        Boolean.parseBoolean(
            getFirstNonNull(context.getAttribute("browserProxyingEnabled"), "false").toString());
  }

  private String getNormalizedMappedUnder(ServletConfig config) {
    String mappedUnder = config.getInitParameter(MAPPED_UNDER_KEY);
    if (mappedUnder == null) {
      return null;
    }
    if (mappedUnder.endsWith("/")) {
      mappedUnder = mappedUnder.substring(0, mappedUnder.length() - 1);
    }
    return mappedUnder;
  }

  private boolean getFileContextForwardingFlagFrom(ServletConfig config) {
    String flagValue = config.getInitParameter(SHOULD_FORWARD_TO_FILES_CONTEXT);
    return Boolean.parseBoolean(flagValue);
  }

  @Override
  protected void service(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    LocalNotifier.set(notifier);

    // TODO: The HTTP/1.x CONNECT is also forwarded to the servlet now. To keep backward
    // compatible behavior (with proxy involved), skipping the CONNECT handling altogether.
    if (Objects.equals(httpServletRequest.getMethod(), "CONNECT")) {
      return;
    }

    Request request =
        new WireMockHttpServletRequestAdapter(
            httpServletRequest, multipartRequestConfigurer, mappedUnder, browserProxyingEnabled);

    ServletHttpResponder responder =
        new ServletHttpResponder(httpServletRequest, httpServletResponse);

    final ServeEvent originalServeEvent =
        httpServletRequest.getAttribute(ORIGINAL_SERVE_EVENT_KEY) != null
            ? (ServeEvent) httpServletRequest.getAttribute(ORIGINAL_SERVE_EVENT_KEY)
            : null;

    requestHandler.handle(request, responder, originalServeEvent);
  }

  private class ServletHttpResponder implements HttpResponder {

    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    public ServletHttpResponder(
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      this.httpServletRequest = httpServletRequest;
      this.httpServletResponse = httpServletResponse;
    }

    @Override
    public void respond(
        final Request request, final Response response, Map<String, Object> attributes) {
      if (Thread.currentThread().isInterrupted()) {
        return;
      }

      httpServletRequest.setAttribute(ORIGINAL_REQUEST_KEY, LoggedRequest.createFrom(request));
      attributes.forEach(httpServletRequest::setAttribute);

      if (isAsyncSupported(response, httpServletRequest)) {
        respondAsync(request, response);
      } else {
        respondSync(request, response);
      }
    }

    private void respondSync(Request request, Response response) {
      delayIfRequired(response.getInitialDelay());
      respondTo(request, response);
    }

    private void delayIfRequired(long delayMillis) {
      try {
        MILLISECONDS.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    private boolean isAsyncSupported(Response response, HttpServletRequest httpServletRequest) {
      return scheduledExecutorService != null
          && response.getInitialDelay() > 0
          && httpServletRequest.isAsyncSupported();
    }

    private void respondAsync(final Request request, final Response response) {
      final AsyncContext asyncContext = httpServletRequest.startAsync();
      scheduledExecutorService.schedule(
          () -> {
            try {
              respondTo(request, response);
            } finally {
              asyncContext.complete();
            }
          },
          response.getInitialDelay(),
          MILLISECONDS);
    }

    private void respondTo(Request request, Response response) {
      try {
        if (response.wasConfigured()) {
          applyResponse(response, httpServletRequest, httpServletResponse);
        } else if (request.getMethod().equals(GET) && shouldForwardToFilesContext) {
          forwardToFilesContext(httpServletRequest, httpServletResponse, request);
        } else {
          httpServletResponse.sendError(HTTP_NOT_FOUND);
        }
      } catch (Exception e) {
        throwUnchecked(e);
      }
    }
  }

  public void applyResponse(
      Response response,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse) {
    Fault fault = response.getFault();
    if (fault != null) {
      FaultInjector faultInjector = buildFaultInjector(httpServletRequest, httpServletResponse);
      fault.apply(faultInjector);
      httpServletResponse.addHeader(Fault.class.getName(), fault.name());
      return;
    }

    if (response.getStatusMessage() == null) {
      httpServletResponse.setStatus(response.getStatus());
    } else {
      // The Jetty 11 does not implement HttpServletResponse::setStatus and always sets the
      // reason as `null`, the workaround using
      // org.eclipse.jetty.server.Response::setStatusWithReason
      // still works.
      if (httpServletResponse instanceof org.eclipse.jetty.server.Response) {
        final org.eclipse.jetty.server.Response jettyResponse =
            (org.eclipse.jetty.server.Response) httpServletResponse;
        jettyResponse.setStatusWithReason(response.getStatus(), response.getStatusMessage());
      } else if (JettyUtils.isServlet6()) {
        // Servet 6.0 specification does not allow to set the status message anymore
        httpServletResponse.setStatus(response.getStatus());
      } else {
        httpServletResponse.setStatus(response.getStatus(), response.getStatusMessage());
      }
    }

    for (HttpHeader header : response.getHeaders().all()) {
      for (String value : header.values()) {
        httpServletResponse.addHeader(header.key(), value);
      }
    }

    if ((chunkedEncodingPolicy == NEVER
            || (chunkedEncodingPolicy == BODY_FILE && response.hasInlineBody()))
        && httpServletResponse.getHeader(CONTENT_LENGTH) == null) {
      httpServletResponse.setContentLength(response.getBody().length);
    }

    if (response.shouldAddChunkedDribbleDelay()) {
      writeAndTranslateExceptionsWithChunkedDribbleDelay(
          httpServletResponse, response.getBodyStream(), response.getChunkedDribbleDelay());
    } else {
      writeAndTranslateExceptions(httpServletResponse, response.getBodyStream());
    }
  }

  private FaultInjector buildFaultInjector(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    return faultHandlerFactory.buildFaultInjector(httpServletRequest, httpServletResponse);
  }

  private static void writeAndTranslateExceptions(
      HttpServletResponse httpServletResponse, InputStream content) {
    try (ServletOutputStream out = httpServletResponse.getOutputStream()) {
      content.transferTo(out);
      out.flush();
    } catch (IOException e) {
      throwUnchecked(e);
    } finally {
      try {
        content.close();
      } catch (IOException e) {
        // well, we tried
      }
    }
  }

  private void writeAndTranslateExceptionsWithChunkedDribbleDelay(
      HttpServletResponse httpServletResponse,
      InputStream bodyStream,
      ChunkedDribbleDelay chunkedDribbleDelay) {
    try (ServletOutputStream out = httpServletResponse.getOutputStream()) {
      byte[] body = bodyStream.readAllBytes();

      if (body.length < 1) {
        notifier.error("Cannot chunk dribble delay when no body set");
        out.flush();
        return;
      }

      byte[][] chunkedBody = BodyChunker.chunkBody(body, chunkedDribbleDelay.getNumberOfChunks());

      int chunkInterval = chunkedDribbleDelay.getTotalDuration() / chunkedBody.length;

      for (byte[] bodyChunk : chunkedBody) {
        Thread.sleep(chunkInterval);
        out.write(bodyChunk);
        out.flush();
      }

    } catch (IOException e) {
      throwUnchecked(e);
    } catch (InterruptedException ignored) {
      // Ignore the interrupt quietly since it's probably the client timing out, which is a
      // completely valid outcome
    }
  }

  private void forwardToFilesContext(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      Request request)
      throws ServletException, IOException {
    String forwardUrl = wiremockFileSourceRoot + WireMockApp.FILES_ROOT + request.getUrl();
    RequestDispatcher dispatcher =
        httpServletRequest.getRequestDispatcher(decode(forwardUrl, UTF_8));
    dispatcher.forward(httpServletRequest, httpServletResponse);
  }
}
