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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.stubbing.*;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.servlet.BasicResponseRenderer;
import com.github.tomakehurst.wiremock.servlet.MockServiceResponseRenderer;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.verification.InMemoryRequestJournal;
import com.github.tomakehurst.wiremock.verification.RequestJournal;

public class WireMockApp {
    
    public static final String FILES_ROOT = "__files";
    
    private final StubMappings stubMappings;
    private final InMemoryRequestJournal requestJournal;
    private final RequestHandler mockServiceRequestHandler;
    private final RequestHandler adminRequestHandler;
    private final GlobalSettingsHolder globalSettingsHolder;
    private final RequestDelayControl requestDelayControl;

    public static final String ADMIN_CONTEXT_ROOT = "/__admin";

    public WireMockApp(FileSource fileSource, Notifier notifier, boolean enableBrowserProxying, RequestDelayControl socketAcceptor) {
        this.requestDelayControl = socketAcceptor;
        globalSettingsHolder = new GlobalSettingsHolder();
        stubMappings = new InMemoryStubMappings();
        requestJournal = new InMemoryRequestJournal();
        mockServiceRequestHandler = new StubRequestHandler(stubMappings,
                new MockServiceResponseRenderer(fileSource.child(FILES_ROOT), globalSettingsHolder), enableBrowserProxying);
        mockServiceRequestHandler.addRequestListener(requestJournal);
        adminRequestHandler = new AdminRequestHandler(stubMappings, requestJournal, globalSettingsHolder,
                new BasicResponseRenderer(), requestDelayControl);
    }

    public RequestHandler getMockServiceRequestHandler() {
        return mockServiceRequestHandler;
    }

    public RequestHandler getAdminRequestHandler() {
        return adminRequestHandler;
    }

    public GlobalSettingsHolder getGlobalSettingsHolder() {
        return globalSettingsHolder;
    }
    
    public RequestJournal getRequestJournal() {
        return requestJournal;
    }
    
    public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
        mappingsLoader.loadMappingsInto(stubMappings);
    }
    
    public void addMockServiceRequestListener(RequestListener listener) {
        mockServiceRequestHandler.addRequestListener(listener);
    }
    
}
