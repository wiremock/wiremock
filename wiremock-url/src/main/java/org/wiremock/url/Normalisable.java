/*
 * Copyright (C) 2026 Thomas Akehurst
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
 * Many elements of URI References have a normal form - e.g. the schemes {@code HTTP} and {@code
 * HtTp} both normalise to {@code http}.
 *
 * <p>{@code Type.normalise().equals(Type.normalise().normalise()} should always be true.
 *
 * @param <SELF> the self type
 */
public interface Normalisable<SELF extends Normalisable<SELF>> {

  /**
   * Returns a normalised form of this value.
   *
   * @return a normalised value
   */
  SELF normalise();

  boolean isNormalForm();
}
