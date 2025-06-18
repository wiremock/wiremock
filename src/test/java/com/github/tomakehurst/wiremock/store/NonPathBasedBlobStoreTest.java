/*
 * Copyright (C) 2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.StreamSources;
import com.github.tomakehurst.wiremock.store.files.BlobStoreFileSource;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NonPathBasedBlobStoreTest {

  @Test
  void canUseANonPathBasedBlobStoreTest() {
    WireMockServer wireMockServer =
        new WireMockServer(
            wireMockConfig().fileSource(new BlobStoreFileSource(new NonPathBasedBlobStore())));
    try {
      wireMockServer.start();
    } finally {
      wireMockServer.stop();
    }
  }

  static class NonPathBasedBlobStore implements BlobStore {
    @Override
    public Optional<InputStream> getStream(String key) {
      return Optional.empty();
    }

    @Override
    public InputStreamSource getStreamSource(String key) {
      return StreamSources.empty();
    }

    @Override
    public Stream<String> getAllKeys() {
      return Stream.empty();
    }

    @Override
    public Optional<byte[]> get(String key) {
      return Optional.empty();
    }

    @Override
    public void put(String key, byte[] content) {}

    @Override
    public void remove(String key) {}

    @Override
    public void clear() {}
  }
}
