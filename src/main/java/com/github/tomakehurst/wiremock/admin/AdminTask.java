/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/**
 * Defines the contract for all executable administrative tasks in WireMock.
 *
 * <p>Each implementation of this interface represents a specific operation that can be triggered
 * via the admin API, such as creating a stub, resetting the server, or querying requests. This is a
 * functional interface whose single method is {@link #execute}.
 */
public interface AdminTask {

  /**
   * Executes the administrative task.
   *
   * @param admin The core {@code Admin} instance, providing access to server functions.
   * @param serveEvent The HTTP request event that triggered this task.
   * @param pathParams Parameters extracted from the request's URL path (e.g., stub IDs).
   * @return A {@link ResponseDefinition} to be sent back to the client.
   */
  ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams);
}
