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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.Objects;

public class RequestInitiatedChannelTarget implements ChannelTarget {

  private final ChannelType channelType;
  private final RequestPattern requestPattern;

  @JsonCreator
  public RequestInitiatedChannelTarget(
      @JsonProperty("channelType") ChannelType channelType,
      @JsonProperty("requestPattern") RequestPattern requestPattern) {
    this.channelType = channelType;
    this.requestPattern = requestPattern;
  }

  public static RequestInitiatedChannelTarget forPattern(RequestPattern requestPattern) {
    return new RequestInitiatedChannelTarget(null, requestPattern);
  }

  public static RequestInitiatedChannelTarget forTypeAndPattern(
      ChannelType channelType, RequestPattern requestPattern) {
    return new RequestInitiatedChannelTarget(channelType, requestPattern);
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public ChannelType getChannelType() {
    return channelType;
  }

  public RequestPattern getRequestPattern() {
    return requestPattern;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    RequestInitiatedChannelTarget that = (RequestInitiatedChannelTarget) o;
    return channelType == that.channelType && Objects.equals(requestPattern, that.requestPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelType, requestPattern);
  }

  @Override
  public String toString() {
    return "RequestInitiatedChannelTarget{"
        + "channelType="
        + channelType
        + ", requestPattern="
        + requestPattern
        + '}';
  }
}
