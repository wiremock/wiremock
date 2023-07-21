/*
 * Copyright (C) 2023 Thomas Akehurst
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

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.common.Notifier;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class NotifyingWiremockNetworkTrafficListenerTest {
  private final Notifier mockNotifier = mock(Notifier.class);

  @Test
  public void opened_withSocket_shouldNotifyAtInfo() {
    NotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new NotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
    Socket socket = new Socket();

    consoleNotifyingWiremockNetworkTrafficListener.opened(socket);

    verify(mockNotifier).info(contains("Opened "));
  }

  @Test
  public void closed_withSocket_shouldNotifyAtInfo() {
    NotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new NotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
    Socket socket = new Socket();

    consoleNotifyingWiremockNetworkTrafficListener.closed(socket);

    verify(mockNotifier).info(contains("Closed "));
  }

  @Test
  public void incoming_withBytebufferWithIncompatibleCharset_shouldNotifyBytesOmittedAtInfo() {
    NotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new NotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
    Socket socket = new Socket();
    ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_16);

    consoleNotifyingWiremockNetworkTrafficListener.incoming(socket, byteBuffer);

    verify(mockNotifier).error(contains("Incoming bytes omitted."));
  }

  @Test
  public void incoming_withBytebufferWithCompatibleCharset_shouldNotifyWithIncomingBytes() {
    NotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new NotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
    Socket socket = new Socket();
    ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_8);

    consoleNotifyingWiremockNetworkTrafficListener.incoming(socket, byteBuffer);

    verify(mockNotifier).info(contains("Hello world"));
  }

  @Test
  public void outgoing_withBytebufferWithIncompatibleCharset_shouldNotifyBytesOmittedAtInfo() {
    NotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new NotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
    Socket socket = new Socket();
    ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_16);

    consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

    verify(mockNotifier).error(contains("Outgoing bytes omitted."));
  }

  @Test
  public void outgoing_withBytebufferWithCompatibleCharset_shouldNotifyWithIncomingBytes() {
    NotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new NotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
    Socket socket = new Socket();
    ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_8);

    consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

    verify(mockNotifier).info(contains("Hello world"));
  }

  public static ByteBuffer stringToByteBuffer(String msg, Charset charset) {
    return ByteBuffer.wrap(msg.getBytes(charset));
  }
}
