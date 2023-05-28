/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

public class FormParameter extends MultiValue {

  @JsonCreator
  public FormParameter(
      @JsonProperty("key") String key, @JsonProperty("values") List<String> values) {
    super(key, values);
  }

  public static FormParameter formParam(String key, String[] values) {
    return new FormParameter(key, asList(values));
  }

  public static FormParameter absent(String key) {
    return new FormParameter(key, Collections.emptyList());
  }

  @JsonIgnore
  @Override
  public boolean isPresent() {
    return super.isPresent();
  }

  @JsonProperty
  @Override
  public String key() {
    return super.key();
  }

  @JsonProperty
  @Override
  public List<String> values() {
    return super.values();
  }

  @JsonIgnore
  @Override
  public boolean isSingleValued() {
    return super.isSingleValued();
  }
}
