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
package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.UUID;

public class ServedStub {

    private final UUID id;
    private final LoggedRequest request;
    private final ResponseDefinition responseDefinition;

    @JsonCreator
    public ServedStub(@JsonProperty("id") UUID id,
                      @JsonProperty("request") LoggedRequest request,
                      @JsonProperty("responseDefinition") ResponseDefinition responseDefinition) {
        this.id = id;
        this.request = request;
        this.responseDefinition = responseDefinition;
    }

    public ServedStub(LoggedRequest request, ResponseDefinition responseDefinition) {
        this(UUID.randomUUID(), request, responseDefinition);
    }

    public static ServedStub noExactMatch(LoggedRequest request) {
        return new ServedStub(request, ResponseDefinition.notConfigured());
    }

    public static ServedStub exactMatch(LoggedRequest request, ResponseDefinition responseDefinition) {
        return new ServedStub(request, responseDefinition);
    }

    @JsonIgnore
    public boolean isNoExactMatch() {
        return !responseDefinition.wasConfigured();
    }

    public UUID getId() {
        return id;
    }

    public LoggedRequest getRequest() {
        return request;
    }

    public ResponseDefinition getResponseDefinition() {
        return responseDefinition;
    }

    public static final Function<ServedStub, LoggedRequest> TO_LOGGED_REQUEST = new Function<ServedStub, LoggedRequest>() {
        @Override
        public LoggedRequest apply(ServedStub servedStub) {
            return servedStub.getRequest();
        }
    };

    public static final Predicate<ServedStub> NOT_MATCHED = new Predicate<ServedStub>() {
        @Override
        public boolean apply(ServedStub servedStub) {
            return servedStub.isNoExactMatch();
        }
    };
}
