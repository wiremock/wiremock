/*
 * Copyright (C) 2022-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.Exceptions.uncheck;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.store.ObjectStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class FileSourceJsonObjectStore implements ObjectStore, PathBased {

  private final FileSource fileSource;
  private final KeyLocks keyLocks = new KeyLocks();

  public FileSourceJsonObjectStore(String root) {
    this.fileSource = new SingleRootFileSource(root);
  }

  public FileSourceJsonObjectStore(FileSource fileSource) {
    this.fileSource = fileSource;
  }

  @Override
  public Stream<String> getAllKeys() {
    final String rootPath = new File(fileSource.getUri().getSchemeSpecificPart()).getPath();
    return fileSource.listFilesRecursively().stream()
        .map(TextFile::getPath)
        .map(path -> path.substring(rootPath.length() + 1));
  }

  @Override
  public Optional<Object> get(String key) {
    return get(key, Object.class);
  }

  @Override
  public <T> Optional<T> get(String key, Class<T> type) {
    return getBytes(key).map(bytes -> uncheck(() -> Json.read(bytes, type), type));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T compute(String key, Function<T, T> valueFunction) {
    return keyLocks.withLock(
        key,
        () -> {
          final T newValue =
              get(key)
                  .map(value -> valueFunction.apply((T) value))
                  .orElseGet(() -> valueFunction.apply(null));
          put(key, newValue);
          return newValue;
        });
  }

  private Optional<byte[]> getBytes(String key) {
    try {
      return Optional.of(fileSource.getBinaryFileNamed(createFileName(key)).readContents());
    } catch (Exception exception) {
      if (!(exception instanceof FileNotFoundException)) {
        notifier()
            .error("Error when working with FileSource:\n" + Json.write(exception.getMessage()));
        return Optional.of(throwUnchecked(exception, byte[].class));
      } else {
        return Optional.empty();
      }
    }
  }

  @Override
  public void put(String key, Object content) {
    final String json = Json.write(content);
    final String fileName = createFileName(key);
    fileSource.writeBinaryFile(fileName, Strings.bytesFromString(json));
  }

  @Override
  public Optional<Object> getAndPut(String key, Object content) {
    return keyLocks.withLock(
        key,
        () -> {
          final Optional<Object> value = get(key);
          put(key, content);
          return value;
        });
  }

  @Override
  public void remove(String key) {
    fileSource.deleteFile(createFileName(key));
  }

  @Override
  public Optional<Object> getAndRemove(String key) {
    return keyLocks.withLock(
        key,
        () -> {
          final Optional<Object> value = get(key);
          remove(key);
          return value;
        });
  }

  @Override
  public void clear() {
    fileSource.listFilesRecursively().forEach(file -> fileSource.deleteFile(file.getPath()));
  }

  public FileSource getFileSource() {
    return fileSource;
  }

  @Override
  public String getPath() {
    return fileSource.getPath();
  }

  private static String createFileName(String key) {
    return key + ".json";
  }
}
