/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface StubMappingStore {

  Stream<StubMapping> getAll();

  Optional<StubMapping> get(UUID id);

  default Stream<StubMapping> findAllMatchingRequest(
      Request request, Map<String, RequestMatcherExtension> customMatchers) {
    return getAll()
        .filter(
            stubMapping -> stubMapping.getRequest().match(request, customMatchers).isExactMatch());
  }

  void add(StubMapping stub);

  void replace(StubMapping existing, StubMapping updated);

  void remove(StubMapping stubMapping);

  void clear();
}
