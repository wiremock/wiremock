/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.message;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.Metadata;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@JsonDeserialize()
class SingleMessageStubMappingWrapper implements MessageStubMappingOrMappings {

  private final MessageStubMapping mapping;

  @JsonCreator
  public SingleMessageStubMappingWrapper(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("priority") Integer priority,
      @JsonProperty("trigger") MessageTrigger trigger,
      @JsonProperty("actions") List<MessageAction> actions,
      @JsonProperty("metadata") Metadata metadata,
      @JsonProperty("insertionIndex") Long insertionIndex) {
    this.mapping =
        new MessageStubMapping(
            id, name, priority, trigger, actions, metadata, getFirstNonNull(insertionIndex, 0L));
  }

  @Override
  public List<MessageStubMapping> getMappingOrMappings() {
    return Collections.singletonList(mapping);
  }

  @Override
  public boolean isMulti() {
    return false;
  }
}
