package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public class StubRequestHandlerFactory {

    public static final String FILES_ROOT = "__files";

    public StubRequestHandler buildStubRequestHandler(StubServer stubServer, Admin admin,
                                                      Options options, GlobalSettingsHolder globalSettingsHolder, RequestJournal requestJournal, List<RequestFilter> stubRequestFilters) {
        Map<String, PostServeAction> postServeActions = options.extensionsOfType(PostServeAction.class);
        BrowserProxySettings browserProxySettings = options.browserProxySettings();
        return new StubRequestHandler(
                stubServer,
                new StubResponseRenderer(
                        options.filesRoot().child(FILES_ROOT),
                        globalSettingsHolder,
                        new ProxyResponseRenderer(
                                options.proxyVia(),
                                options.httpsSettings().trustStore(),
                                options.shouldPreserveHostHeader(),
                                options.proxyHostHeader(),
                                globalSettingsHolder,
                                browserProxySettings.trustAllProxyTargets(),
                                browserProxySettings.trustedProxyTargets()
                        ),
                        ImmutableList.copyOf(options.extensionsOfType(ResponseTransformer.class).values())
                ),
                admin,
                postServeActions,
                requestJournal,
                stubRequestFilters,
                options.getStubRequestLoggingDisabled()
        );
    }
}
