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
package com.github.tomakehurst.wiremock.store.files;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.store.BlobStore;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class BlobStoreFileSource implements FileSource {

  private final BlobStore blobStore;

  public BlobStoreFileSource(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  @Override
  public BinaryFile getBinaryFileNamed(String name) {
    return new BlobStoreBinaryFile(blobStore, name);
  }

  @Override
  public TextFile getTextFileNamed(String name) {
    return new BlobStoreTextFile(blobStore, name);
  }

  @Override
  public void createIfNecessary() {}

  @Override
  public FileSource child(String subDirectoryName) {
    return this;
  }

  @Override
  public String getPath() {
    if (blobStore instanceof PathBased) {
      return ((PathBased) blobStore).getPath();
    }

    return "";
  }

  @Override
  public URI getUri() {
    return null;
  }

  @Override
  public List<TextFile> listFilesRecursively() {
    return blobStore
        .getAllKeys()
        .map(path -> new BlobStoreTextFile(blobStore, path))
        .collect(Collectors.toList());
  }

  @Override
  public void writeTextFile(String name, String contents) {
    blobStore.put(name, Strings.bytesFromString(contents));
  }

  @Override
  public void writeBinaryFile(String name, byte[] contents) {
    blobStore.put(name, contents);
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public void deleteFile(String name) {
    blobStore.remove(name);
  }
}
