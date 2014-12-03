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

public class HttpsSettings {

    private final int port;
    private final String keyStorePath;
    private final String keyStorePassword;
    private final String trustStorePath;
    private final String trustStorePassword;

    private final boolean needClientAuth;

    public HttpsSettings(int port, String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword, boolean needClientAuth) {
        this.port = port;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.needClientAuth = needClientAuth;
    }

    public HttpsSettings(int port, String keyStorePath, String keyStorePassword) {
        this(port, keyStorePath, keyStorePassword, null, null, false);
    }

    public static final HttpsSettings NO_HTTPS = new HttpsSettings(0, null, null);

    public int port() {
        return port;
    }

    public String keyStorePath() {
        return keyStorePath;
    }

    public boolean enabled() {
        return this != NO_HTTPS;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public boolean needClientAuth() {
        return needClientAuth;
    }

    @Override
    public String toString() {
        return "HttpsSettings{" +
                "port=" + port +
                ", keyStorePath='" + keyStorePath + '\'' +
                ", keyStorePassword='" + keyStorePassword + '\'' +
                ", trustStorePath='" + trustStorePath + '\'' +
                ", trustStorePassword='" + trustStorePassword + '\'' +
                ", needClientAuth=" + needClientAuth +
                '}';
    }
}
