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
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EncodingType;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.FormatType;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = Message.MessageDeserializer.class)
public class Message {

  private final Entity body;

  public Message(Entity body) {
    this.body = body;
  }

  @JsonIgnore
  public Entity getBody() {
    return body;
  }

  @JsonIgnore
  public byte[] getBodyAsBytes() {
    if (body == null) {
      return null;
    }
    return body.getData();
  }

  @JsonValue
  public String getBodyAsString() {
    if (body == null) {
      return null;
    }
    byte[] data = body.getData();
    return data != null ? new String(data, StandardCharsets.UTF_8) : null;
  }

  @JsonIgnore
  public boolean isBinary() {
    return body != null && EncodingType.BINARY.equals(body.getEncoding());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Message message = (Message) o;
    return Objects.equals(body, message.body);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(body);
  }

  @Override
  public String toString() {
    return getBodyAsString();
  }

  static class MessageDeserializer extends ValueDeserializer<Message> {
    @Override
    public Message deserialize(JsonParser p, DeserializationContext ctxt) {
      String text = p.getValueAsString();
      if (text == null) {
        return new Message(null);
      }
      byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
      InputStreamSource streamSource = () -> new ByteArrayInputStream(bytes);
      Entity entity =
          new Entity(EncodingType.TEXT, FormatType.TEXT, CompressionType.NONE, streamSource);
      return new Message(entity);
    }
  }
}
