/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.StubMappingStore;
import java.util.List;
import java.util.Map;

public class StoreBackedStubMappings extends AbstractStubMappings {

  public StoreBackedStubMappings(
      StubMappingStore store,
      Scenarios scenarios,
      Map<String, RequestMatcherExtension> customMatchers,
      Map<String, ResponseDefinitionTransformer> transformers,
      Map<String, ResponseDefinitionTransformerV2> v2transformers,
      BlobStore filesBlobStore,
      List<StubLifecycleListener> stubLifecycleListeners,
      boolean failIfMultipleMappingsMatch) {
    super(
        store,
        scenarios,
        customMatchers,
        transformers,
        v2transformers,
        filesBlobStore,
        stubLifecycleListeners,
        failIfMultipleMappingsMatch);
  }
}
