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

import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.google.common.base.MoreObjects.firstNonNull;

public class BasicResponseRenderer implements ResponseRenderer {

    @Override
    public Response render(ResponseDefinition responseDefinition) {

        // Allows JavaScript Client access to Wiremock admin endpoints
        // See: http://en.wikipedia.org/wiki/Cross-origin_resource_sharing
        HttpHeader corsHeader = new HttpHeader("Access-Control-Allow-Origin", "*");
        HttpHeaders httpHeaders =
            firstNonNull(responseDefinition.getHeaders(), new HttpHeaders())
            .plus(corsHeader);

        return response()
                .status(responseDefinition.getStatus())
                .headers(httpHeaders)
                .body(responseDefinition.getBody())
                .build();
    }
}
