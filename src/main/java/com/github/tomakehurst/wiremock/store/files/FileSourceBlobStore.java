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

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.store.BlobStore;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class FileSourceBlobStore implements BlobStore {

  private final FileSource fileSource;

  FileSourceBlobStore(String root) {
    this.fileSource = new SingleRootFileSource(root);
  }

  public FileSourceBlobStore(FileSource fileSource) {
    this.fileSource = fileSource;
  }

  @Override
  public Optional<InputStream> getStream(String key) {
    return Optional.of(fileSource.getBinaryFileNamed(key).getStream());
  }

  @Override
  public InputStreamSource getStreamSource(String key) {
    return StreamSources.forBlobStoreItem(this, key);
  }

  @Override
  public Stream<String> getAllKeys() {
    return fileSource.listFilesRecursively().stream().map(TextFile::getPath);
  }

  @Override
  public Optional<byte[]> get(String key) {
    return Optional.of(fileSource.getBinaryFileNamed(key).readContents());
  }

  @Override
  public void put(String key, byte[] content) {
    fileSource.writeBinaryFile(key, content);
  }

  @Override
  public void remove(String key) {
    fileSource.deleteFile(key);
  }

  @Override
  public void clear() {
    fileSource.listFilesRecursively().forEach(file -> fileSource.deleteFile(file.getPath()));
  }
}
