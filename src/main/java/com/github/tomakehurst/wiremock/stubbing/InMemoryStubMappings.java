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

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.store.InMemoryStubMappingStore;
import com.github.tomakehurst.wiremock.store.files.FileSourceBlobStore;
import java.util.Collections;

public class InMemoryStubMappings extends StoreBackedStubMappings {

  public InMemoryStubMappings() {
    super(
        new InMemoryStubMappingStore(),
        new InMemoryScenarios(),
        Collections.<String, RequestMatcherExtension>emptyMap(),
        Collections.<String, ResponseDefinitionTransformer>emptyMap(),
        new FileSourceBlobStore(new SingleRootFileSource(".")),
        Collections.<StubLifecycleListener>emptyList());
  }
}
