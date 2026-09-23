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

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.MessageHeader;
import com.github.tomakehurst.wiremock.message.MessageHeaders;
import com.github.tomakehurst.wiremock.message.RequestInitiatedChannelTarget;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.message.SendMessageActionBuilder;
import java.util.List;

public class SendSseMessageActionBuilder extends SendMessageActionBuilder {

  public SendSseMessageActionBuilder() {}

  public SendSseMessageActionBuilder(String data) {
    withBody(data);
  }

  @Override
  public SendMessageAction onChannelsMatching(RequestPattern targetChannelPattern) {
    return buildAction(sseTarget(targetChannelPattern));
  }

  @Override
  public TargetedSendMessageActionBuilder toMatchingChannels(RequestPattern targetChannelPattern) {
    return targetedBuilder(sseTarget(targetChannelPattern));
  }

  private static RequestInitiatedChannelTarget sseTarget(RequestPattern requestPattern) {
    return RequestInitiatedChannelTarget.forTypeAndPattern(ChannelType.SSE, requestPattern);
  }

  public SendSseMessageActionBuilder withEventName(String eventName) {
    validateNoLineBreaks("event name", eventName);
    replaceHeader(new MessageHeader(SseMessageChannel.EVENT_HEADER, eventName));
    return this;
  }

  public SendSseMessageActionBuilder withEventId(String id) {
    validateNoLineBreaks("event id", id);
    replaceHeader(new MessageHeader(SseMessageChannel.ID_HEADER, id));
    return this;
  }

  public SendSseMessageActionBuilder withRetry(long milliseconds) {
    replaceHeader(new MessageHeader(SseMessageChannel.RETRY_HEADER, String.valueOf(milliseconds)));
    return this;
  }

  private static void validateSingleValuedSseHeaders(MessageHeaders headers) {
    for (String key :
        List.of(
            SseMessageChannel.EVENT_HEADER,
            SseMessageChannel.ID_HEADER,
            SseMessageChannel.RETRY_HEADER)) {
      MessageHeader header = headers.getHeader(key);
      if (header.isPresent() && !header.isSingleValued()) {
        throw new IllegalStateException(
            "SSE header '"
                + key
                + "' must have a single value but had "
                + header.values().size()
                + ". Use withEventName/withEventId/withRetry, or a single-valued withHeader.");
      }
    }
  }

  private static void validateHeadersHaveNoLineBreaks(MessageHeaders headers) {
    for (MessageHeader header : headers.all()) {
      validateNoLineBreaks("header key", header.key());
      for (String value : header.values()) {
        validateNoLineBreaks("header value", value);
      }
    }
  }

  private static void validateNoLineBreaks(String description, String value) {
    if (value != null && (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)) {
      throw new IllegalStateException("SSE " + description + " must not contain line breaks");
    }
  }

  @Override
  public SendSseMessageActionBuilder withBody(String message) {
    validateNoLineBreaks("message body", message);
    super.withBody(message);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withBodyFromStore(String storeName, String key) {
    super.withBodyFromStore(storeName, key);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withBodyFromFile(String filePath) {
    super.withBodyFromFile(filePath);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withHeader(String key, String... values) {
    validateNoLineBreaks("header key", key);
    for (String value : values) {
      validateNoLineBreaks("header value", value);
    }
    MessageHeaders candidate = headers().plus(new MessageHeader(key, values));
    validateSingleValuedSseHeaders(candidate);
    super.withHeaders(candidate);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withHeaders(MessageHeaders headers) {
    validateHeadersHaveNoLineBreaks(headers);
    validateSingleValuedSseHeaders(headers);
    super.withHeaders(headers);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withTransformer(String transformerName) {
    super.withTransformer(transformerName);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withTransformers(String... transformerNames) {
    super.withTransformers(transformerNames);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withTransformerParameters(Parameters parameters) {
    super.withTransformerParameters(parameters);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withTransformerParameter(String key, Object value) {
    super.withTransformerParameter(key, value);
    return this;
  }
}
