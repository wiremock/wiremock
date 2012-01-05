package com.tomakehurst.wiremock;

import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.Notifier;
import com.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.tomakehurst.wiremock.mapping.AdminRequestHandler;
import com.tomakehurst.wiremock.mapping.InMemoryMappings;
import com.tomakehurst.wiremock.mapping.Mappings;
import com.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.mapping.RequestListener;
import com.tomakehurst.wiremock.servlet.BasicResponseRenderer;
import com.tomakehurst.wiremock.servlet.MockServiceResponseRenderer;
import com.tomakehurst.wiremock.standalone.MappingsLoader;
import com.tomakehurst.wiremock.verification.InMemoryRequestJournal;

public class WireMockApp {
    
    public static final String FILES_ROOT = "__files";
    
    private final Mappings mappings;
    private final InMemoryRequestJournal requestJournal;
    private final RequestHandler mockServiceRequestHandler;
    private final RequestHandler adminRequestHandler;
    private final GlobalSettingsHolder globalSettingsHolder;

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
