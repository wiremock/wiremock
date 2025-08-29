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

import com.github.tomakehurst.wiremock.admin.Paginator;
import java.util.List;

/**
 * An abstract base class for paginated results that depend on the request journal.
 *
 * <p>This class extends {@link PaginatedResult} to include a flag indicating whether the request
 * journal is disabled, as this affects the availability of the data.
 *
 * @param <T> The type of the items being paginated.
 */
public abstract class RequestJournalDependentResult<T> extends PaginatedResult<T> {

  private final boolean requestJournalDisabled;

  /**
   * Constructs a new RequestJournalDependentResult from a paginator.
   *
   * @param paginator The paginator for the result items.
   * @param requestJournalDisabled True if the request journal is disabled.
   */
  protected RequestJournalDependentResult(Paginator<T> paginator, boolean requestJournalDisabled) {
    super(paginator);
    this.requestJournalDisabled = requestJournalDisabled;
  }

  /**
   * Constructs a new RequestJournalDependentResult with a pre-defined list and metadata.
   *
   * <p>Typically used by subclasses for JSON deserialization.
   *
   * @param source The list of items for the current page.
   * @param meta The pagination metadata.
   * @param requestJournalDisabled True if the request journal is disabled.
   */
  protected RequestJournalDependentResult(
      List<T> source, Meta meta, boolean requestJournalDisabled) {
    super(source, meta);
    this.requestJournalDisabled = requestJournalDisabled;
  }

  public boolean isRequestJournalDisabled() {
    return requestJournalDisabled;
  }
}
