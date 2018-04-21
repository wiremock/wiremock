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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.google.common.base.Preconditions;

public class ProxySettings {

    public static final ProxySettings NO_PROXY = new ProxySettings(null, 0);

    private final String host;
    private final int port;

    private String username;
    private String password;

    public ProxySettings(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static ProxySettings fromString(String config) {
        int portPosition = config.lastIndexOf(":");
        String host;
        int port;
        if(portPosition != -1){
            host = config.substring(0, portPosition);
            port = Integer.valueOf(config.substring(portPosition+1));
        }else{
            host = config;
            port = 80;
        }
        Preconditions.checkArgument(!host.isEmpty(), "Host part of proxy must be specified");
        return new ProxySettings(host, port);
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        if (this == NO_PROXY) {
            return "(no proxy)";
        }

        return String.format("%s:%s%s", host(), port(), (!isEmpty(this.username) ? " (with credentials)" : ""));
    }
}
