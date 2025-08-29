/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.admin.model.GetGlobalSettingsResult;
import com.github.tomakehurst.wiremock.admin.model.GetScenariosResult;
import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.admin.model.SingleServedStubResult;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.FindServeEventsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import java.util.UUID;

/** The interface Admin. */
public interface Admin {

  /**
   * Add stub mapping.
   *
   * @param stubMapping the stub mapping
   */
  void addStubMapping(StubMapping stubMapping);

  /**
   * Edit stub mapping.
   *
   * @param stubMapping the stub mapping
   */
  void editStubMapping(StubMapping stubMapping);

  /**
   * Remove stub mapping.
   *
   * @param stubbMapping the stubb mapping
   */
  void removeStubMapping(StubMapping stubbMapping);

  /**
   * Remove stub mapping.
   *
   * @param id the id
   */
  void removeStubMapping(UUID id);

  /**
   * List all stub mappings list stub mappings result.
   *
   * @return the list stub mappings result
   */
  ListStubMappingsResult listAllStubMappings();

  /**
   * Gets stub mapping.
   *
   * @param id the id
   * @return the stub mapping
   */
  SingleStubMappingResult getStubMapping(UUID id);

  /** Save mappings. */
  void saveMappings();

  /** Reset requests. */
  void resetRequests();

  /** Reset scenarios. */
  void resetScenarios();

  /** Reset mappings. */
  void resetMappings();

  /** Reset all. */
  void resetAll();

  /** Reset to default mappings. */
  void resetToDefaultMappings();

  /**
   * Gets serve events.
   *
   * @return the serve events
   */
  GetServeEventsResult getServeEvents();

  /**
   * Gets serve events.
   *
   * @param query the query
   * @return the serve events
   */
  GetServeEventsResult getServeEvents(ServeEventQuery query);

  /**
   * Gets served stub.
   *
   * @param id the id
   * @return the served stub
   */
  SingleServedStubResult getServedStub(UUID id);

  /**
   * Count requests matching verification result.
   *
   * @param requestPattern the request pattern
   * @return the verification result
   */
  VerificationResult countRequestsMatching(RequestPattern requestPattern);

  /**
   * Find requests matching find requests result.
   *
   * @param requestPattern the request pattern
   * @return the find requests result
   */
  FindRequestsResult findRequestsMatching(RequestPattern requestPattern);

  /**
   * Find unmatched requests find requests result.
   *
   * @return the find requests result
   */
  FindRequestsResult findUnmatchedRequests();

  /**
   * Remove serve event.
   *
   * @param eventId the event id
   */
  void removeServeEvent(UUID eventId);

  /**
   * Remove serve events matching find serve events result.
   *
   * @param requestPattern the request pattern
   * @return the find serve events result
   */
  FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern);

  /**
   * Remove serve events for stubs matching metadata find serve events result.
   *
   * @param pattern the pattern
   * @return the find serve events result
   */
  FindServeEventsResult removeServeEventsForStubsMatchingMetadata(StringValuePattern pattern);

  /**
   * Find top near misses for find near misses result.
   *
   * @param loggedRequest the logged request
   * @return the find near misses result
   */
  FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest);

  /**
   * Find top near misses for find near misses result.
   *
   * @param requestPattern the request pattern
   * @return the find near misses result
   */
  FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern);

  /**
   * Find near misses for unmatched requests find near misses result.
   *
   * @return the find near misses result
   */
  FindNearMissesResult findNearMissesForUnmatchedRequests();

  /**
   * Gets all scenarios.
   *
   * @return the all scenarios
   */
  GetScenariosResult getAllScenarios();

  /**
   * Reset scenario.
   *
   * @param name the name
   */
  void resetScenario(String name);

  /**
   * Sets scenario state.
   *
   * @param name the name
   * @param state the state
   */
  void setScenarioState(String name, String state);

  /**
   * Update global settings.
   *
   * @param settings the settings
   */
  void updateGlobalSettings(GlobalSettings settings);

  /**
   * Snapshot record snapshot record result.
   *
   * @return the snapshot record result
   */
  SnapshotRecordResult snapshotRecord();

  /**
   * Snapshot record snapshot record result.
   *
   * @param spec the spec
   * @return the snapshot record result
   */
  SnapshotRecordResult snapshotRecord(RecordSpec spec);

  /**
   * Snapshot record snapshot record result.
   *
   * @param spec the spec
   * @return the snapshot record result
   */
  SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec);

  /**
   * Start recording.
   *
   * @param targetBaseUrl the target base url
   */
  void startRecording(String targetBaseUrl);

  /**
   * Start recording.
   *
   * @param spec the spec
   */
  void startRecording(RecordSpec spec);

  /**
   * Start recording.
   *
   * @param recordSpec the record spec
   */
  void startRecording(RecordSpecBuilder recordSpec);

  /**
   * Stop recording snapshot record result.
   *
   * @return the snapshot record result
   */
  SnapshotRecordResult stopRecording();

  /**
   * Gets recording status.
   *
   * @return the recording status
   */
  RecordingStatusResult getRecordingStatus();

  /**
   * Gets options.
   *
   * @return the options
   */
  Options getOptions();

  /** Shutdown server. */
  void shutdownServer();

  /**
   * Find unmatched stubs list stub mappings result.
   *
   * @return the list stub mappings result
   */
  ListStubMappingsResult findUnmatchedStubs();

  /**
   * Find all stubs by metadata list stub mappings result.
   *
   * @param pattern the pattern
   * @return the list stub mappings result
   */
  ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern);

  /**
   * Remove stubs by metadata.
   *
   * @param pattern the pattern
   */
  void removeStubsByMetadata(StringValuePattern pattern);

  /**
   * Import stubs.
   *
   * @param stubImport the stub import
   */
  void importStubs(StubImport stubImport);

  /**
   * Gets global settings.
   *
   * @return the global settings
   */
  GetGlobalSettingsResult getGlobalSettings();
}
