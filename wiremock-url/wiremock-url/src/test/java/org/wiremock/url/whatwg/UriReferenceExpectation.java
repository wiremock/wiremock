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
package org.wiremock.url.whatwg;

import org.jspecify.annotations.Nullable;

public record UriReferenceExpectation(
    String stringValue,
    String type,
    @Nullable String scheme,
    @Nullable String authority,
    @Nullable String userInfo,
    @Nullable String username,
    @Nullable String password,
    @Nullable String host,
    @Nullable String port,
    String path,
    @Nullable String query,
    @Nullable String fragment) {}
