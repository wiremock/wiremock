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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.RequestMethod;

/**
 * Defines the contract for adding new admin routes.
 *
 * <p>A {@code Router} is used to build the mapping between an HTTP request specification (method
 * and URL template) and the {@link AdminTask} that should be executed.
 *
 * @see AdminTask
 * @see RequestSpec
 */
public interface Router {

  /**
   * Adds a new route mapping a request specification to an {@link AdminTask} class.
   *
   * <p>The router implementation is expected to instantiate the task from the provided class.
   *
   * @param method The HTTP request method to match.
   * @param urlTemplate The URL path template to match (e.g., "/mappings/{id}").
   * @param task The {@code Class} of the admin task to execute.
   */
  void add(RequestMethod method, String urlTemplate, Class<? extends AdminTask> task);

  /**
   * Adds a new route mapping a request specification to a pre-instantiated {@link AdminTask}.
   *
   * @param method The HTTP request method to match.
   * @param urlTemplate The URL path template to match.
   * @param adminTask The {@code AdminTask} instance to execute.
   */
  void add(RequestMethod method, String urlTemplate, AdminTask adminTask);
}
