/*
 * Copyright (C) 2022-2025 Thomas Akehurst
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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.store.BlobStore;
import java.io.InputStream;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class BlobStoreTextFile extends TextFile {

  private final BlobStore blobStore;
  private final String path;

  public BlobStoreTextFile(BlobStore blobStore, String path) {
    super(null);
    this.blobStore = blobStore;
    this.path = path;
  }

  @Override
  public byte[] readContents() {
    return blobStore.get(path).orElseThrow(() -> new NotFoundException(path + " not found"));
  }

  @Override
  public String name() {
    return path;
  }

  @Override
  public String toString() {
    return name();
  }

  @Override
  public InputStream getStream() {
    return blobStore.getStream(path).orElseThrow(() -> new NotFoundException(path + " not found"));
  }

  @Override
  public String readContentsAsString() {
    return new String(readContents(), UTF_8);
  }

  @Override
  public String getPath() {
    return path;
  }
}
