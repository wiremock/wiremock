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
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Optional;

/**
 * A result class representing a single {@link ServeEvent}.
 *
 * <p>This class extends {@link SingleItemResult} and is used as a data transfer object (DTO) for
 * admin API responses returning a single served stub. It inherits the {@code @JsonValue}
 * serialization behavior from its parent, meaning it serializes directly to the contained {@code
 * ServeEvent} object.
 *
 * @see SingleItemResult
 * @see ServeEvent
 */
public class SingleServedStubResult extends SingleItemResult<ServeEvent> {

  /**
   * Constructs a new SingleServedStubResult.
   *
   * @param item The {@link ServeEvent} to be wrapped in the result.
   */
  @JsonCreator
  public SingleServedStubResult(ServeEvent item) {
    super(item);
  }

  /**
   * A convenience factory method to create a result from an {@link Optional}.
   *
   * @param servedStub An {@code Optional<ServeEvent>}.
   * @return A new {@code SingleServedStubResult} containing the serve event if present, or
   *     containing null if the optional is empty.
   */
  public static SingleServedStubResult fromOptional(Optional<ServeEvent> servedStub) {
    return new SingleServedStubResult(servedStub.orElse(null));
  }
}
