/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Optional;

/**
 * A result class representing a single {@link StubMapping}.
 *
 * <p>Extends {@link SingleItemResult} to serve as a data transfer object (DTO) for admin API
 * responses that return a single stub mapping. It inherits the {@code @JsonValue} serialization
 * behavior, serializing directly to the contained {@code StubMapping} object.
 *
 * @see SingleItemResult
 * @see StubMapping
 */
public class SingleStubMappingResult extends SingleItemResult<StubMapping> {

  /**
   * Constructs a new SingleStubMappingResult.
   *
   * @param item The {@link StubMapping} to be wrapped.
   */
  @JsonCreator
  public SingleStubMappingResult(StubMapping item) {
    super(item);
  }

  /**
   * A factory method to create a result from an {@link Optional}.
   *
   * @param optional An {@code Optional<StubMapping>}.
   * @return A new {@code SingleStubMappingResult} containing the stub mapping if present, or
   *     containing null if the optional is empty.
   */
  public static SingleStubMappingResult fromOptional(Optional<StubMapping> optional) {
    return new SingleStubMappingResult(optional.orElse(null));
  }
}
