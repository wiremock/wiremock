/*
 * Copyright (C) 2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.store.InMemoryRequestJournalStore;
import com.github.tomakehurst.wiremock.store.RequestJournalStore;
import com.google.common.base.Optional;
import java.util.Map;

public class InMemoryRequestJournal extends AbstractRequestJournal {

  public InMemoryRequestJournal(
      Optional<Integer> maxEntries,
      Map<String, RequestMatcherExtension> customMatchers,
      RequestJournalStore store) {
    super(maxEntries, customMatchers, store);
  }

  public InMemoryRequestJournal(
      Optional<Integer> maxEntries, Map<String, RequestMatcherExtension> customMatchers) {
    super(maxEntries, customMatchers, new InMemoryRequestJournalStore());
  }
}
