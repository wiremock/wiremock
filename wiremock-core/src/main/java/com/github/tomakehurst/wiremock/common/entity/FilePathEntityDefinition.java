/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.entity;

import java.nio.charset.Charset;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

class FilePathEntityDefinition extends EntityDefinition {

  private final @NonNull String filePath;

  FilePathEntityDefinition(
      CompressionType compression, Format format, Charset charset, @NonNull String filePath) {
    super(compression, format, charset);
    this.filePath = filePath;
  }

  @Override
  public @NonNull String getFilePath() {
    return filePath;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FilePathEntityDefinition that)) {
      return false;
    }
    return Objects.equals(compression, that.compression)
        && Objects.equals(format, that.format)
        && Objects.equals(charset, that.charset)
        && Objects.equals(filePath, that.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compression, format, charset, filePath);
  }
}
