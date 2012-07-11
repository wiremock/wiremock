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

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class HttpHeaders extends HashMap<String, String> {

    private final Map<String, HttpHeader> headers;

    public HttpHeaders() {
        headers = newHashMap();
    }

    public HttpHeaders(Map<? extends String, ? extends String> m) {
        super(m);
        headers = newHashMap();
    }

    public HttpHeaders(HttpHeader... headers) {
        this();
        for (HttpHeader header: headers) {
            this.headers.put(header.key(), header);
        }
    }

    public HttpHeader getHeader(String key) {
        if (!headers.containsKey(key)) {
            return HttpHeader.absent(key);
        }

        return headers.get(key);
    }

    

}
