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
package com.github.tomakehurst.wiremock.message.sse;

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessageHeader;
import com.github.tomakehurst.wiremock.message.MessageValidator;
import java.util.List;

public class SseMessageValidator implements MessageValidator {

  public static final SseMessageValidator INSTANCE = new SseMessageValidator();

  @Override
  public void validate(MessageDefinition message) {
    for (String key : List.of(SseMessageChannel.EVENT_HEADER, SseMessageChannel.ID_HEADER)) {
      MessageHeader header = message.getHeaders().getHeader(key);
      if (header.isPresent()) {
        for (String value : header.values()) {
          rejectLineBreaks("headers/" + key, "SSE header '" + key + "'", value);
        }
      }
    }
    if (message.getBody().isPlainInlineString()) {
      Object data = message.getBody().getData();
      if (data instanceof String text) {
        rejectLineBreaks("body", "SSE message body", text);
      }
    }
  }

  private static void rejectLineBreaks(String pointer, String description, String value) {
    if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
      throw new InvalidInputException(
          Errors.single(
              10, pointer, "Invalid SSE message", description + " must not contain line breaks"));
    }
  }
}
