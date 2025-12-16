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
package com.github.tomakehurst.wiremock.extension;

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExtensionsTest {

  @Test
  void extensionsAreReturnedInRegistrationOrder() {

    WireMockConfiguration options =
        WireMockConfiguration.options()
            .extensions(() -> "e1", () -> "e2")
            .extensions(() -> "e3")
            .extensions(() -> "e4", () -> "e5")
            .extensions(
                services -> List.of(() -> "e6", () -> "e7"), services -> List.of(() -> "e8"));

    Extensions extensions =
        new Extensions(
            options.getDeclaredExtensions(),
            new FakeAdmin(),
            options,
            options.getStores(),
            options.filesRoot().child(FILES_ROOT));

    extensions.load();

    List<String> allExtensions = extensions.ofType(Extension.class).keySet().stream().toList();

    assertEquals(
        List.of(
            "e1",
            "e2",
            "e3",
            "e4",
            "e5",
            "e6",
            "e7",
            "e8",
            "response-template",
            "webhook",
            "proxied-hostname-rewrite"),
        allExtensions);
  }

  private static class FakeAdmin implements Admin {
    @Override
    public void addStubMapping(StubMapping stubMapping) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void editStubMapping(StubMapping stubMapping) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeStubMapping(StubMapping stubbMapping) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeStubMapping(UUID id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ListStubMappingsResult listAllStubMappings() {
      throw new UnsupportedOperationException();
    }

    @Override
    public SingleStubMappingResult getStubMapping(UUID id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void saveMappings() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetRequests() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetScenarios() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetMappings() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetAll() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetToDefaultMappings() {
      throw new UnsupportedOperationException();
    }

    @Override
    public GetServeEventsResult getServeEvents() {
      throw new UnsupportedOperationException();
    }

    @Override
    public GetServeEventsResult getServeEvents(ServeEventQuery query) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SingleServedStubResult getServedStub(UUID id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindRequestsResult findUnmatchedRequests() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeServeEvent(UUID eventId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindServeEventsResult removeServeEventsForStubsMatchingMetadata(
        StringValuePattern pattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FindNearMissesResult findNearMissesForUnmatchedRequests() {
      throw new UnsupportedOperationException();
    }

    @Override
    public GetScenariosResult getAllScenarios() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetScenario(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setScenarioState(String name, String state) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void updateGlobalSettings(GlobalSettings settings) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SnapshotRecordResult snapshotRecord() {
      throw new UnsupportedOperationException();
    }

    @Override
    public SnapshotRecordResult snapshotRecord(RecordSpec spec) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void startRecording(String targetBaseUrl) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void startRecording(RecordSpec spec) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void startRecording(RecordSpecBuilder recordSpec) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SnapshotRecordResult stopRecording() {
      throw new UnsupportedOperationException();
    }

    @Override
    public RecordingStatusResult getRecordingStatus() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Options getOptions() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void shutdownServer() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ListStubMappingsResult findUnmatchedStubs() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeStubsByMetadata(StringValuePattern pattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void importStubs(StubImport stubImport) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeStubMappings(List<StubMapping> stubMappings) {
      throw new UnsupportedOperationException();
    }

    @Override
    public GetGlobalSettingsResult getGlobalSettings() {
      throw new UnsupportedOperationException();
    }

    @Override
    public SendChannelMessageResult sendChannelMessage(
        com.github.tomakehurst.wiremock.websocket.ChannelType type,
        RequestPattern requestPattern,
        String message) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SendChannelMessageResult sendWebSocketMessage(
        RequestPattern requestPattern, String message) {
      throw new UnsupportedOperationException();
    }

    @Override
    public com.github.tomakehurst.wiremock.websocket.MessageChannels getMessageChannels() {
      throw new UnsupportedOperationException();
    }
  }
}
