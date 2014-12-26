/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.google.common.collect.ImmutableList;

import java.util.Collection;

import static java.util.Arrays.asList;

public class HttpHeader extends MultiValue {

    public HttpHeader(String key, String... values) {
        super(key, asList(values));
    }

    public HttpHeader(CaseInsensitiveKey key, Collection<String> values) {
        super(key.value(), ImmutableList.copyOf(values));
    }

    public HttpHeader(String key, Collection<String> values) {
        super(key, ImmutableList.copyOf(values));
    }

    public static HttpHeader httpHeader(CaseInsensitiveKey key, String... values) {
        return new HttpHeader(key.value(), values);
    }

    public static HttpHeader httpHeader(String key, String... values) {
        return new HttpHeader(key, values);
    }

    public static HttpHeader absent(String key) {
        return new HttpHeader(key);
    }

    public CaseInsensitiveKey caseInsensitiveKey() {
        return CaseInsensitiveKey.from(key);
    }

}
