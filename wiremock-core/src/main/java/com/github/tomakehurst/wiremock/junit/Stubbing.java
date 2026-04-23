/*
 * Copyright (C) 2013-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.MessageStubMappingBuilder;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Stubbing {

  StubMapping givenThat(MappingBuilder mappingBuilder);

  StubMapping stubFor(MappingBuilder mappingBuilder);

  void editStub(MappingBuilder mappingBuilder);

  void removeStub(MappingBuilder mappingBuilder);

  void removeStub(StubMapping mappingBuilder);

  void removeStub(UUID id);

  List<StubMapping> getStubMappings();

  StubMapping getSingleStubMapping(UUID id);

  List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern);

  void removeStubMappingsByMetadata(StringValuePattern pattern);

  void verify(RequestPatternBuilder requestPatternBuilder);

  void verify(int count, RequestPatternBuilder requestPatternBuilder);

  void verify(
      CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder);

  List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder);

  List<ServeEvent> getAllServeEvents();

  void setGlobalFixedDelay(int milliseconds);

  List<LoggedRequest> findAllUnmatchedRequests();

  List<NearMiss> findNearMissesForAllUnmatchedRequests();

  List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest);

  List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder);

  // Message stub mapping methods

  /**
   * Registers a message stub mapping using a builder.
   *
   * @param builder the message stub mapping builder
   * @return the registered MessageStubMapping
   */
  MessageStubMapping messageStubFor(MessageStubMappingBuilder builder);

  /**
   * Registers a message stub mapping directly.
   *
   * @param messageStubMapping the message stub mapping to register
   * @return the registered MessageStubMapping
   */
  MessageStubMapping messageStubFor(MessageStubMapping messageStubMapping);

  /**
   * Removes a message stub mapping.
   *
   * @param id the UUID of the message stub mapping to remove
   */
  void removeMessageStub(UUID id);

  /**
   * Gets all registered message stub mappings.
   *
   * @return list of all message stub mappings
   */
  List<MessageStubMapping> getMessageStubMappingsList();

  /** Removes all message stub mappings. */
  void resetMessageStubs();

  // Message journal verification methods

  /**
   * Gets all message serve events from the message journal.
   *
   * @return list of all message serve events
   */
  List<MessageServeEvent> getAllMessageServeEvents();

  /**
   * Gets message serve events matching the given pattern.
   *
   * @param pattern the pattern to match events against
   * @return list of matching events
   */
  List<MessageServeEvent> findAllMessageEvents(MessagePattern pattern);

  /**
   * Verifies that at least one message event matches the given pattern.
   *
   * @param pattern the pattern to match events against
   */
  void verifyMessageEvent(MessagePattern pattern);

  /**
   * Verifies that exactly the specified number of message events match the given pattern.
   *
   * @param expectedCount the expected number of matching events
   * @param pattern the pattern to match events against
   */
  void verifyMessageEvent(int expectedCount, MessagePattern pattern);

  /**
   * Verifies that the number of message events matching the pattern satisfies the count strategy.
   *
   * @param expectedCount the count matching strategy
   * @param pattern the pattern to match events against
   */
  void verifyMessageEvent(CountMatchingStrategy expectedCount, MessagePattern pattern);

  /**
   * Waits for a message event matching the given pattern to appear in the journal.
   *
   * @param pattern the pattern to match events against
   * @param maxWait the maximum duration to wait
   * @return the matching event if found within the timeout
   */
  Optional<MessageServeEvent> waitForMessageEvent(MessagePattern pattern, Duration maxWait);

  /**
   * Waits for a specific number of message events matching the given pattern.
   *
   * @param pattern the pattern to match events against
   * @param count the number of events to wait for
   * @param maxWait the maximum duration to wait
   * @return list of matching events
   */
  List<MessageServeEvent> waitForMessageEvents(MessagePattern pattern, int count, Duration maxWait);

  /** Resets the message journal, removing all events. */
  void resetMessageJournal();
}
