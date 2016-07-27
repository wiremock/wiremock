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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class Cookie {

    private String value;

    @JsonCreator
    public static Cookie cookie(String value) {
        return new Cookie(value);
    }

    public static Cookie absent() {
        return new Cookie(null);
    }

    public Cookie(String value) {
        this.value = value;
    }

    @JsonIgnore
    public boolean isPresent() {
        return value != null;
    }

    @JsonIgnore
    public boolean isAbsent() {
        return value == null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return isAbsent() ? "(absent)" : value;
    }
}
