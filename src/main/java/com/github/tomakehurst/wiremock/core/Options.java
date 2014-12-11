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

import java.util.List;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;

/**
 * Options for running wiremock. More information on most options can be found in the manual (Stand-Alone operation)
 */
public interface Options {

    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    /**
     * @return Port number on which wiremock is listening
     */
    int portNumber();

    /**
     * @return Settings for HTTPS connections to wiremock
     */
    HttpsSettings httpsSettings();

    /**
     * @return File source that supplies initial configuration files for wiremock.
     */
    FileSource filesRoot();

    /**
     * @return Logger for warnings and errors
     */
    Notifier notifier();

    /**
     * @return true if request journal is disabled (see manual)
     */
    boolean requestJournalDisabled();

    /**
     * @return Maximum size of request journal (see manual)
     */
    Integer maxEntriesRequestJournal();

    /**
     * @return Address to bind to. Determines from which hosts wiremock accepts connection requests.
     */
    public String bindAddress();

    /**
     * @return Enable browser proxying (see manual)
     */
    boolean browserProxyingEnabled();

    /**
     * @return Settings for browser proxying (see manual)
     */
    ProxySettings proxyVia();

    /**
     * @return Headers to capture in record mode
     */
    List<CaseInsensitiveKey> matchingHeaders();

    /**
     * @return Proxy all requests to another base URL. Usually used in record mode.
     */
    public String proxyUrl();

    /**
     * @return Preserve host header in record mode
     */
    public boolean shouldPreserveHostHeader();

    /**
     * @return Host header to use when forwarding requests in record mode.
     */
    String proxyHostHeader();
}
