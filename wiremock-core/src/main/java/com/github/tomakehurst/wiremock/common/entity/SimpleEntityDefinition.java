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

import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.StreamSources;
import com.github.tomakehurst.wiremock.store.Stores;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class SimpleEntityDefinition extends EntityDefinition {

  private final byte[] data;

  SimpleEntityDefinition(CompressionType compression, Format format, Charset charset, byte[] data) {
    super(compression, format, charset);
    this.data = data;
  }

  public boolean isInline() {
    return true;
  }

  @Override
  @NonNull InputStreamSource resolveEntityData(@Nullable Stores stores) {
    return StreamSources.forBytes(getDataAsBytes());
  }

  @Override
  public @Nullable Object getData() {
    if (!isBinary() && !isCompressed()) {
      return stringFromBytes(data, charset);
    }

    return null;
  }

  public @Nullable String getBase64Data() {
    if (isBinary() || isCompressed()) {
      return Encoding.encodeBase64(data);
    }
    return null;
  }

  @Override
  public String getDataAsString() {
    if (!isBinary() && !isCompressed()) {
      return stringFromBytes(data, charset);
    } else {
      return getBase64Data();
    }
  }

  @Override
  public byte[] getDataAsBytes() {
    return data;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(
        this.compression, this.format, this.charset, this.data, null, null, null, false);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SimpleEntityDefinition that)) {
      return false;
    }
    return Objects.equals(compression, that.compression)
        && Objects.equals(format, that.format)
        && Objects.equals(charset, that.charset)
        && Objects.deepEquals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compression, format, charset, Arrays.hashCode(data));
  }
}
