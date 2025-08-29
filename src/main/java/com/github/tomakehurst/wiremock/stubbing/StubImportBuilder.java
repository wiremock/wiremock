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

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.util.ArrayList;
import java.util.List;

/** The type Stub import builder. */
public class StubImportBuilder {

  private List<StubMapping> mappings = new ArrayList<>();
  private StubImport.Options.DuplicatePolicy duplicatePolicy =
      StubImport.Options.DuplicatePolicy.OVERWRITE;
  private Boolean deleteAllNotInImport = false;

  /** Instantiates a new Stub import builder. */
  StubImportBuilder() {}

  /**
   * Stub stub import builder.
   *
   * @param stubMappingBuilder the stub mapping builder
   * @return the stub import builder
   */
  public StubImportBuilder stub(MappingBuilder stubMappingBuilder) {
    mappings.add(stubMappingBuilder.build());
    return this;
  }

  /**
   * Stub stub import builder.
   *
   * @param stubMapping the stub mapping
   * @return the stub import builder
   */
  public StubImportBuilder stub(StubMapping stubMapping) {
    mappings.add(stubMapping);
    return this;
  }

  /**
   * Ignore existing stub import builder.
   *
   * @return the stub import builder
   */
  public StubImportBuilder ignoreExisting() {
    duplicatePolicy = StubImport.Options.DuplicatePolicy.IGNORE;
    return this;
  }

  /**
   * Overwrite existing stub import builder.
   *
   * @return the stub import builder
   */
  public StubImportBuilder overwriteExisting() {
    duplicatePolicy = StubImport.Options.DuplicatePolicy.OVERWRITE;
    return this;
  }

  /**
   * Delete all existing stubs not in import stub import builder.
   *
   * @return the stub import builder
   */
  public StubImportBuilder deleteAllExistingStubsNotInImport() {
    deleteAllNotInImport = true;
    return this;
  }

  /**
   * Do not delete existing stubs stub import builder.
   *
   * @return the stub import builder
   */
  public StubImportBuilder doNotDeleteExistingStubs() {
    deleteAllNotInImport = false;
    return this;
  }

  /**
   * Build stub import.
   *
   * @return the stub import
   */
  public StubImport build() {
    return new StubImport(mappings, new StubImport.Options(duplicatePolicy, deleteAllNotInImport));
  }
}
