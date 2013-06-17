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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.RequestMethod;

import static com.google.common.base.Preconditions.checkNotNull;

public class RequestSpec {

    private final RequestMethod method;
    private final String path;

    public RequestSpec(RequestMethod method, String path) {
        checkNotNull(method);
        checkNotNull(path);
        this.method = method;
        this.path = path;
    }

    public static RequestSpec requestSpec(RequestMethod method, String path) {
        return new RequestSpec(method, path);
    }

    public RequestMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestSpec that = (RequestSpec) o;

        if (method != that.method) return false;
        if (!path.equals(that.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}
