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
package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageJournal {

  int countEventsMatching(MessagePattern pattern);

  List<MessageServeEvent> getEventsMatching(MessagePattern pattern);

  List<MessageServeEvent> getAllMessageServeEvents();

  Optional<MessageServeEvent> getMessageServeEvent(UUID id);

  void reset();

  void messageReceived(MessageServeEvent event);

  void removeEvent(UUID eventId);

  List<MessageServeEvent> removeEventsMatching(MessagePattern pattern);

  List<MessageServeEvent> removeEventsForStubsMatchingMetadata(StringValuePattern metadataPattern);

  Optional<MessageServeEvent> waitForEvent(MessagePattern pattern, Duration maxWait);

  List<MessageServeEvent> waitForEvents(MessagePattern pattern, int count, Duration maxWait);
}
