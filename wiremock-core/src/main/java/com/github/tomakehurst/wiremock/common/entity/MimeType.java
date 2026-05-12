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

import org.jspecify.annotations.NonNull;

public class MimeType {

  @NonNull private final String group;
  @NonNull private final String type;
  private final String subType;

  public MimeType(String group, String type, String subType) {
    this.group = group;
    this.type = type;
    this.subType = subType;
  }

  public @NonNull String getGroup() {
    return group;
  }

  public @NonNull String getType() {
    return type;
  }

  public String getSubType() {
    return subType;
  }

  public static MimeType parse(String mimeType) {
    String[] parts = mimeType.split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid MIME type: " + mimeType);
    }

    String group = parts[0];

    String[] subParts = parts[1].split("\\+");
    String type = subParts[0];
    String subType = null;
    if (subParts.length == 2) {
      subType = subParts[1];
    }

    return new MimeType(group, type, subType);
  }

  @Override
  public String toString() {
    return group + "/" + type + (subType != null ? "+" + subType : "");
  }
}
