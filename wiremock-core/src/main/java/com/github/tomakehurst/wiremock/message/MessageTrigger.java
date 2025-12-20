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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.Map;
import java.util.Objects;

@JsonInclude(NON_EMPTY)
public class MessageTrigger {

  public static final MessageTrigger ANYTHING = new MessageTrigger(null, null);

  private final RequestPattern channelPattern;
  private final MessagePattern messagePattern;

  @JsonCreator
  public MessageTrigger(
      @JsonProperty("channelPattern") RequestPattern channelPattern,
      @JsonProperty("messagePattern") MessagePattern messagePattern) {
    this.channelPattern = channelPattern;
    this.messagePattern = messagePattern;
  }

  public RequestPattern getChannelPattern() {
    return channelPattern;
  }

  public MessagePattern getMessagePattern() {
    return messagePattern != null ? messagePattern : MessagePattern.ANYTHING;
  }

  @JsonIgnore
  public StringValuePattern getBodyPattern() {
    return messagePattern != null ? messagePattern.getBodyPattern() : null;
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

    if (messagePattern != null) {
      return messagePattern.matches(channelRequest, message, customMatchers);
    }

    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageTrigger that = (MessageTrigger) o;
    return Objects.equals(channelPattern, that.channelPattern)
        && Objects.equals(messagePattern, that.messagePattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelPattern, messagePattern);
  }
}
