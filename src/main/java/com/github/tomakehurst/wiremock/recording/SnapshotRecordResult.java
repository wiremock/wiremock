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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** The type Snapshot record result. */
@JsonDeserialize(using = SnapshotRecordResultDeserialiser.class)
public class SnapshotRecordResult {

  /** The Stub mappings. */
  protected final List<StubMapping> stubMappings;

  /**
   * Instantiates a new Snapshot record result.
   *
   * @param mappings the mappings
   */
  protected SnapshotRecordResult(List<StubMapping> mappings) {
    this.stubMappings = mappings;
  }

  /**
   * Gets stub mappings.
   *
   * @return the stub mappings
   */
  @JsonIgnore
  public List<StubMapping> getStubMappings() {
    return stubMappings;
  }

  /**
   * Full snapshot record result.
   *
   * @param stubMappings the stub mappings
   * @return the snapshot record result
   */
  public static SnapshotRecordResult full(List<StubMapping> stubMappings) {
    return new Full(stubMappings);
  }

  /**
   * Ids from mappings snapshot record result.
   *
   * @param stubMappings the stub mappings
   * @return the snapshot record result
   */
  public static SnapshotRecordResult idsFromMappings(List<StubMapping> stubMappings) {
    return new Ids(stubMappings.stream().map(StubMapping::getId).collect(Collectors.toList()));
  }

  /**
   * Ids snapshot record result.
   *
   * @param ids the ids
   * @return the snapshot record result
   */
  public static SnapshotRecordResult ids(List<UUID> ids) {
    return new Ids(ids);
  }

  /**
   * Empty snapshot record result.
   *
   * @return the snapshot record result
   */
  public static SnapshotRecordResult empty() {
    return full(Collections.emptyList());
  }

  /** The type Full. */
  public static class Full extends SnapshotRecordResult {

    /**
     * Instantiates a new Full.
     *
     * @param mappings the mappings
     */
    public Full(List<StubMapping> mappings) {
      super(mappings);
    }

    /**
     * Gets mappings.
     *
     * @return the mappings
     */
    public List<StubMapping> getMappings() {
      return stubMappings;
    }
  }

  /** The type Ids. */
  public static class Ids extends SnapshotRecordResult {

    private final List<UUID> ids;

    /**
     * Instantiates a new Ids.
     *
     * @param ids the ids
     */
    public Ids(List<UUID> ids) {
      super(Collections.emptyList());
      this.ids = ids;
    }

    /**
     * Gets ids.
     *
     * @return the ids
     */
    public List<UUID> getIds() {
      return ids;
    }
  }
}
