/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store.files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FileSourceBlobStoreTest {
  private static final String DIRECTORY_PATH =
      "src/test/java/com/github/tomakehurst/wiremock/store/files/test/empty";

  @Test
  public void createFileSourceBlobStoreWithEmptyDirectory_get() {
    FileSourceBlobStore fileSourceBlobStore = new FileSourceBlobStore(DIRECTORY_PATH);

    assertDoesNotThrow(
        () -> {
          Optional<byte[]> result = fileSourceBlobStore.get("any-key");
          assertEquals(Optional.empty(), result);
        });
  }

  @Test
  public void createFileSourceBlobStoreWithEmptyDirectory_getStream() {
    FileSourceBlobStore fileSourceBlobStore = new FileSourceBlobStore(DIRECTORY_PATH);

    assertDoesNotThrow(
        () -> {
          Optional<InputStream> result = fileSourceBlobStore.getStream("any-key");
          assertEquals(Optional.empty(), result);
        });
  }
}
