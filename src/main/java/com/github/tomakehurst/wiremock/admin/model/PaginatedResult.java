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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.Paginator;
import java.util.List;

/**
 * An abstract base class for paginated API results.
 *
 * <p>It provides the common structure for a paginated response, including a {@link Meta} object
 * with the total item count and the selection of items for the current page.
 *
 * @param <T> The type of the items being paginated.
 */
@JsonInclude(NON_NULL)
public abstract class PaginatedResult<T> {

  private final List<T> selection;
  private final Meta meta;

  /**
   * Constructs a new PaginatedResult from a {@link Paginator}.
   *
   * @param paginator The paginator used to select the current page and get the total count.
   */
  protected PaginatedResult(Paginator<T> paginator) {
    selection = paginator.select();
    meta = new Meta(paginator.getTotal());
  }

  /**
   * Constructs a new PaginatedResult with a pre-defined list and metadata.
   *
   * <p>Typically used by subclasses for JSON deserialization.
   *
   * @param source The list of items for the current page.
   * @param meta The pagination metadata.
   */
  protected PaginatedResult(List<T> source, Meta meta) {
    this.selection = source;
    this.meta = meta;
  }

  public Meta getMeta() {
    return meta;
  }

  /**
   * Gets the selected items for the current page.
   *
   * <p>This method is intended for use by subclasses, which typically expose this list via a more
   * specifically named public getter (e.g., {@code getMappings()}).
   *
   * @return a list of items for the current page.
   */
  protected List<T> select() {
    return selection;
  }

  /** A data holder for pagination metadata. */
  public static class Meta {

    public final int total;

    /**
     * Constructs a new Meta instance.
     *
     * @param total The total number of available items across all pages.
     */
    @JsonCreator
    public Meta(@JsonProperty("total") int total) {
      this.total = total;
    }
  }
}
