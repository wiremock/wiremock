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
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.List;
import org.wiremock.annotations.Beta;

/** Result containing a list of message serve events from the message journal. */
@Beta(justification = "Message Journal API")
public class GetMessageServeEventsResult {

  private final List<MessageServeEvent> messageServeEvents;
  private final boolean messageJournalDisabled;

  @JsonCreator
  public GetMessageServeEventsResult(
      @JsonProperty("messageServeEvents") List<MessageServeEvent> messageServeEvents,
      @JsonProperty("messageJournalDisabled") boolean messageJournalDisabled) {
    this.messageServeEvents = messageServeEvents;
    this.messageJournalDisabled = messageJournalDisabled;
  }

  public static GetMessageServeEventsResult messageJournalEnabled(
      List<MessageServeEvent> messageServeEvents) {
    return new GetMessageServeEventsResult(messageServeEvents, false);
  }

  public static GetMessageServeEventsResult messageJournalDisabled() {
    return new GetMessageServeEventsResult(List.of(), true);
  }

  public List<MessageServeEvent> getMessageServeEvents() {
    return messageServeEvents;
  }

  public boolean isMessageJournalDisabled() {
    return messageJournalDisabled;
  }
}
