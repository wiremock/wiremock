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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.Format;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@JsonDeserialize(using = Message.MessageDeserializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NullMarked
@SuppressWarnings("ReferenceEquality")
public class Message {

  private final Entity body;
  private final MessageHeaders headers;

  public Message(Entity body) {
    this(body, MessageHeaders.noHeaders());
  }

  public Message(Entity body, MessageHeaders headers) {
    this.body = body;
    this.headers = headers != null ? headers : MessageHeaders.noHeaders();
  }

  @JsonIgnore
  public Entity getBody() {
    return body;
  }

  @JsonIgnore
  public byte @Nullable [] getBodyAsBytes() {
    if (body == Entity.EMPTY) {
      return null;
    }
    return body.getData();
  }

  @JsonProperty("body")
  public @Nullable String getBodyAsString() {
    if (body == Entity.EMPTY) {
      return null;
    }
    if (isBinary()) {
      return null;
    }
    byte[] data = body.getData();
    return data != null ? new String(data, StandardCharsets.UTF_8) : null;
  }

  @JsonProperty("base64Body")
  public @Nullable String getBase64Body() {
    if (!isBinary()) {
      return null;
    }
    byte[] data = body.getData();
    return data != null ? Base64.getEncoder().encodeToString(data) : null;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public MessageHeaders getHeaders() {
    return headers;
  }

  @JsonIgnore
  public boolean isBinary() {
    return body != Entity.EMPTY && Format.BINARY.equals(body.getFormat());
  }

  @Override
  public boolean equals(Object o) {
    if (getClass() != o.getClass()) return false;
    Message message = (Message) o;
    return Objects.equals(body, message.body) && Objects.equals(headers, message.headers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, headers);
  }

  @Override
  public @Nullable String toString() {
    return isBinary() ? getBase64Body() : getBodyAsString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public Message transform(java.util.function.Function<Builder, Builder> builderTransform) {
    return builderTransform.apply(new Builder(this)).build();
  }

  @NullUnmarked
  public static class Builder {
    private Entity body;
    private MessageHeaders headers = MessageHeaders.noHeaders();

    public Builder() {}

    private Builder(Message message) {
      this.body = message.body;
      this.headers = message.headers;
    }

    public Builder withBody(Entity body) {
      this.body = body;
      return this;
    }

    public Builder withTextBody(@Nullable String text) {
      if (text == null) {
        this.body = Entity.EMPTY;
        return this;
      }

      this.body = Entity.builder().setData(text).build();
      return this;
    }

    public Builder withBinaryBody(byte[] data) {
      this.body = Entity.builder().setFormat(Format.BINARY).setData(data).build();
      return this;
    }

    public Builder withHeaders(MessageHeaders headers) {
      this.headers = headers;
      return this;
    }

    public Builder withHeader(String key, String... values) {
      this.headers = this.headers.plus(new MessageHeader(key, values));
      return this;
    }

    public Message build() {
      return new Message(body, headers);
    }
  }

  static class MessageDeserializer extends JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.currentToken() == JsonToken.VALUE_STRING) {
        String text = p.getValueAsString();
        if (text == null) {
          return new Message(Entity.EMPTY);
        }
        return new Message(Entity.builder().setData(text).build());
      }

      JsonNode node = p.readValueAsTree();
      Entity body = Entity.EMPTY;
      JsonNode bodyNode = node.get("body");
      JsonNode base64BodyNode = node.get("base64Body");
      if (bodyNode != null && bodyNode.isTextual()) {
        body = Entity.builder().setData(bodyNode.textValue()).build();
      } else if (base64BodyNode != null && base64BodyNode.isTextual()) {
        byte[] data = Base64.getDecoder().decode(base64BodyNode.textValue());
        body = Entity.builder().setFormat(Format.BINARY).setData(data).build();
      }

      MessageHeaders headers = MessageHeaders.noHeaders();
      JsonNode headersNode = node.get("headers");
      if (headersNode != null && headersNode.isObject()) {
        JsonParser headersParser = headersNode.traverse(p.getCodec());
        headersParser.nextToken();
        headers = ctxt.readValue(headersParser, MessageHeaders.class);
      }

      return new Message(body, headers);
    }
  }
}
