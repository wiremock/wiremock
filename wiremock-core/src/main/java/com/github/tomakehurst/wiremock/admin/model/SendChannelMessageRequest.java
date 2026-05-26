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
package com.github.tomakehurst.wiremock.admin.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.MessageDefinition;

/** Request model for sending a message to channels of a specific type. */
@JsonInclude(NON_NULL)
public class SendChannelMessageRequest {

  private final ChannelType type;
  private final RequestPattern initiatingRequestPattern;
  private final String providerName;
  private final String channelName;
  private final MessageDefinition message;

  @JsonCreator
  public SendChannelMessageRequest(
      @JsonProperty("type") ChannelType type,
      @JsonProperty("initiatingRequest") RequestPattern initiatingRequestPattern,
      @JsonProperty("providerName") String providerName,
      @JsonProperty("channelName") String channelName,
      @JsonProperty("message") MessageDefinition message) {
    this.type = type;
    this.initiatingRequestPattern = initiatingRequestPattern;
    this.providerName = providerName;
    this.channelName = channelName;
    this.message = message;
  }

  public static SendChannelMessageRequest forWebSocket(
      ChannelType type, RequestPattern initiatingRequestPattern, MessageDefinition message) {
    return new SendChannelMessageRequest(type, initiatingRequestPattern, null, null, message);
  }

  public static SendChannelMessageRequest forFixedChannel(
      String providerName, String channelName, MessageDefinition message) {
    return new SendChannelMessageRequest(ChannelType.FIXED, null, providerName, channelName, message);
  }

  public ChannelType getType() {
    return type;
  }

  public RequestPattern getInitiatingRequest() {
    return initiatingRequestPattern;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getChannelName() {
    return channelName;
  }

  public MessageDefinition getMessage() {
    return message;
  }
}
