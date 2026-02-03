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
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.store.Stores;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@JsonSerialize(as = EmptyEntityDefinition.class)
@JsonDeserialize(as = EmptyEntityDefinition.class)
public class EmptyEntityDefinition extends EntityDefinition {

  public static final EmptyEntityDefinition INSTANCE = new EmptyEntityDefinition();

  protected EmptyEntityDefinition() {
    super(NONE, null, null);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof EmptyEntityDefinition;
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public EntityDefinition.Builder toBuilder() {
    return new EntityDefinition.Builder();
  }

  @Override
  @NonNull Entity resolve(Stores stores) {
    return Entity.EMPTY;
  }

  @Override
  @Nullable InputStreamSource resolveEntityData(@Nullable Stores stores) {
    return null;
  }
}
