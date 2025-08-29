/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.admin.model.SingleServedStubResult;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.UUID;

/**
 * An admin task to retrieve a single logged request (a "served stub") by its ID.
 *
 * <p>This task extends {@link AbstractSingleServeEventTask} to handle the API request for fetching
 * a specific {@link ServeEvent} from the journal.
 *
 * @see ServeEvent
 * @see AbstractSingleServeEventTask
 */
public class GetServedStubTask extends AbstractSingleServeEventTask {

  @Override
  protected ResponseDefinition processServeEvent(Admin admin, ServeEvent adminServeEvent, UUID id) {
    final SingleServedStubResult result = admin.getServedStub(id);
    return result.isPresent()
        ? ResponseDefinition.okForJson(result.getItem())
        : ResponseDefinition.notFound();
  }
}
