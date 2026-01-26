/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
  @JsonSubTypes.Type(SnapshotRecordResult.Full.class),
  @JsonSubTypes.Type(SnapshotRecordResult.Ids.class),
})
public class SnapshotRecordResult {

  protected final List<StubMapping> stubMappings;
  protected final List<? extends RecordError> errors;

  protected SnapshotRecordResult(List<StubMapping> mappings, List<? extends RecordError> errors) {
    this.stubMappings = mappings;
    this.errors = errors;
  }

  @JsonIgnore
  public List<StubMapping> getStubMappings() {
    return stubMappings;
  }

  public List<? extends RecordError> getErrors() {
    return errors;
  }

  public static SnapshotRecordResult full(
      List<StubMapping> stubMappings, List<? extends RecordError> errors) {
    return new Full(stubMappings, errors);
  }

  public static SnapshotRecordResult idsFromMappings(
      List<StubMapping> stubMappings, List<? extends RecordError> errors) {
    return new Ids(
        stubMappings.stream().map(StubMapping::getId).collect(Collectors.toList()), errors);
  }

  public static SnapshotRecordResult empty() {
    return full(Collections.emptyList(), Collections.emptyList());
  }

  public static class Full extends SnapshotRecordResult {

    @JsonCreator
    public Full(
        @JsonProperty("mappings") List<StubMapping> mappings,
        @JsonProperty("errors") List<? extends RecordError> errors) {
      super(mappings, errors);
    }

    public List<StubMapping> getMappings() {
      return stubMappings;
    }
  }

  public static class Ids extends SnapshotRecordResult {

    private final List<UUID> ids;

    @JsonCreator
    public Ids(
        @JsonProperty("ids") List<UUID> ids,
        @JsonProperty("errors") List<? extends RecordError> errors) {
      super(Collections.emptyList(), errors);
      this.ids = ids;
    }

    public List<UUID> getIds() {
      return ids;
    }
  }
}
