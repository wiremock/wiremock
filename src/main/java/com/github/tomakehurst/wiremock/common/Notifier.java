/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

/** The interface Notifier. */
public interface Notifier {

  /** The constant KEY. */
  public static final String KEY = "Notifier";

  /**
   * Info.
   *
   * @param message the message
   */
  void info(String message);

  /**
   * Error.
   *
   * @param message the message
   */
  void error(String message);

  /**
   * Error.
   *
   * @param message the message
   * @param t the t
   */
  void error(String message, Throwable t);
}
