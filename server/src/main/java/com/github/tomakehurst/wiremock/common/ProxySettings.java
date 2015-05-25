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

import com.google.common.base.Preconditions;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getFirst;

public class ProxySettings {

    public static final ProxySettings NO_PROXY = new ProxySettings(null, 0);

    private final String host;
    private final int port;

    public ProxySettings(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static ProxySettings fromString(String config) {
        Iterable<String> parts = on(":").split(config);
        String host = getFirst(parts, "");
        Preconditions.checkArgument(!host.isEmpty(), "Host part of proxy must be specified");

        int port = Integer.valueOf(get(parts, 1, "80"));
        return new ProxySettings(host, port);
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public String toString() {
        if (this == NO_PROXY) {
            return "(no proxy)";
        }

        return host() + ":" + port();
    }
}
