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

import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.charset.Charset;
import org.jspecify.annotations.NonNull;

public class SimpleStringEntityDefinition extends EntityDefinition {

  @JsonValue
  public String getText() {
    return getDataAsString();
  }

  @JsonCreator
  public SimpleStringEntityDefinition(@NonNull String text) {
    this(text, DEFAULT_CHARSET);
  }

  SimpleStringEntityDefinition(@NonNull String text, @NonNull Charset charset) {
    super(NONE, Format.TEXT, charset, null, bytesFromString(text, charset), null);
  }

  SimpleStringEntityDefinition(byte[] data, Charset charset) {
    super(NONE, Format.TEXT, charset, null, data, null);
  }
}
