/*
 * Copyright (C) 2021-2023 Thomas Akehurst
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Optional;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectCallHttpServerTest {
  @Mock private SleepFacade sleepFacade;
  @Mock private Options options;
  @Mock private JettySettings jettySettings;
  @Mock private AdminRequestHandler adminRequestHandler;
  @Mock private StubRequestHandler stubRequestHandler;

  private DirectCallHttpServer server;

  @BeforeEach
  void setup() {
    when(options.jettySettings()).thenReturn(jettySettings);
    when(jettySettings.getStopTimeout()).thenReturn(Optional.absent());
    server =
        new DirectCallHttpServer(sleepFacade, options, adminRequestHandler, stubRequestHandler);
  }

  @Nested
  class Constructor {
    @Test
    void publicConstructor() {
      assertDoesNotThrow(
          () -> new DirectCallHttpServer(options, adminRequestHandler, stubRequestHandler));
    }
  }

  @Nested
  class Start {
    @Test
    void doesNothing() {
      assertDoesNotThrow(server::start);
    }
  }

  @Nested
  class Stop {
    @Test
    void doesNothing() {
      assertDoesNotThrow(server::start);
    }
  }

  @Nested
  class IsRunning {
    @Test
    void isAlwaysTrue() {
      assertTrue(server.isRunning());
    }

    @Test
    void isUnaffectedByStop() {
      boolean isRunning = server.isRunning();

      server.stop();

      assertEquals(server.isRunning(), isRunning);
    }
  }

  @Nested
  class Port {
    @Test
    void isInvalidPortNumber() {
      assertEquals(server.port(), -1);
    }
  }

  @Nested
  class HttpsPort {
    @Test
    void isInvalidPortNumber() {
      assertEquals(server.httpsPort(), -2);
    }
  }

  @Nested
  class AdminRequest extends AbstractRequestHandlerTest {

    @Override
    AbstractRequestHandler handler() {
      return adminRequestHandler;
    }

    @Override
    Response handle(Request request) {
      return server.adminRequest(request);
    }
  }

  @Nested
  class StubRequest extends AbstractRequestHandlerTest {

    @Override
    AbstractRequestHandler handler() {
      return stubRequestHandler;
    }

    @Override
    Response handle(Request request) {
      return server.stubRequest(request);
    }
  }

  abstract class AbstractRequestHandlerTest {

    abstract AbstractRequestHandler handler();

    abstract Response handle(Request request);

    @Mock private Request request;
    @Mock private Response response;
    private Response actual;

    @Nested
    class HappyPath {

      @BeforeEach
      void setup() {
        doAnswer(
                (i) -> {
                  HttpResponder responder = i.getArgument(1, HttpResponder.class);
                  responder.respond(request, response);
                  return null;
                })
            .when(handler())
            .handle(any(), any());

        actual = handle(request);
      }

      @Test
      void delegatesRequest() {
        verify(handler()).handle(eq(request), any());
      }

      @Test
      void returnsResponse() {
        assertEquals(response, actual);
      }
    }

    @Nested
    class WhenDelay {

      @Nested
      class WhenFixed {

        @BeforeEach
        void setup() {
          when(response.getInitialDelay()).thenReturn(1000L);
          doAnswer(
                  (i) -> {
                    HttpResponder responder = i.getArgument(1, HttpResponder.class);
                    responder.respond(request, response);
                    return null;
                  })
              .when(handler())
              .handle(any(), any());

          actual = handle(request);
        }

        @Test
        void delegatesRequest() {
          verify(handler()).handle(eq(request), any());
        }

        @Test
        void delegatesDelays() {
          verify(sleepFacade).sleep(1000L);
        }

        @Test
        void returnsResponse() {
          assertEquals(response, actual);
        }
      }

      @Nested
      class WhenRandomDelay {
        @Disabled("Needs to be implemented")
        @Test
        void todo() {
          fail();
        }
      }

      @Disabled("Is not implemented")
      @Nested
      class ChunkedDribbleDelay {}
    }

    @Disabled("Is not implemented")
    @Nested
    class Fault {}

    @Nested
    class AsyncTimeout {
      @BeforeEach
      void setup() {
        when(jettySettings.getStopTimeout()).thenReturn(Optional.of(5L));
        server = new DirectCallHttpServer(options, adminRequestHandler, stubRequestHandler);
      }

      @Test
      void throwsIllegalStateExceptionWhenNoResponse() {
        IllegalStateException actual =
            assertThrows(IllegalStateException.class, () -> handle(request));

        assertEquals("The request was not handled within the timeout of 5ms", actual.getMessage());
        assertTrue(actual.getCause() instanceof TimeoutException);
      }
    }
  }
}
