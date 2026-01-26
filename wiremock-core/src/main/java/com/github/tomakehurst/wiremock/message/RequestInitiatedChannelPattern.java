/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.Collections;
import java.util.Objects;

@JsonInclude(NON_EMPTY)
public class RequestInitiatedChannelPattern implements ChannelPattern {

  private final RequestPattern initiatingRequestPattern;
  private final ChannelType channelType;

  @JsonCreator
  public RequestInitiatedChannelPattern(
      @JsonProperty("type") ChannelType type,
      @JsonProperty("initiatingRequestPattern") RequestPattern initiatingRequestPattern) {
    this.channelType = type;
    this.initiatingRequestPattern = initiatingRequestPattern;
  }

  public static RequestInitiatedChannelPattern forRequestPattern(
      ChannelType channelType, RequestPattern requestPattern) {
    return new RequestInitiatedChannelPattern(channelType, requestPattern);
  }

  @Override
  @JsonInclude
  public ChannelType getType() {
    return channelType;
  }

  public RequestPattern getInitiatingRequestPattern() {
    return initiatingRequestPattern;
  }

  @Override
  public boolean matches(MessageChannel channel) {
    if (channel instanceof RequestInitiatedMessageChannel) {
      return matches(((RequestInitiatedMessageChannel) channel).getInitiatingRequest());
    }
    return initiatingRequestPattern == null;
  }

  public boolean matches(Request channelRequest) {
    if (initiatingRequestPattern != null) {
      MatchResult channelMatch =
          initiatingRequestPattern.match(channelRequest, Collections.emptyMap());
      return channelMatch.isExactMatch();
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestInitiatedChannelPattern that = (RequestInitiatedChannelPattern) o;
    return Objects.equals(initiatingRequestPattern, that.initiatingRequestPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(initiatingRequestPattern);
  }
}
