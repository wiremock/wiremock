/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class EditStubMappingTask extends AbstractSingleStubTask {

  @Override
  protected ResponseDefinition processStubMapping(
      Admin admin, ServeEvent serveEvent, StubMapping stubMapping) {
    StubMapping newStubMapping = StubMapping.buildFrom(serveEvent.getRequest().getBodyAsString());
    newStubMapping.setId(stubMapping.getId());
    admin.editStubMapping(newStubMapping);
    return ResponseDefinition.okForJson(newStubMapping);
  }
}
