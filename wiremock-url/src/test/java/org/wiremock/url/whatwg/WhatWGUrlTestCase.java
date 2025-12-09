/*
 * Copyright (C) 2025 Thomas Akehurst
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

// 869 tests
// 273 failure
// 596 success
public record WhatWGUrlTestCase(

    // only present once, not empty
    @Nullable String comment,

    // always present, never null, can be empty signifying empty input
    String input,

    // always present, can be null, never empty
    // 328 null base & success
    // 268 present base & success
    // 213 null base & failure
    //  60 present base & failure
    @Nullable String base,

    // can be absent (false)
    boolean failure,

    // can be absent (null), never empty. Always absent if origin is absent
    // only present 17 times
    // when present, always of form { "input": "...", "base": null, "failure": true, "relativeTo":
    // "..." }
    // 16 times `"relativeTo": "non-opaque-path-base"`
    // 1 times `"relativeTo": "any-base"`
    @Nullable String relativeTo,

    // can be absent (null), never empty, 536 occurrences
    // always absent (null) on failure
    @Nullable String href,

    // can be absent (null), never empty. What I call base url
    // always absent (null) on failure
    @Nullable String origin,

    // always present, never empty, never just ":" on success
    // always absent (null) on failure
    @Nullable String protocol,

    // always present, can be empty on success
    // always absent (null) on failure
    @Nullable String username,

    // always present, can be empty on success
    // always absent (null) on failure
    @Nullable String password,

    // always present, can be empty (226) on success
    // always absent (null) on failure
    // what I call hostAndPort
    @Nullable String host,

    // always present, can be empty (226) on success
    // always absent (null) on failure
    @Nullable String hostname,

    // sometimes empty on success
    // sometimes absent on success TEST ME
    // always absent (null) on failure
    @Nullable String port,

    // always present, can be empty (21) on success
    // always absent (null) on failure
    @Nullable String pathname,

    // always present, often empty (531) on success
    // always absent (null) on failure
    String search,

    // sometimes present (9), can be empty (7) on success
    // always absent (null) on failure
    @Nullable String searchParams,

    // always present, often empty (537) on success
    // always absent (null) on failure
    String hash) {
  boolean success() {
    return !failure;
  }
}
