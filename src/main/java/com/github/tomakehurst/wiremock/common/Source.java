/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

/**
 * The interface Source.
 *
 * @param <T> the type parameter
 */
public interface Source<T> {
  /**
   * Load t.
   *
   * @return the t
   */
  T load();

  /**
   * Save.
   *
   * @param item the item
   */
  void save(T item);

  /**
   * Exists boolean.
   *
   * @return the boolean
   */
  boolean exists();
}
