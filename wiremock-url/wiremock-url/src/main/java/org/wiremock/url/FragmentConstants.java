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
 * The constants of {@link Fragment}. They are declared here because this interface has no default
 * methods, so initialising a {@link FragmentValue} doesn't initialise it. If they were on {@code
 * Fragment}, one thread initialising {@code Fragment} while another initialises {@code
 * FragmentValue} could deadlock.
 */
sealed interface FragmentConstants permits Fragment {

  Fragment EMPTY = new FragmentValue("", true);
}
