/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonIgnoreProperties({"$schema", "meta", "uuid"})
@JsonDeserialize() // stops infinite recursion when deserializing as StubMappingOrMappings
public class StubMappingCollection implements StubMappingOrMappings {

  private List<StubMapping> mappings;

  @Override
  public List<StubMapping> getMappingOrMappings() {
    return getMappings();
  }

  @Override
  public boolean isMulti() {
    return true;
  }

  public List<StubMapping> getMappings() {
    return mappings;
  }

  public void setMappings(List<StubMapping> mappings) {
    this.mappings = mappings;
  }
}
