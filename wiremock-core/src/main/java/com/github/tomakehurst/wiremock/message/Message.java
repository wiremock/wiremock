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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.Format;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@JsonDeserialize(using = Message.MessageDeserializer.class)
@NullMarked
@SuppressWarnings("ReferenceEquality")
public class Message {

  private final Entity body;
  @Nullable private final String eventName;
  @Nullable private final String id;

  public Message(Entity body) {
    this(body, null, null);
  }

  public Message(Entity body, @Nullable String eventName, @Nullable String id) {
    this.body = body;
    this.eventName = eventName;
    this.id = id;
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

  @JsonValue
  @Nullable
  public String getBodyAsString() {
    if (body == Entity.EMPTY) {
      return null;
    }
    byte[] data = body.getData();
    return data != null ? new String(data, StandardCharsets.UTF_8) : null;
  }

  @JsonIgnore
  public boolean isBinary() {
    return body != Entity.EMPTY && Format.BINARY.equals(body.getFormat());
  }

  @JsonIgnore
  public @Nullable String getEventName() {
    return eventName;
  }

  @JsonIgnore
  public @Nullable String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (getClass() != o.getClass()) return false;
    Message message = (Message) o;
    return Objects.equals(body, message.body)
        && Objects.equals(eventName, message.eventName)
        && Objects.equals(id, message.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, eventName, id);
  }

  @Override
  @Nullable
  public String toString() {
    return getBodyAsString();
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
    @Nullable private String eventName;
    @Nullable private String id;

    public Builder() {}

    private Builder(Message message) {
      this.body = message.body;
      this.eventName = message.eventName;
      this.id = message.id;
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

    public Builder withEventName(@Nullable String eventName) {
      this.eventName = eventName;
      return this;
    }

    public Builder withId(@Nullable String id) {
      this.id = id;
      return this;
    }

    public Message build() {
      return new Message(body, eventName, id);
    }
  }

  static class MessageDeserializer extends JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text = p.getValueAsString();
      if (text == null) {
        return new Message(Entity.EMPTY);
      }

      Entity entity = Entity.builder().setData(text).build();
      return new Message(entity);
    }
  }
}
