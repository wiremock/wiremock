/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

/**
 * The constants of {@link RequestMethod} that are instances of its subclasses. They are declared
 * here because this interface has no default methods, so initialising a subclass doesn't initialise
 * it. If they were on {@code RequestMethod}, one thread initialising {@code RequestMethod} while
 * another initialises a subclass could deadlock.
 */
sealed interface RequestMethodConstants permits RequestMethod {

  RequestMethod GET_OR_HEAD = RequestMethod.isOneOf(RequestMethod.GET, RequestMethod.HEAD);
}
