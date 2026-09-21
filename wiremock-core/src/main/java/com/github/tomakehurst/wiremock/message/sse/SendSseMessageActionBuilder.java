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
import com.github.tomakehurst.wiremock.message.MessageHeaders;
import com.github.tomakehurst.wiremock.message.SendMessageActionBuilder;

public class SendSseMessageActionBuilder extends SendMessageActionBuilder {

  public SendSseMessageActionBuilder() {}

  public SendSseMessageActionBuilder(String data) {
    withBody(data);
  }

  public SendSseMessageActionBuilder withEventName(String eventName) {
    withHeader(SseMessageChannel.EVENT_HEADER, eventName);
    return this;
  }

  public SendSseMessageActionBuilder withEventId(String id) {
    withHeader(SseMessageChannel.ID_HEADER, id);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withBody(String message) {
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
    super.withHeader(key, values);
    return this;
  }

  @Override
  public SendSseMessageActionBuilder withHeaders(MessageHeaders headers) {
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
