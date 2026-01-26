/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.*;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Admin {

  void addStubMapping(StubMapping stubMapping);

  void editStubMapping(StubMapping stubMapping);

  void removeStubMapping(StubMapping stubbMapping);

  void removeStubMapping(UUID id);

  ListStubMappingsResult listAllStubMappings();

  SingleStubMappingResult getStubMapping(UUID id);

  void saveMappings();

  void resetRequests();

  void resetScenarios();

  void resetMappings();

  void resetAll();

  void resetToDefaultMappings();

  GetServeEventsResult getServeEvents();

  GetServeEventsResult getServeEvents(ServeEventQuery query);

  SingleServedStubResult getServedStub(UUID id);

  VerificationResult countRequestsMatching(RequestPattern requestPattern);

  FindRequestsResult findRequestsMatching(RequestPattern requestPattern);

  FindRequestsResult findUnmatchedRequests();

  void removeServeEvent(UUID eventId);

  FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern);

  FindServeEventsResult removeServeEventsForStubsMatchingMetadata(StringValuePattern pattern);

  FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest);

  FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern);

  FindNearMissesResult findNearMissesForUnmatchedRequests();

  GetScenariosResult getAllScenarios();

  void resetScenario(String name);

  void setScenarioState(String name, String state);

  void updateGlobalSettings(GlobalSettings settings);

  SnapshotRecordResult snapshotRecord();

  SnapshotRecordResult snapshotRecord(RecordSpec spec);

  SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec);

  void startRecording(String targetBaseUrl);

  void startRecording(RecordSpec spec);

  void startRecording(RecordSpecBuilder recordSpec);

  SnapshotRecordResult stopRecording();

  RecordingStatusResult getRecordingStatus();

  Options getOptions();

  void shutdownServer();

  ListStubMappingsResult findUnmatchedStubs();

  ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern);

  void removeStubsByMetadata(StringValuePattern pattern);

  void importStubs(StubImport stubImport);

  void removeStubMappings(List<StubMapping> stubMappings);

  GetGlobalSettingsResult getGlobalSettings();

  /**
   * Sends a message to all channels of the specified type matching the given request pattern.
   *
   * @param type the channel type to target
   * @param requestPattern the pattern to match against the original upgrade request
   * @param message the message to send
   * @return result containing the matched channels that were messaged
   */
  SendChannelMessageResult sendChannelMessage(
      ChannelType type, RequestPattern requestPattern, MessageDefinition message);

  /**
   * Sends a message to all channels of the specified type matching the given request pattern.
   * Convenience method that wraps the string message in a MessageDefinition.
   *
   * @param type the channel type to target
   * @param requestPattern the pattern to match against the original upgrade request
   * @param message the message to send
   * @return result containing the matched channels that were messaged
   */
  default SendChannelMessageResult sendChannelMessage(
      ChannelType type, RequestPattern requestPattern, String message) {
    return sendChannelMessage(type, requestPattern, MessageDefinition.fromString(message));
  }

  /**
   * Lists all active message channels.
   *
   * @return result containing all message channels
   */
  ListMessageChannelsResult listAllMessageChannels();

  /**
   * Adds a message stub mapping that will be matched against incoming messages on channels.
   *
   * @param messageStubMapping the message stub mapping to add
   */
  void addMessageStubMapping(MessageStubMapping messageStubMapping);

  /**
   * Removes a message stub mapping by its ID.
   *
   * @param id the ID of the message stub mapping to remove
   */
  void removeMessageStubMapping(UUID id);

  /** Removes all message stub mappings. */
  void resetMessageStubMappings();

  /**
   * Finds all message stub mappings matching the given metadata pattern.
   *
   * @param pattern the pattern to match stub metadata against
   * @return result containing matching message stub mappings
   */
  ListMessageStubMappingsResult findAllMessageStubsByMetadata(StringValuePattern pattern);

  /**
   * Removes all message stub mappings matching the given metadata pattern.
   *
   * @param pattern the pattern to match stub metadata against
   */
  void removeMessageStubsByMetadata(StringValuePattern pattern);

  /**
   * Lists all message stub mappings.
   *
   * @return result containing all message stub mappings
   */
  ListMessageStubMappingsResult listAllMessageStubMappings();

  /**
   * Gets all message serve events from the message journal.
   *
   * @return result containing all message serve events
   */
  GetMessageServeEventsResult getMessageServeEvents();

  /**
   * Gets a specific message serve event by ID.
   *
   * @param id the event ID
   * @return the event if found
   */
  SingleMessageServeEventResult getMessageServeEvent(UUID id);

  /**
   * Counts message events matching the given pattern.
   *
   * @param pattern the pattern to match events against
   * @return the count of matching events
   */
  int countMessageEventsMatching(MessagePattern pattern);

  /**
   * Gets message events matching the given pattern.
   *
   * @param pattern the pattern to match events against
   * @return list of matching events
   */
  List<MessageServeEvent> findMessageEventsMatching(MessagePattern pattern);

  /**
   * Removes a specific message serve event from the journal.
   *
   * @param eventId the ID of the event to remove
   */
  void removeMessageServeEvent(UUID eventId);

  /**
   * Removes all message serve events matching the given pattern.
   *
   * @param pattern the pattern to match events against
   * @return result containing the removed events
   */
  FindMessageServeEventsResult removeMessageServeEventsMatching(MessagePattern pattern);

  /**
   * Removes all message serve events for stubs matching the given metadata pattern.
   *
   * @param pattern the pattern to match stub metadata against
   * @return result containing the removed events
   */
  FindMessageServeEventsResult removeMessageServeEventsForStubsMatchingMetadata(
      StringValuePattern pattern);

  /** Resets the message journal, removing all events. */
  void resetMessageJournal();

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
}
