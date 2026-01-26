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
package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DisabledMessageJournal implements MessageJournal {

  @Override
  public int countEventsMatching(MessagePattern pattern) {
    throw new MessageJournalDisabledException();
  }

  @Override
  public List<MessageServeEvent> getEventsMatching(MessagePattern pattern) {
    throw new MessageJournalDisabledException();
  }

  @Override
  public List<MessageServeEvent> getAllMessageServeEvents() {
    throw new MessageJournalDisabledException();
  }

  @Override
  public Optional<MessageServeEvent> getMessageServeEvent(UUID id) {
    throw new MessageJournalDisabledException();
  }

  @Override
  public void reset() {}

  @Override
  public void messageReceived(MessageServeEvent event) {}

  @Override
  public void removeEvent(UUID eventId) {}

  @Override
  public List<MessageServeEvent> removeEventsMatching(MessagePattern pattern) {
    throw new MessageJournalDisabledException();
  }

  @Override
  public List<MessageServeEvent> removeEventsForStubsMatchingMetadata(
      StringValuePattern metadataPattern) {
    throw new MessageJournalDisabledException();
  }

  @Override
  public Optional<MessageServeEvent> waitForEvent(MessagePattern pattern, Duration maxWait) {
    throw new MessageJournalDisabledException();
  }

  @Override
  public List<MessageServeEvent> waitForEvents(
      MessagePattern pattern, int count, Duration maxWait) {
    throw new MessageJournalDisabledException();
  }
}
