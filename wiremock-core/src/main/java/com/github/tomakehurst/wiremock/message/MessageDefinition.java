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
package com.github.tomakehurst.wiremock.message;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonInclude(NON_NULL)
@NullMarked
public class MessageDefinition {

  private final EntityDefinition body;
  private final MessageHeaders headers;

  @JsonCreator
  public MessageDefinition(
      @JsonProperty("body") EntityDefinition body,
      @Nullable @JsonProperty("headers") MessageHeaders headers) {
    this.body = body;
    this.headers = headers != null ? headers : MessageHeaders.noHeaders();
  }

  public MessageDefinition(EntityDefinition body) {
    this(body, MessageHeaders.noHeaders());
  }

  public static MessageDefinition fromString(@Nullable String message) {
    return new MessageDefinition(EntityDefinition.simple(message));
  }

  public static MessageDefinition fromBytes(byte[] data) {
    return new MessageDefinition(WireMock.binaryEntity().setData(data).build());
  }

  public EntityDefinition getBody() {
    return body;
  }

  @JsonInclude(NON_EMPTY)
  public MessageHeaders getHeaders() {
    return headers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (getClass() != o.getClass()) return false;
    MessageDefinition that = (MessageDefinition) o;
    return Objects.equals(body, that.body) && Objects.equals(headers, that.headers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, headers);
  }
}
