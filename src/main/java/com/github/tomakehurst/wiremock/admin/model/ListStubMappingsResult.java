/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.LimitAndOffsetPaginator;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;

/**
 * A paginated result class for listing {@link StubMapping} objects.
 *
 * <p>This serves as a data transfer object (DTO) for the admin API endpoint that lists all
 * registered stub mappings, supporting pagination via limit and offset.
 *
 * @see StubMapping
 * @see PaginatedResult
 * @see LimitAndOffsetPaginator
 */
public class ListStubMappingsResult extends PaginatedResult<StubMapping> {

  /**
   * Constructs a new ListStubMappingsResult for JSON deserialization.
   *
   * @param mappings The full list of stub mappings.
   * @param meta The metadata including pagination details.
   */
  @JsonCreator
  public ListStubMappingsResult(
      @JsonProperty("mappings") List<StubMapping> mappings, @JsonProperty("meta") Meta meta) {
    super(mappings, meta);
  }

  /**
   * Constructs a new ListStubMappingsResult from a paginator.
   *
   * @param paginator The paginator for the stub mappings.
   */
  public ListStubMappingsResult(LimitAndOffsetPaginator<StubMapping> paginator) {
    super(paginator);
  }

  public List<StubMapping> getMappings() {
    return select();
  }
}
