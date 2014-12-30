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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;

import java.util.List;
import java.util.Map;

public interface Options {

    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_CONTAINER_THREADS = 200;
    public static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    int portNumber();
    HttpsSettings httpsSettings();
    int containerThreads();
    boolean browserProxyingEnabled();
    ProxySettings proxyVia();
    FileSource filesRoot();
    Notifier notifier();
    boolean requestJournalDisabled();
    public String bindAddress();
    List<CaseInsensitiveKey> matchingHeaders();
    public String proxyUrl();
    public boolean shouldPreserveHostHeader();
    String proxyHostHeader();
    <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType);
}
