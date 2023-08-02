/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.trafficlistener;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ConsoleNotifyingWiremockNetworkTrafficListener
    implements WiremockNetworkTrafficListener {

  private final WiremockNetworkTrafficListener wiremockNetworkTrafficListener;

  public ConsoleNotifyingWiremockNetworkTrafficListener(Charset charset) {
    this.wiremockNetworkTrafficListener =
        WiremockNetworkTrafficListeners.createConsoleNotifying(charset);
  }

  public ConsoleNotifyingWiremockNetworkTrafficListener() {
    this.wiremockNetworkTrafficListener = WiremockNetworkTrafficListeners.createConsoleNotifying();
  }

  @Override
  public void opened(Socket socket) {
    wiremockNetworkTrafficListener.opened(socket);
  }

  @Override
  public void incoming(Socket socket, ByteBuffer bytes) {
    wiremockNetworkTrafficListener.incoming(socket, bytes);
  }

  @Override
  public void outgoing(Socket socket, ByteBuffer bytes) {
    wiremockNetworkTrafficListener.outgoing(socket, bytes);
  }

  @Override
  public void closed(Socket socket) {
    wiremockNetworkTrafficListener.closed(socket);
  }
}
