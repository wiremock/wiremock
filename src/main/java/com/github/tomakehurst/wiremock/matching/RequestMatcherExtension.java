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
package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;

public abstract class RequestMatcherExtension implements Extension, RequestMatcher {

    @Override
    public boolean isMatchedBy(Request request) {
        return isMatchedBy(request, Parameters.empty());
    }

    public abstract boolean isMatchedBy(Request request, Parameters parameters);

    @Override
    public String name() {
        return "inline";
    }

    public static final RequestMatcherExtension ALWAYS = new RequestMatcherExtension() {
        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            return true;
        }
    };

    public static final RequestMatcherExtension NEVER = new RequestMatcherExtension() {
        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            return false;
        }
    };
}
