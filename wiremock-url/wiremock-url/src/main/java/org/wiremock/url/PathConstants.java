/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package org.wiremock.url;

/**
 * The constants of {@link Path}. They are declared here because this interface has no default
 * methods, so initialising a {@link PathValue} doesn't initialise it. If they were on {@code Path},
 * one thread initialising {@code Path} while another initialises {@code PathValue} could deadlock.
 */
sealed interface PathConstants permits Path {

  /** An empty path with no segments. */
  Path EMPTY = new PathValue("", true);

  /** A root path consisting of a single forward slash. */
  Path ROOT = new PathValue("/", true);
}
