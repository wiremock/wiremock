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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessageAction;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Default implementation of MessageStubMappingBuilder. */
public class BasicMessageStubMappingBuilder implements MessageStubMappingBuilder {

  private UUID id;
  private String name;
  private Integer priority;
  private RequestPattern channelPattern;
  private StringValuePattern messagePattern;
  private final List<MessageAction> actions = new ArrayList<>();

  /**
   * Creates a new builder with the specified channel pattern.
   *
   * @param channelPattern the pattern to match channels against
   */
  public BasicMessageStubMappingBuilder(RequestPattern channelPattern) {
    this.channelPattern = channelPattern;
  }

  @Override
  public MessageStubMappingBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public MessageStubMappingBuilder withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public MessageStubMappingBuilder atPriority(Integer priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public MessageStubMappingBuilder withMessageBody(StringValuePattern messagePattern) {
    this.messagePattern = messagePattern;
    return this;
  }

  @Override
  public MessageStubMapping willTriggerActions(MessageAction... actions) {
    this.actions.addAll(Arrays.asList(actions));
    return build();
  }

  @Override
  public MessageStubMapping build() {
    return new MessageStubMapping(id, name, priority, channelPattern, messagePattern, actions);
  }
}
