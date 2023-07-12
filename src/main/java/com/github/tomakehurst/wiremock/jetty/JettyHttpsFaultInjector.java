/*
 * Copyright (C) 2014-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;

public class JettyHttpsFaultInjector implements FaultInjector {

  private static final byte[] GARBAGE = "lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes(UTF_8);

  private final Response response;
  private final Socket socket;

  public JettyHttpsFaultInjector(HttpServletResponse response) {
    this.response = JettyUtils.unwrapResponse(response);
    this.socket = JettyUtils.getTlsSocket(this.response);
  }

  @Override
  public void connectionResetByPeer() {
    try {
      socket.setSoLinger(true, 0);
      socket.close();
    } catch (IOException e) {
      throwUnchecked(e);
    }
  }

  @Override
  public void emptyResponseAndCloseConnection() {
    try {
      socket.close();
    } catch (IOException e) {
      throwUnchecked(e);
    }
  }

  @Override
  public void malformedResponseChunk() {
    try {
      response.setStatus(200);
      response.flushBuffer();
      writeGarbageThenCloseSocket();
    } catch (IOException e) {
      throwUnchecked(e);
    }
  }

  @Override
  public void randomDataAndCloseConnection() {
    writeGarbageThenCloseSocket();
  }

  private void writeGarbageThenCloseSocket() {
    response
        .getHttpOutput()
        .getHttpChannel()
        .getEndPoint()
        .write(
            new Callback() {
              @Override
              public void succeeded() {
                try {
                  socket.close();
                } catch (IOException e) {
                  notifier().error("Failed to close socket after Garbage write succeeded", e);
                }
              }

              @Override
              public void failed(Throwable x) {
                try {
                  socket.close();
                } catch (IOException e) {
                  notifier().error("Failed to close socket after Garbage write failed", e);
                }
              }
            },
            BufferUtil.toBuffer(GARBAGE));
  }
}
