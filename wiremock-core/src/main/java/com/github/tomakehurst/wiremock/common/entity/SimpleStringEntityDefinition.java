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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.charset.Charset;

public class SimpleStringEntityDefinition extends TextEntityDefinition {

  @JsonValue
  public String getText() {
    return getDataAsString();
  }

  @JsonCreator
  public SimpleStringEntityDefinition(String text) {
    this(text, DEFAULT_CHARSET);
  }

  SimpleStringEntityDefinition(String text, Charset charset) {
    super(TextFormat.TEXT, charset, CompressionType.NONE, null, null, DataFormat.plain, text, null);
  }

  @Override
  public DataFormat getDataFormat() {
    return DataFormat.plain;
  }

  @Override
  public SimpleStringEntityDefinition withCharset(Charset charset) {
    return new SimpleStringEntityDefinition(getText(), charset);
  }
}
