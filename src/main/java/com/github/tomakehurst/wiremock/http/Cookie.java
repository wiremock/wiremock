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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;

public class Cookie {

    private String name;

    private String value;

    @JsonCreator
    public static Cookie cookie(@JsonProperty("name") String name, @JsonProperty("value") String value) {
        return new Cookie(name, value);
    }

    @JsonAnySetter
    public void set(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static Cookie absent() {
        return new Cookie(null, null);
    }

    public Cookie(String name, String value) {
        this.name = name;
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

    @JsonIgnore
    public String getValue() {
        return value;
    }

    @JsonValue
    public Serializable getSerializable() {
        return ImmutableMap.of(name, value);
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return isAbsent() ? "(absent)" : getSerializable().toString();
    }
}
