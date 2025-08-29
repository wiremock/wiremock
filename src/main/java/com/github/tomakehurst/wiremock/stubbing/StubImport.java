/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The type Stub import. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StubImport {

  private final List<StubMapping> mappings;
  private final Options importOptions;

  /**
   * Instantiates a new Stub import.
   *
   * @param mappings the mappings
   * @param importOptions the import options
   */
  public StubImport(
      @JsonProperty("mappings") List<StubMapping> mappings,
      @JsonProperty("importOptions") Options importOptions) {
    this.mappings = mappings;
    this.importOptions = importOptions;
  }

  /**
   * Gets mappings.
   *
   * @return the mappings
   */
  public List<StubMapping> getMappings() {
    return mappings;
  }

  /**
   * Gets import options.
   *
   * @return the import options
   */
  public Options getImportOptions() {
    return importOptions;
  }

  /**
   * Stub import stub import builder.
   *
   * @return the stub import builder
   */
  public static StubImportBuilder stubImport() {
    return new StubImportBuilder();
  }

  /** The type Options. */
  public static class Options {

    /** The enum Duplicate policy. */
    public enum DuplicatePolicy {
      /** Overwrite duplicate policy. */
      OVERWRITE,
      /** Ignore duplicate policy. */
      IGNORE
    }

    private final DuplicatePolicy duplicatePolicy;
    private final Boolean deleteAllNotInImport;

    /**
     * Instantiates a new Options.
     *
     * @param duplicatePolicy the duplicate policy
     * @param deleteAllNotInImport the delete all not in import
     */
    public Options(
        @JsonProperty("duplicatePolicy") DuplicatePolicy duplicatePolicy,
        @JsonProperty("deleteAllNotInImport") Boolean deleteAllNotInImport) {
      this.duplicatePolicy = duplicatePolicy;
      this.deleteAllNotInImport = deleteAllNotInImport;
    }

    /**
     * Gets duplicate policy.
     *
     * @return the duplicate policy
     */
    public DuplicatePolicy getDuplicatePolicy() {
      return duplicatePolicy;
    }

    /**
     * Gets delete all not in import.
     *
     * @return the delete all not in import
     */
    public Boolean getDeleteAllNotInImport() {
      return deleteAllNotInImport;
    }

    /** The constant DEFAULTS. */
    public static final Options DEFAULTS = new Options(DuplicatePolicy.OVERWRITE, false);
  }
}
