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
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static com.google.common.base.MoreObjects.firstNonNull;

public class ServeEvent {

    private final UUID id;
    private final LoggedRequest request;
    private final ResponseDefinition responseDefinition;

    private final StubMapping stubMapping;

    @JsonCreator
    public ServeEvent(@JsonProperty("id") UUID id,
                      @JsonProperty("request") LoggedRequest request,
                      @JsonProperty("responseDefinition") ResponseDefinition responseDefinition,
                      @JsonProperty("mapping") StubMapping stubMapping) {
        this.id = id;
        this.request = request;
        this.responseDefinition = responseDefinition;
        this.stubMapping = stubMapping;
    }

    private ServeEvent(LoggedRequest request, ResponseDefinition responseDefinition, StubMapping stubMapping) {
        this(UUID.randomUUID(), request, responseDefinition, stubMapping);
    }

    public static ServeEvent forUnmatchedRequest(LoggedRequest request) {
        return new ServeEvent(request, ResponseDefinition.notConfigured(), null);
    }

    public static ServeEvent of(LoggedRequest request, ResponseDefinition responseDefinition) {
        return new ServeEvent(request, responseDefinition, null);
    }

    public static ServeEvent of(LoggedRequest request, ResponseDefinition responseDefinition, StubMapping stubMapping) {
        return new ServeEvent(request, responseDefinition, stubMapping);
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

    public StubMapping getStubMapping() {
        return stubMapping;
    }

    @JsonIgnore
    public Map<String, Parameters> getPostServeActions() {
        return stubMapping != null && stubMapping.getPostServeActions() != null ?
            getStubMapping().getPostServeActions() :
            Collections.<String, Parameters>emptyMap();
    }

    public static final Function<ServeEvent, LoggedRequest> TO_LOGGED_REQUEST = new Function<ServeEvent, LoggedRequest>() {
        @Override
        public LoggedRequest apply(ServeEvent serveEvent) {
            return serveEvent.getRequest();
        }
    };

    public static final Predicate<ServeEvent> NOT_MATCHED = new Predicate<ServeEvent>() {
        @Override
        public boolean apply(ServeEvent serveEvent) {
            return serveEvent.isNoExactMatch();
        }
    };
}
