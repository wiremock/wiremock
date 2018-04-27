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
package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.UUID;

/**
 * A predicate to filter proxied ServeEvents against RequestPattern filters and IDs
 */
public class ProxiedServeEventFilters implements Predicate<ServeEvent> {

    @JsonUnwrapped
    private final RequestPattern filters;

    @JsonUnwrapped
    private final List<UUID> ids;

    @JsonUnwrapped
    private final boolean allowNonProxied;

    public ProxiedServeEventFilters() {
        this(null, null, false);
    }

    @JsonCreator
    public ProxiedServeEventFilters(
        @JsonProperty("filters") RequestPattern filters,
        @JsonProperty("ids") List<UUID> ids,
        @JsonProperty("allowNonProxied") boolean allowNonProxied
    ) {
        this.filters = filters;
        this.ids = ids;
        this.allowNonProxied = allowNonProxied;
    }

    @Override
    public boolean apply(ServeEvent serveEvent) {
        if (!serveEvent.getResponseDefinition().isProxyResponse() && !allowNonProxied) {
            return false;
        }

        if (filters != null && !filters.match(serveEvent.getRequest()).isExactMatch()) {
            return false;
        }

        if (ids != null && !ids.contains(serveEvent.getId())) {
            return false;
        }

        return true;
    }
}
