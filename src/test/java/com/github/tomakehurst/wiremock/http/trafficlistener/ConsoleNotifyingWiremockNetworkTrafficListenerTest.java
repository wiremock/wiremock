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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class ConsoleNotifyingWiremockNetworkTrafficListenerTest {

  @Test
  public void defaultConstructor_notifiesToSystemOutAndUsesUTF8Charset() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new ConsoleNotifyingWiremockNetworkTrafficListener();
    Socket socket = new Socket();
    ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_8);

    consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

    assertThat(out.toString(), containsString("Hello world"));

    System.setOut(originalOut);
  }

  @Test
  public void charsetConstructor_notifiesToSystemOutAndUsesSpecifiedCharset() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener =
        new ConsoleNotifyingWiremockNetworkTrafficListener(StandardCharsets.UTF_16);
    Socket socket = new Socket();
    ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_16);

    consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

    assertThat(out.toString(), containsString("Hello world"));

    System.setOut(originalOut);
  }

  public static ByteBuffer stringToByteBuffer(String msg, Charset charset) {
    return ByteBuffer.wrap(msg.getBytes(charset));
  }
}
