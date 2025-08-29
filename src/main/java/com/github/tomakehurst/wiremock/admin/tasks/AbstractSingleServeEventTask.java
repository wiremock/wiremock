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
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.UUID;

/**
 * An abstract base class for admin tasks that operate on a single {@link ServeEvent} identified by
 * its ID.
 *
 * <p>This class handles the common logic of extracting and validating the UUID from the URL path
 * before delegating to the concrete implementation.
 */
public abstract class AbstractSingleServeEventTask implements AdminTask {

  @Override
  public ResponseDefinition execute(
      Admin admin, ServeEvent adminServeEvent, PathParams pathParams) {
    String idString = pathParams.get("id");
    UUID id;
    try {
      id = UUID.fromString(idString);
    } catch (IllegalArgumentException e) {
      return ResponseDefinition.badRequest(Errors.single(10, idString + " is not a valid UUID"));
    }

    return processServeEvent(admin, adminServeEvent, id);
  }

  /**
   * Processes the specified serve event.
   *
   * @param admin The core WireMock admin instance.
   * @param adminServeEvent The request event that triggered this task.
   * @param id The UUID of the serve event to be processed.
   * @return The response definition to be sent to the client.
   */
  protected abstract ResponseDefinition processServeEvent(
      Admin admin, ServeEvent adminServeEvent, UUID id);
}
