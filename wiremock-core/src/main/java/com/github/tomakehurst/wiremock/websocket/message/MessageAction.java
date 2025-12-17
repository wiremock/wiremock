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
package com.github.tomakehurst.wiremock.websocket.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.tomakehurst.wiremock.websocket.MessageChannel;
import com.github.tomakehurst.wiremock.websocket.MessageChannels;

/**
 * Represents an action to be taken when a message stub matches an incoming message. Actions are
 * executed in order when a message stub matches.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SendMessageAction.class, name = "send")})
public interface MessageAction {

  /**
   * Executes this action.
   *
   * @param originatingChannel the channel on which the triggering message was received
   * @param messageChannels the collection of all message channels
   * @param incomingMessage the message that triggered this action
   */
  void execute(
      MessageChannel originatingChannel, MessageChannels messageChannels, String incomingMessage);
}
