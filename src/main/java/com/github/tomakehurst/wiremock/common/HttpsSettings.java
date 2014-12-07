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
    private final String keystore;
    private final String keyPassword;
    private final String truststore;
    private final String trustPassword;

    private final boolean needClientAuth;

    public HttpsSettings(int port, String keystore, String keyPassword, String truststore, String trustPassword, boolean needClientAuth) {
        this.port = port;
        this.keystore = keystore;
        this.keyPassword = keyPassword;
        this.truststore = truststore;
        this.trustPassword = trustPassword;
        this.needClientAuth = needClientAuth;
    }

    public HttpsSettings(int port, String keystore, String keyPassword) {
        this(port, keystore, keyPassword, null, null, false);
    }

    public static final HttpsSettings NO_HTTPS = new HttpsSettings(0, null, null);

    public int port() {
        return port;
    }

    public String keystore() {
        return keystore;
    }

    public boolean enabled() {
        return this != NO_HTTPS;
    }

    public String keyPassword() {
        return keyPassword;
    }

    public String truststore() {
        return truststore;
    }

    public String trustPassword() {
        return trustPassword;
    }

    public boolean needClientAuth() {
        return needClientAuth;
    }

    @Override
    public String toString() {
        return "HttpsSettings{" +
                "port=" + port +
                ", keystore='" + keystore + '\'' +
                ", keyPassword='" + keyPassword + '\'' +
                ", truststore='" + truststore + '\'' +
                ", trustPassword='" + trustPassword + '\'' +
                ", needClientAuth=" + needClientAuth +
                '}';
    }
}
