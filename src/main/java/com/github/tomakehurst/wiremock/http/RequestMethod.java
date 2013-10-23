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
import com.fasterxml.jackson.annotation.JsonValue;

import static java.util.Arrays.asList;

public enum RequestMethod {
	GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE, ANY;

    @JsonCreator
    public static RequestMethod fromString(String value) {
        return RequestMethod.valueOf(value);
    }

    @JsonValue
    public String value() {
        return super.toString();
    }

    public boolean isOneOf(RequestMethod... methods) {
        return asList(methods).contains(this);
    }
}
