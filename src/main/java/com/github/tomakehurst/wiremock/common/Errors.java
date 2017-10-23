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
package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Collections.singletonList;

public class Errors {

    private final List<Error> errors;

    public Errors(@JsonProperty("errors") List<Error> errors) {
        this.errors = errors;
    }

    public static Errors single(Integer code, String sourcePointer, String title) {
        return new Errors(singletonList(new Error(code, new Error.Source(sourcePointer), title)));
    }

    public static Errors single(Integer code, String title) {
        return new Errors(singletonList(new Error(code, title)));
    }

    public static Errors notRecording() {
        return single(30, "Not currently recording.");
    }

    public static Errors validation(String pointer, String message) {
        return single(10, pointer, message);
    }

    public Error first() {
        if (errors.isEmpty()) {
            throw new IllegalStateException("No errors are present");
        }

        return errors.get(0);
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static class Error {

        private final Integer code;
        private final Source source;
        private final String title;

        public Error(@JsonProperty("code") Integer code,
                     @JsonProperty("source") Source source,
                     @JsonProperty("title") String title) {
            this.code = code;
            this.source = source;
            this.title = title;
        }

        public Error(int code, String title) {
            this(code, null, title);
        }

        public Integer getCode() {
            return code;
        }

        public Source getSource() {
            return source;
        }

        public String getTitle() {
            return title;
        }

        public static class Source {

            private final String pointer;

            public Source(@JsonProperty("pointer") String pointer) {
                this.pointer = pointer;
            }

            public String getPointer() {
                return pointer;
            }
        }
    }


}
