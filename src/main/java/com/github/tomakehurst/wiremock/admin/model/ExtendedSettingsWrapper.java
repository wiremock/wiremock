/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.extension.Parameters;

/**
 * A wrapper class for serializing and deserializing extended settings.
 *
 * <p>This class serves as a data transfer object (DTO) for custom settings defined in WireMock
 * extensions, typically used in JSON-based admin API requests.
 *
 * @see com.github.tomakehurst.wiremock.extension.Parameters
 */
public class ExtendedSettingsWrapper {

  private final Parameters extended;

  /**
   * Constructs a new ExtendedSettingsWrapper.
   *
   * @param extended The {@link Parameters} object containing the custom settings. The
   *     {@code @JsonProperty} annotation maps this from the "extended" field in a JSON object.
   */
  public ExtendedSettingsWrapper(@JsonProperty("extended") Parameters extended) {
    this.extended = extended;
  }

  public Parameters getExtended() {
    return extended;
  }
}
