package com.github.tomakehurst.wiremock.giffgaff;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Request;
import org.slf4j.MDC;

public class GiffGaffLoggingExtension extends StubRequestFilter {

  private static final String TRACE_ID_HEADER = "X-B3-TraceId";
  private static final String SPAN_ID_HEADER = "X-B3-SpanId";
  private static final String TRACE_JSON_VALUE = "traceId";
  private static final String SPAN_JSON_VALUE = "spanId";

  @Override
  public String getName() {
    return "giffgaff-logging-extension";
  }

  @Override
  public RequestFilterAction filter(Request request) {
    MDC.put(TRACE_JSON_VALUE, request.getHeader(TRACE_ID_HEADER));
    MDC.put(SPAN_JSON_VALUE, request.getHeader(SPAN_ID_HEADER));
    return RequestFilterAction.continueWith(request);
  }
}
