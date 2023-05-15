/*
 * Copyright (C) 2021-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.*;
import java.util.List;
import java.util.UUID;

public class DslWrapper implements Admin, Stubbing {

  protected Admin admin;
  protected Stubbing stubbing;

  @Override
  public void addStubMapping(StubMapping stubMapping) {
    admin.addStubMapping(stubMapping);
  }

  @Override
  public void editStubMapping(StubMapping stubMapping) {
    admin.editStubMapping(stubMapping);
  }

  @Override
  public void removeStubMapping(StubMapping stubbMapping) {
    admin.removeStubMapping(stubbMapping);
  }

  @Override
  public void removeStubMapping(UUID id) {
    admin.removeStubMapping(id);
  }

  @Override
  public ListStubMappingsResult listAllStubMappings() {
    return admin.listAllStubMappings();
  }

  @Override
  public SingleStubMappingResult getStubMapping(UUID id) {
    return admin.getStubMapping(id);
  }

  @Override
  public void saveMappings() {
    admin.saveMappings();
  }

  @Override
  public void resetRequests() {
    admin.resetRequests();
  }

  @Override
  public void resetScenarios() {
    admin.resetScenarios();
  }

  @Override
  public void resetMappings() {
    admin.resetMappings();
  }

  @Override
  public void resetAll() {
    admin.resetAll();
  }

  @Override
  public void resetToDefaultMappings() {
    admin.resetToDefaultMappings();
  }

  @Override
  public GetServeEventsResult getServeEvents() {
    return admin.getServeEvents();
  }

  @Override
  public GetServeEventsResult getServeEvents(ServeEventQuery query) {
    return admin.getServeEvents(query);
  }

  @Override
  public SingleServedStubResult getServedStub(UUID id) {
    return admin.getServedStub(id);
  }

  @Override
  public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
    return admin.countRequestsMatching(requestPattern);
  }

  @Override
  public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
    return admin.findRequestsMatching(requestPattern);
  }

  @Override
  public FindRequestsResult findUnmatchedRequests() {
    return admin.findUnmatchedRequests();
  }

  @Override
  public void removeServeEvent(UUID eventId) {
    admin.removeServeEvent(eventId);
  }

  @Override
  public FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern) {
    return admin.removeServeEventsMatching(requestPattern);
  }

  @Override
  public FindServeEventsResult removeServeEventsForStubsMatchingMetadata(
      StringValuePattern pattern) {
    return admin.removeServeEventsForStubsMatchingMetadata(pattern);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
    return admin.findTopNearMissesFor(loggedRequest);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
    return admin.findTopNearMissesFor(requestPattern);
  }

  @Override
  public FindNearMissesResult findNearMissesForUnmatchedRequests() {
    return admin.findNearMissesForUnmatchedRequests();
  }

  @Override
  public GetScenariosResult getAllScenarios() {
    return admin.getAllScenarios();
  }

  @Override
  public void resetScenario(String name) {
    admin.resetScenario(name);
  }

  @Override
  public void setScenarioState(String name, String state) {
    admin.setScenarioState(name, state);
  }

  @Override
  public void updateGlobalSettings(GlobalSettings settings) {
    admin.updateGlobalSettings(settings);
  }

  @Override
  public SnapshotRecordResult snapshotRecord() {
    return admin.snapshotRecord();
  }

  @Override
  public SnapshotRecordResult snapshotRecord(RecordSpec spec) {
    return admin.snapshotRecord(spec);
  }

  @Override
  public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
    return admin.snapshotRecord(spec);
  }

  @Override
  public void startRecording(String targetBaseUrl) {
    admin.startRecording(targetBaseUrl);
  }

  @Override
  public void startRecording(RecordSpec spec) {
    admin.startRecording(spec);
  }

  @Override
  public void startRecording(RecordSpecBuilder recordSpec) {
    admin.startRecording(recordSpec);
  }

  @Override
  public SnapshotRecordResult stopRecording() {
    return admin.stopRecording();
  }

  @Override
  public RecordingStatusResult getRecordingStatus() {
    return admin.getRecordingStatus();
  }

  @Override
  public Options getOptions() {
    return admin.getOptions();
  }

  @Override
  public void shutdownServer() {
    admin.shutdownServer();
  }

  @Override
  public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
    return admin.findAllStubsByMetadata(pattern);
  }

  @Override
  public void removeStubsByMetadata(StringValuePattern pattern) {
    admin.removeStubsByMetadata(pattern);
  }

  @Override
  public void importStubs(StubImport stubImport) {
    admin.importStubs(stubImport);
  }

  @Override
  public GetGlobalSettingsResult getGlobalSettings() {
    return admin.getGlobalSettings();
  }

  @Override
  public StubMapping givenThat(MappingBuilder mappingBuilder) {
    return stubbing.givenThat(mappingBuilder);
  }

  @Override
  public StubMapping stubFor(MappingBuilder mappingBuilder) {
    return stubbing.stubFor(mappingBuilder);
  }

  @Override
  public void editStub(MappingBuilder mappingBuilder) {
    stubbing.editStub(mappingBuilder);
  }

  @Override
  public void removeStub(MappingBuilder mappingBuilder) {
    stubbing.removeStub(mappingBuilder);
  }

  @Override
  public void removeStub(StubMapping mappingBuilder) {
    stubbing.removeStub(mappingBuilder);
  }

  @Override
  public List<StubMapping> getStubMappings() {
    return stubbing.getStubMappings();
  }

  @Override
  public StubMapping getSingleStubMapping(UUID id) {
    return stubbing.getSingleStubMapping(id);
  }

  @Override
  public List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern) {
    return stubbing.findStubMappingsByMetadata(pattern);
  }

  @Override
  public void removeStubMappingsByMetadata(StringValuePattern pattern) {
    stubbing.removeStubMappingsByMetadata(pattern);
  }

  @Override
  public void verify(RequestPatternBuilder requestPatternBuilder) {
    stubbing.verify(requestPatternBuilder);
  }

  @Override
  public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
    stubbing.verify(count, requestPatternBuilder);
  }

  @Override
  public void verify(
      CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder) {
    stubbing.verify(countMatchingStrategy, requestPatternBuilder);
  }

  @Override
  public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
    return stubbing.findAll(requestPatternBuilder);
  }

  @Override
  public List<ServeEvent> getAllServeEvents() {
    return stubbing.getAllServeEvents();
  }

  @Override
  public void setGlobalFixedDelay(int milliseconds) {
    stubbing.setGlobalFixedDelay(milliseconds);
  }

  @Override
  public List<LoggedRequest> findAllUnmatchedRequests() {
    return stubbing.findAllUnmatchedRequests();
  }

  @Override
  public List<NearMiss> findNearMissesForAllUnmatchedRequests() {
    return stubbing.findNearMissesForAllUnmatchedRequests();
  }

  @Override
  public List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest) {
    return stubbing.findNearMissesFor(loggedRequest);
  }

  @Override
  public List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder) {
    return stubbing.findAllNearMissesFor(requestPatternBuilder);
  }
}
