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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.text.StringEscapeUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// 596 success
@JsonIgnoreProperties("comment")
public record SuccessWhatWGUrlTestCase(

    // always present, never null, can be empty signifying empty input
    String input,

    // always present, can be null, never empty
    // 328 null base & success
    // 268 present base & success
    // 213 null base & failure
    //  60 present base & failure
    @Nullable String base,

    // can be absent (null), never empty, 536 occurrences
    @Nullable String href,

    // can be absent (null), never empty. What I call base url
    @Nullable String origin,

    // always present, never empty, never just ":" on success
    String protocol,

    // always present, can be empty on success
    String username,

    // always present, can be empty on success
    String password,

    // always present, can be empty (226) on success
    // what I call hostAndPort
    String host,

    // always present, can be empty (226) on success
    // what I call host
    String hostname,

    // sometimes empty on success
    // sometimes absent on success TEST ME
    @Nullable String port,

    // always present, can be empty (21) on success
    String pathname,

    // always present, often empty (531) on success
    String search,

    // sometimes present (9), can be empty (7) on success
    @Nullable String searchParams,

    // always present, often empty (537) on success
    String hash)
    implements WhatWGUrlTestCase {

  public boolean success() {
    return true;
  }

  @Override
  @NonNull
  public String toString() {
    return "new SuccessWhatWGUrlTestCase("
        + appendParameter("input", input)
        + ','
        + appendParameter("base", base)
        + ','
        + appendParameter("href", href)
        + ','
        + appendParameter("origin", origin)
        + ','
        + appendParameter("protocol", protocol)
        + ','
        + appendParameter("username", username)
        + ','
        + appendParameter("password", password)
        + ','
        + appendParameter("host", host)
        + ','
        + appendParameter("hostname", hostname)
        + ','
        + appendParameter("port", port)
        + ','
        + appendParameter("pathname", pathname)
        + ','
        + appendParameter("search", search)
        + ','
        + appendParameter("searchParams", searchParams)
        + ','
        + appendParameter("hash", hash)
        + "\n)";
  }

  private static @NonNull String appendParameter(String name, String value) {
    return "\n  /* " + name + " */ " + stringOrNull(value);
  }

  private static @NonNull String stringOrNull(@Nullable String value) {
    return value == null ? "null" : '"' + StringEscapeUtils.escapeJava(value) + '"';
  }

  public static void withContext(SuccessWhatWGUrlTestCase testCase, Runnable o) {
    try {
      o.run();
    } catch (Throwable e) {
      System.out.println(testCase);
      throw e;
    }
  }
}
