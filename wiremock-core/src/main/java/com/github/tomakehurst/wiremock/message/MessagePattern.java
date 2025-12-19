/*
 * Copyright (C) 2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MessagePattern {

  public static final MessagePattern ANYTHING = new MessagePattern(null, null);

  private final RequestPattern channelPattern;
  private final StringValuePattern bodyPattern;

  @JsonCreator
  public MessagePattern(
      @JsonProperty("channelPattern") RequestPattern channelPattern,
      @JsonProperty("messagePattern") StringValuePattern bodyPattern) {
    this.channelPattern = channelPattern;
    this.bodyPattern = bodyPattern;
  }

  public static Builder messagePattern() {
    return new Builder();
  }

  public static MessagePattern create(Consumer<Builder> transformer) {
    final Builder builder = messagePattern();
    transformer.accept(builder);
    return builder.build();
  }

  public MessagePattern transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public RequestPattern getChannelPattern() {
    return channelPattern;
  }

  public StringValuePattern getBodyPattern() {
    return bodyPattern;
  }

  public boolean matches(
      MessageChannel channel,
      Message message,
      Map<String, RequestMatcherExtension> customMatchers) {
    return matches(channel.getRequest(), message, customMatchers);
  }

  public boolean matches(
      Request channelRequest,
      Message message,
      Map<String, RequestMatcherExtension> customMatchers) {
    if (channelPattern != null) {
      MatchResult channelMatch = channelPattern.match(channelRequest, customMatchers);
      if (!channelMatch.isExactMatch()) {
        return false;
      }
    }

    if (bodyPattern != null) {
      String messageBody = message != null ? message.getBodyAsString() : null;
      MatchResult messageMatch = bodyPattern.match(messageBody);
      if (!messageMatch.isExactMatch()) {
        return false;
      }
    }

    return true;
  }

  public boolean matches(
      MessageServeEvent event, Map<String, RequestMatcherExtension> customMatchers) {
    return matches(event.getChannelRequest(), event.getMessage(), customMatchers);
  }

  public boolean matches(MessageServeEvent event) {
    return matches(event, Collections.emptyMap());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessagePattern that = (MessagePattern) o;
    return Objects.equals(channelPattern, that.channelPattern)
        && Objects.equals(bodyPattern, that.bodyPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelPattern, bodyPattern);
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  public static class Builder {
    private RequestPattern channelPattern;
    private StringValuePattern messagePattern;

    public Builder() {}

    public Builder(MessagePattern existing) {
      this.channelPattern = existing.channelPattern;
      this.messagePattern = existing.bodyPattern;
    }

    public RequestPattern getChannelPattern() {
      return channelPattern;
    }

    public Builder setChannelPattern(RequestPattern channelPattern) {
      this.channelPattern = channelPattern;
      return this;
    }

    public Builder withChannelPattern(RequestPattern channelPattern) {
      return setChannelPattern(channelPattern);
    }

    public StringValuePattern getBodyPattern() {
      return messagePattern;
    }

    public Builder setBodyPattern(StringValuePattern messagePattern) {
      this.messagePattern = messagePattern;
      return this;
    }

    public Builder withBody(StringValuePattern messagePattern) {
      return setBodyPattern(messagePattern);
    }

    public MessagePattern build() {
      return new MessagePattern(channelPattern, messagePattern);
    }
  }
}
