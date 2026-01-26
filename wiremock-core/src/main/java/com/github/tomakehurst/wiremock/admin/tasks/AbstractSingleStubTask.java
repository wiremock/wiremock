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
package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.UUID;

public abstract class AbstractSingleStubTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    String idString = pathParams.get("id");
    UUID id;
    try {
      id = UUID.fromString(idString);
    } catch (IllegalArgumentException e) {
      return ResponseDefinition.badRequest(Errors.single(10, idString + " is not a valid UUID"));
    }

    final SingleStubMappingResult result = admin.getStubMapping(id);
    return result.isPresent()
        ? processStubMapping(admin, serveEvent, result.getItem())
        : ResponseDefinition.notFound();
  }

  protected abstract ResponseDefinition processStubMapping(
      Admin admin, ServeEvent serveEvent, StubMapping stubMapping);
}
