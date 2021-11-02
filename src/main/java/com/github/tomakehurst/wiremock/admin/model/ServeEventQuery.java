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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ServeEventQuery {

    public static final ServeEventQuery ALL = new ServeEventQuery(false);
    public static final ServeEventQuery ALL_UNMATCHED = new ServeEventQuery(true);

    public static ServeEventQuery fromRequest(Request request) {
        final QueryParameter unmatchedParameter = request.queryParameter("unmatched");
        boolean unmatched = unmatchedParameter.isPresent() && unmatchedParameter.containsValue("true");
        return new ServeEventQuery(unmatched);
    }

    private final boolean onlyUnmatched;

    public ServeEventQuery(@JsonProperty("onlyUnmatched") boolean onlyUnmatched) {
        this.onlyUnmatched = onlyUnmatched;
    }

    public boolean isOnlyUnmatched() {
        return onlyUnmatched;
    }

    public List<ServeEvent> filter(List<ServeEvent> events) {
        return onlyUnmatched ?
                events.stream().filter(serveEvent -> !serveEvent.getWasMatched()).collect(toList()) :
                events;
    }
}
