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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A generic wrapper class for a single item result.
 *
 * <p>This class is typically used as a data transfer object (DTO) for admin API responses that
 * return a single entity. Due to the {@code @JsonValue} annotation on the getter, an instance of
 * this class will be serialized directly as the wrapped item, rather than as an object containing
 * the item.
 *
 * @param <T> The type of the wrapped item.
 */
@JsonInclude(NON_NULL)
public class SingleItemResult<T> {

  private final T item;

  /**
   * Constructs a new SingleItemResult.
   *
   * @param item The single item to be wrapped in the result.
   */
  public SingleItemResult(T item) {
    this.item = item;
  }

  @JsonValue
  public T getItem() {
    return item;
  }

  @JsonIgnore
  public boolean isPresent() {
    return item != null;
  }
}
