/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty.sse;

import com.github.tomakehurst.wiremock.message.sse.SseSession;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.eclipse.jetty.ee11.servlets.EventSource;
import org.eclipse.jetty.ee11.servlets.EventSourceServlet;

public class JettySseSession implements SseSession {

  private static final SseServletHelper HELPER = new SseServletHelper();

  private final EventSource.Emitter emitter;
  private final AsyncContext asyncContext;
  private volatile boolean open = true;

  public JettySseSession(AsyncContext asyncContext) throws IOException {
    this.asyncContext = asyncContext;
    this.emitter = HELPER.newEmitter(asyncContext);
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void sendEvent(String eventName, String data, String id) {
    if (!open) {
      return;
    }
    try {
      if (eventName != null && data != null) {
        emitter.event(eventName, data);
      } else if (data != null) {
        emitter.data(data);
      }
    } catch (IOException e) {
      open = false;
      completeAsyncContext();
    }
  }

  @Override
  public void close() {
    open = false;
    completeAsyncContext();
  }

  public void comment(String comment) throws IOException {
    if (!open) {
      return;
    }
    try {
      emitter.comment(comment);
    } catch (IOException e) {
      open = false;
      completeAsyncContext();
      throw e;
    }
  }

  private void completeAsyncContext() {
    try {
      asyncContext.complete();
    } catch (IllegalStateException ignored) {
    }
  }

  private static class SseServletHelper extends EventSourceServlet {

    EventSource.Emitter newEmitter(AsyncContext asyncContext) throws IOException {
      return new EventSourceEmitter(NOOP_SOURCE, asyncContext);
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest request) {
      return null;
    }

    private static final EventSource NOOP_SOURCE =
        new EventSource() {
          @Override
          public void onOpen(Emitter emitter) {}

          @Override
          public void onClose() {}
        };
  }
}
