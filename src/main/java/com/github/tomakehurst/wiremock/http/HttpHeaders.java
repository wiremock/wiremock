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
import java.util.Set;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.google.common.collect.Maps.newHashMap;

public class HttpHeaders extends HashMap<String, String> {

    private final Map<String, HttpHeader> headers;

    public HttpHeaders() {
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

    public boolean hasContentTypeHeader() {
        return headers.containsKey(ContentTypeHeader.KEY);
    }

    @Override
    public String get(Object key) {
        return super.get(key);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String put(String key, String value) {
        headers.put(key, httpHeader(key, value));
        return super.put(key, value);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Map.Entry<? extends String, ? extends String> entry: m.entrySet()) {
            headers.put(entry.getKey(), httpHeader(entry.getKey(), entry.getValue()));
        }

        super.putAll(m);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return super.entrySet();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> keySet() {
        return super.keySet();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
