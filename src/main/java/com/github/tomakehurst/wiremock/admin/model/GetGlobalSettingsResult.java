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
import com.github.tomakehurst.wiremock.global.GlobalSettings;

/**
 * A wrapper for the result of a "get global settings" admin API call.
 *
 * <p>This serves as a data transfer object (DTO) to structure the JSON response containing the
 * server's {@link GlobalSettings}.
 *
 * @see com.github.tomakehurst.wiremock.global.GlobalSettings
 */
public class GetGlobalSettingsResult {

  private final GlobalSettings settings;

  /**
   * Constructs a new GetGlobalSettingsResult.
   *
   * @param settings The {@link GlobalSettings} object to be wrapped. The {@code @JsonProperty}
   *     annotation nests this under the "settings" key in the JSON output.
   */
  public GetGlobalSettingsResult(@JsonProperty("settings") GlobalSettings settings) {
    this.settings = settings;
  }

  public GlobalSettings getSettings() {
    return settings;
  }
}
