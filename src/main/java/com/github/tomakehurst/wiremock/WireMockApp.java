package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.mapping.AdminRequestHandler;
import com.github.tomakehurst.wiremock.mapping.InMemoryMappings;
import com.github.tomakehurst.wiremock.mapping.Mappings;
import com.github.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.github.tomakehurst.wiremock.mapping.RequestHandler;
import com.github.tomakehurst.wiremock.mapping.RequestListener;
import com.github.tomakehurst.wiremock.servlet.BasicResponseRenderer;
import com.github.tomakehurst.wiremock.servlet.MockServiceResponseRenderer;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.verification.InMemoryRequestJournal;

public class WireMockApp {
    
    public static final String FILES_ROOT = "__files";
    
    private final Mappings mappings;
    private final InMemoryRequestJournal requestJournal;
    private final RequestHandler mockServiceRequestHandler;
    private final RequestHandler adminRequestHandler;
    private final GlobalSettingsHolder globalSettingsHolder;

    public static final String ADMIN_CONTEXT_ROOT = "/__admin";

    public WireMockApp(FileSource fileSource, Notifier notifier) {
        globalSettingsHolder = new GlobalSettingsHolder();
        mappings = new InMemoryMappings();
        requestJournal = new InMemoryRequestJournal();
        mockServiceRequestHandler = new MockServiceRequestHandler(mappings,
                new MockServiceResponseRenderer(fileSource.child(FILES_ROOT), globalSettingsHolder));
        mockServiceRequestHandler.addRequestListener(requestJournal);
        adminRequestHandler = new AdminRequestHandler(mappings, requestJournal, globalSettingsHolder,
                new BasicResponseRenderer());
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
    
    public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
        mappingsLoader.loadMappingsInto(mappings);
    }
    
    public void addMockServiceRequestListener(RequestListener listener) {
        mockServiceRequestHandler.addRequestListener(listener);
    }
    
}
