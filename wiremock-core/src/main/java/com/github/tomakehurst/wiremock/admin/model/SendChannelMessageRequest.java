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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.MessageDefinition;

/** Request model for sending a message to channels of a specific type. */
public class SendChannelMessageRequest {

  private final ChannelType type;
  private final RequestPattern requestPattern;
  private final MessageDefinition message;

  @JsonCreator
  public SendChannelMessageRequest(
      @JsonProperty("type") ChannelType type,
      @JsonProperty("request") RequestPattern requestPattern,
      @JsonProperty("message") MessageDefinition message) {
    this.type = type;
    this.requestPattern = requestPattern;
    this.message = message;
  }

  public ChannelType getType() {
    return type;
  }

  public RequestPattern getRequestPattern() {
    return requestPattern;
  }

  public MessageDefinition getMessage() {
    return message;
  }
}
