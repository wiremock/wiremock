/*
 * Copyright (C) 2016-2022 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class ConsoleNotifyingWiremockNetworkTrafficListener
    implements WiremockNetworkTrafficListener {
  private static final ConsoleNotifier CONSOLE_NOTIFIER = new ConsoleNotifier(true);

  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final CharsetDecoder DECODER_DEFAULT = CHARSET.newDecoder();
  private static final CharsetDecoder DECODER_REPLACE = CHARSET.newDecoder()
      .onMalformedInput(CodingErrorAction.REPLACE)
      .onUnmappableCharacter(CodingErrorAction.REPLACE);

  @Override
  public void opened(Socket socket) {
    CONSOLE_NOTIFIER.info("Opened " + socket);
  }

  @Override
  public void incoming(Socket socket, ByteBuffer bytes) {
    notifyBytes("incoming", bytes);
  }

  @Override
  public void outgoing(Socket socket, ByteBuffer bytes) {
    notifyBytes("outgoing", bytes);
  }

  @Override
  public void closed(Socket socket) {
    CONSOLE_NOTIFIER.info("Closed " + socket);
  }

  public static String decodeBytes(ByteBuffer bytes) throws CharacterCodingException {
    try {
      return DECODER_DEFAULT.decode(bytes).toString();
    } catch (CharacterCodingException e) {
      return "(erroneous bytes dropped) " + DECODER_REPLACE.decode(bytes).toString();
    }
  }

  private void notifyBytes(String type, ByteBuffer bytes) {
    try {
      CONSOLE_NOTIFIER.info(type + " bytes: " + decodeBytes(bytes));
    } catch (CharacterCodingException e) {
      CONSOLE_NOTIFIER.error("Problem decoding " + type + " network traffic. ", e);
    }
  }
}
