/*
 * Copyright (C) 2013-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.GetGlobalSettingsResult;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class GlobalSettingsUpdateTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    GlobalSettings newSettings;
    try {
      newSettings =
          admin
              .getJson()
              .readValue(serveEvent.getRequest().getBodyAsString(), GlobalSettings.class);
    } catch (Exception e) {
      newSettings =
          admin
              .getJson()
              .readValue(serveEvent.getRequest().getBodyAsString(), GetGlobalSettingsResult.class)
              .getSettings();
    }

    admin.updateGlobalSettings(newSettings);
    return ResponseDefinition.ok();
  }
}
