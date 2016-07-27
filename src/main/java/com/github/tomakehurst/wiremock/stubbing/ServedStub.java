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

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.List;

public class ServedStub {

    private final LoggedRequest request;
    private final ResponseDefinition responseDefinition;

    public ServedStub(LoggedRequest request, ResponseDefinition responseDefinition) {
        this.request = request;
        this.responseDefinition = responseDefinition;
    }

    public static ServedStub noExactMatch(LoggedRequest request) {
        return new ServedStub(request, ResponseDefinition.notConfigured());
    }

    public static ServedStub exactMatch(LoggedRequest request, ResponseDefinition responseDefinition) {
        return new ServedStub(request, responseDefinition);
    }

    public boolean isNoExactMatch() {
        return !responseDefinition.wasConfigured();
    }

    public LoggedRequest getRequest() {
        return request;
    }

    public ResponseDefinition getResponseDefinition() {
        return responseDefinition;
    }

    public List<NearMiss> getNearMisses() {
        return null;
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
