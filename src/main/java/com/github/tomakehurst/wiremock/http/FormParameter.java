/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/** The type Form parameter. */
public class FormParameter extends MultiValue {

  /**
   * Instantiates a new Form parameter.
   *
   * @param key the key
   * @param values the values
   */
  public FormParameter(
      @JsonProperty("key") String key, @JsonProperty("values") List<String> values) {
    super(key, values);
  }

  /**
   * Absent form parameter.
   *
   * @param key the key
   * @return the form parameter
   */
  public static FormParameter absent(String key) {
    return new FormParameter(key, Collections.emptyList());
  }
}
