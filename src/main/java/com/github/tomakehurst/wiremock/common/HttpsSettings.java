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

import com.google.common.io.Resources;

public class HttpsSettings {

    private final int port;
    private final String keyStorePath;

    public HttpsSettings(int port, String keyStorePath) {
        this.port = port;
        this.keyStorePath = keyStorePath;
    }

    public HttpsSettings(int port) {
        this(port, Resources.getResource("keystore").toString());
    }

    public static final HttpsSettings NO_HTTPS = new HttpsSettings(0, null);

    public int port() {
        return port;
    }

    public String keyStorePath() {
        return keyStorePath;
    }

    public boolean enabled() {
        return this != NO_HTTPS;
    }

    @Override
    public String toString() {
        return "HttpsSettings{" +
                "port=" + port +
                ", keystorePath='" + keyStorePath + '\'' +
                '}';
    }
}
