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

import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.nio.charset.Charset;

@JsonSerialize(as = EmptyEntityDefinition.class)
@JsonDeserialize(as = EmptyEntityDefinition.class)
public class EmptyEntityDefinition extends EntityDefinition {

  public static final EmptyEntityDefinition INSTANCE = new EmptyEntityDefinition();

  protected EmptyEntityDefinition() {
    super(NONE, null, null, null, null, null);
  }

  @Override
  public Format getFormat() {
    return null;
  }

  @Override
  public Charset getCharset() {
    return null;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getDataAsString() {
    return null;
  }

  @Override
  public byte[] getDataAsBytes() {
    return null;
  }

  @Override
  public String getDataStore() {
    return null;
  }

  @Override
  public String getDataRef() {
    return null;
  }
}
