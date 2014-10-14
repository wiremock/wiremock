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

import com.google.common.base.Splitter;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class Urls {

    public static Map<String, String> splitQuery(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }

        return Splitter.on('&').withKeyValueSeparator("=").split(query);
    }

    public static Map<String, String> splitQuery(URI uri) {
        if (uri == null) {
            return Collections.emptyMap();
        }

        return splitQuery(uri.getQuery());
    }
}
