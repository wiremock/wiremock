/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Pair;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.files.BlobStoreFileSource;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.function.Function;

/**
 * Applies all registered StubMappingTransformer extensions against a stub mapping when applicable,
 * passing them any supplied Parameters.
 */
class SnapshotStubMappingTransformerRunner
    implements Function<Pair<ServeEvent, StubMapping>, StubGenerationResult> {
  private final FileSource filesRoot;
  private final Parameters parameters;
  private final Iterable<StubMappingTransformer> registeredTransformers;
  private final List<String> requestedTransformers;

  SnapshotStubMappingTransformerRunner(Iterable<StubMappingTransformer> registeredTransformers) {
    this(registeredTransformers, null, null, null);
  }

  SnapshotStubMappingTransformerRunner(
      Iterable<StubMappingTransformer> registeredTransformers,
      List<String> requestedTransformers,
      Parameters parameters,
      BlobStore filesBlobStore) {
    this.requestedTransformers = requestedTransformers;
    this.registeredTransformers = registeredTransformers;
    this.parameters = parameters;
    this.filesRoot = new BlobStoreFileSource(filesBlobStore);
  }

  @Override
  public StubGenerationResult apply(Pair<ServeEvent, StubMapping> serveEventToStubMapping) {
    StubMapping stubMapping = serveEventToStubMapping.b;
    for (StubMappingTransformer transformer : registeredTransformers) {
      if (stubMapping != null
          && (transformer.applyGlobally()
              || (requestedTransformers != null
                  && requestedTransformers.contains(transformer.getName())))) {
        StubGenerationResult result =
            transformer.transform(stubMapping, filesRoot, parameters, serveEventToStubMapping.a);
        if (result instanceof StubGenerationResult.Success success) {
          stubMapping = success.stubMapping();
        } else if (result instanceof StubGenerationResult.Failure failure) {
          return failure;
        } else {
          throw new IllegalStateException("Unexpected result: " + result);
        }
      }
    }

    return new StubGenerationResult.Success(stubMapping);
  }
}
