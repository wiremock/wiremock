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

import java.util.List;

/**
 * Defines the contract for a paginator.
 *
 * <p>A paginator is responsible for selecting a subset (a "page") of items from a larger collection
 * and providing information about the total size of that collection.
 *
 * @param <T> The type of the items being paginated.
 */
public interface Paginator<T> {

  /**
   * Selects and returns the list of items for the current page.
   *
   * @return A list containing the items for the current page.
   */
  List<T> select();

  /**
   * Gets the total number of items available across all pages.
   *
   * <p>This represents the size of the original collection before pagination is applied.
   *
   * @return The total number of items.
   */
  int getTotal();
}
