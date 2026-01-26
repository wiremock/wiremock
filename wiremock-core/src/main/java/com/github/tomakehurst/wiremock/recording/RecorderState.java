/*
 * Copyright (C) 2022-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.UUID;

public class RecorderState {

  private final RecordingStatus status;
  private final StubMapping proxyMapping;
  private final RecordSpec spec;
  private final UUID startingServeEventId;
  private final UUID finishingServeEventId;

  public RecorderState(
      @JsonProperty("status") RecordingStatus status,
      @JsonProperty("proxyMapping") StubMapping proxyMapping,
      @JsonProperty("spec") RecordSpec spec,
      @JsonProperty("startingServeEventId") UUID startingServeEventId,
      @JsonProperty("finishingServeEventId") UUID finishingServeEventId) {
    this.status = status;
    this.proxyMapping = proxyMapping;
    this.spec = spec;
    this.startingServeEventId = startingServeEventId;
    this.finishingServeEventId = finishingServeEventId;
  }

  public static RecorderState initial() {
    return new RecorderState(RecordingStatus.NeverStarted, null, null, null, null);
  }

  public RecorderState start(UUID startingServeEventId, StubMapping proxyMapping, RecordSpec spec) {
    return new RecorderState(
        RecordingStatus.Recording, proxyMapping, spec, startingServeEventId, null);
  }

  public RecorderState stop(UUID finishingServeEventId) {
    return new RecorderState(
        RecordingStatus.Stopped, proxyMapping, spec, startingServeEventId, finishingServeEventId);
  }

  public RecordingStatus getStatus() {
    return status;
  }

  public StubMapping getProxyMapping() {
    return proxyMapping;
  }

  public RecordSpec getSpec() {
    return spec;
  }

  public UUID getStartingServeEventId() {
    return startingServeEventId;
  }

  public UUID getFinishingServeEventId() {
    return finishingServeEventId;
  }
}
