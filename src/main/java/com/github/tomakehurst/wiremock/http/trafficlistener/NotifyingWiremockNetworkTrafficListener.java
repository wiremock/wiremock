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

import com.github.tomakehurst.wiremock.common.Notifier;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

final class NotifyingWiremockNetworkTrafficListener implements WiremockNetworkTrafficListener {

  private final Notifier notifier;
  private final Charset charset;
  private final CharsetDecoder charsetDecoder;

  NotifyingWiremockNetworkTrafficListener(Notifier notifier, Charset charset) {
    this.notifier = notifier;
    this.charset = charset;
    this.charsetDecoder = charset.newDecoder();
  }

  @Override
  public void opened(Socket socket) {
    notifier.info("Opened " + socket);
  }

  @Override
  public void incoming(Socket socket, ByteBuffer bytes) {
    try {
      notifier.info("Incoming bytes: " + charsetDecoder.decode(bytes));
    } catch (CharacterCodingException e) {
      notifier.error("Incoming bytes omitted. Could not decode with charset: " + charset);
    }
  }

  @Override
  public void outgoing(Socket socket, ByteBuffer bytes) {
    try {
      notifier.info("Outgoing bytes: " + charsetDecoder.decode(bytes));
    } catch (CharacterCodingException e) {
      notifier.error("Outgoing bytes omitted. Could not decode with charset: " + charset);
    }
  }

  @Override
  public void closed(Socket socket) {
    notifier.info("Closed " + socket);
  }
}
