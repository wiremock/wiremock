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
package com.github.tomakehurst.wiremock.store.files;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.store.BlobStore;
import org.wiremock.annotations.Beta;

import java.io.InputStream;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class BlobStoreBinaryFile extends BinaryFile {

  private final BlobStore blobStore;
  private final String path;

  public BlobStoreBinaryFile(BlobStore blobStore, String path) {
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
}
