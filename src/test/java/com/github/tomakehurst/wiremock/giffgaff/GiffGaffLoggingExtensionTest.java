package com.github.tomakehurst.wiremock.giffgaff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class GiffGaffLoggingExtensionTest {

  private static final String TRACE_ID_HEADER = "X-B3-TraceId";
  private static final String SPAN_ID_HEADER = "X-B3-SpanId";
  private static final String TRACE_JSON_VALUE = "traceId";
  private static final String SPAN_JSON_VALUE = "spanId";

  @Test
  void mdcUsesLogbackAdapter() {

    Request request =
        new MockRequestBuilder()
            .build();

    sendRequestToExtension(request);

    assertNotNull(MDC.getMDCAdapter());
    assertEquals(MDC.getMDCAdapter().getClass().getSimpleName(), "LogbackMDCAdapter");
  }

  @Test
  void mdcStoresTraceId() {

    Request request =
        new MockRequestBuilder()
            .withHeader(TRACE_ID_HEADER, "abc-123")
            .build();

    sendRequestToExtension(request);

    assertEquals("abc-123", MDC.get(TRACE_JSON_VALUE));
  }

  @Test
  void mdcStoresSpanId() {

    Request request =
        new MockRequestBuilder()
            .withHeader(SPAN_ID_HEADER, "def-456")
            .build();

    sendRequestToExtension(request);

    assertEquals("def-456", MDC.get(SPAN_JSON_VALUE));
  }

  private void sendRequestToExtension(Request request) {
    GiffGaffLoggingExtension giffGaffLoggingExtension = new GiffGaffLoggingExtension();
    giffGaffLoggingExtension.filter(request);
  }
}
