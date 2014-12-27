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

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.core.Container;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.global.ThreadSafeRequestDelayControl;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.jetty6.Jetty6HttpServerFactory;
import com.github.tomakehurst.wiremock.jetty6.LoggerAdapter;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSaver;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.StubMappingJsonRecorder;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.mortbay.log.Log;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.google.common.base.Preconditions.checkState;

public class WireMockServer implements Container, Stubbing {

	public static final String FILES_ROOT = "__files";
    public static final String MAPPINGS_ROOT = "mappings";

	private final WireMockApp wireMockApp;
    private final StubRequestHandler stubRequestHandler;

	private final HttpServer httpServer;
    private final FileSource fileSource;
	private final Notifier notifier;

    private final Options options;

    protected final WireMock client;

    public WireMockServer(Options options) {
        this.options = options;
        this.fileSource = options.filesRoot();
        this.notifier = options.notifier();

        RequestDelayControl requestDelayControl = new ThreadSafeRequestDelayControl();
        MappingsLoader defaultMappingsLoader = makeDefaultMappingsLoader();
        JsonFileMappingsSaver mappingsSaver = new JsonFileMappingsSaver(fileSource.child(MAPPINGS_ROOT));

        wireMockApp = new WireMockApp(
                requestDelayControl,
                options.browserProxyingEnabled(),
                defaultMappingsLoader,
                mappingsSaver,
                options.requestJournalDisabled(),
                options.extensionsOfType(ResponseTransformer.class),
                this
        );

        AdminRequestHandler adminRequestHandler = new AdminRequestHandler(
                wireMockApp,
                new BasicResponseRenderer()
        );
        stubRequestHandler = new StubRequestHandler(
                wireMockApp,
                new StubResponseRenderer(
                        fileSource.child(FILES_ROOT),
                        wireMockApp.getGlobalSettingsHolder(),
                        new ProxyResponseRenderer(
                                options.proxyVia(),
                                options.httpsSettings().trustStore(),
                                options.shouldPreserveHostHeader(),
                                options.proxyHostHeader()
                        )
                )
        );
        HttpServerFactory httpServerFactory = new Jetty6HttpServerFactory();
        httpServer = httpServerFactory.buildHttpServer(
                options,
                adminRequestHandler,
                stubRequestHandler,
                requestDelayControl
        );

        Log.setLog(new LoggerAdapter(notifier));

        client = new WireMock(wireMockApp);
    }

    private MappingsLoader makeDefaultMappingsLoader() {
        FileSource mappingsFileSource = fileSource.child("mappings");
        if (mappingsFileSource.exists()) {
            return new JsonFileMappingsLoader(mappingsFileSource);
        } else {
            return new NoOpMappingsLoader();
        }
    }

    public WireMockServer(int port, Integer httpsPort, FileSource fileSource, boolean enableBrowserProxying, ProxySettings proxySettings, Notifier notifier) {
        this(wireMockConfig()
                .port(port)
                .httpsPort(httpsPort)
                .fileSource(fileSource)
                .enableBrowserProxying(enableBrowserProxying)
                .proxyVia(proxySettings)
                .notifier(notifier));
    }

	public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying, ProxySettings proxySettings) {
        this(wireMockConfig()
                .port(port)
                .fileSource(fileSource)
                .enableBrowserProxying(enableBrowserProxying)
                .proxyVia(proxySettings));
	}

    public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying) {
        this(wireMockConfig()
                .port(port)
                .fileSource(fileSource)
                .enableBrowserProxying(enableBrowserProxying));
    }
	
	public WireMockServer(int port) {
		this(wireMockConfig().port(port));
	}

    public WireMockServer(int port, Integer httpsPort) {
        this(wireMockConfig().port(port).httpsPort(httpsPort));
    }
	
	public WireMockServer() {
		this(wireMockConfig());
	}
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		wireMockApp.loadMappingsUsing(mappingsLoader);
	}
	
	public void addMockServiceRequestListener(RequestListener listener) {
		stubRequestHandler.addRequestListener(listener);
	}
	
	public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
	    addMockServiceRequestListener(
                new StubMappingJsonRecorder(mappingsFileSource, filesFileSource, wireMockApp, options.matchingHeaders()));
	    notifier.info("Recording mappings to " + mappingsFileSource.getPath());
	}
	
	public void stop() {
        httpServer.stop();
	}
	
	public void start() {
        try {
		    httpServer.start();
        } catch (Exception e) {
            throw new FatalStartupException(e);
        }
	}

    /**
     * Gracefully shutdown the server.
     *
     * This method assumes it is being called as the result of an incoming HTTP request.
     */
    @Override
    public void shutdown() {
        final WireMockServer server = this;
        Thread shutdownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // We have to sleep briefly to finish serving the shutdown request before stopping the server, as
                    // there's no support in Jetty for shutting down after the current request.
                    // See http://stackoverflow.com/questions/4650713
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                server.stop();
            }
        });
        shutdownThread.start();
    }

    public int port() {
        checkState(
                isRunning(),
                "Not listening on HTTP port. The WireMock server is most likely stopped"
        );
        return httpServer.port();
    }

    public int httpsPort() {
        checkState(
                isRunning() && options.httpsSettings().enabled(),
                "Not listening on HTTPS port. Either HTTPS is not enabled or the WireMock server is stopped."
        );
        return httpServer.httpsPort();
    }

    public boolean isRunning() {
        return httpServer.isRunning();
    }

    @Override
    public void givenThat(MappingBuilder mappingBuilder) {
        client.register(mappingBuilder);
    }

    @Override
    public void stubFor(MappingBuilder mappingBuilder) {
        givenThat(mappingBuilder);
    }

    @Override
    public void verify(RequestPatternBuilder requestPatternBuilder) {
        client.verifyThat(requestPatternBuilder);
    }

    @Override
    public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
        client.verifyThat(count, requestPatternBuilder);
    }

    @Override
    public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return client.find(requestPatternBuilder);
    }

    @Override
    public void setGlobalFixedDelay(int milliseconds) {
        client.setGlobalFixedDelayVariable(milliseconds);
    }

    @Override
    public void addRequestProcessingDelay(int milliseconds) {
        client.addDelayBeforeProcessingRequests(milliseconds);
    }

    private static class NoOpMappingsLoader implements MappingsLoader {
        @Override
        public void loadMappingsInto(StubMappings stubMappings) {
            // do nothing
        }
    }
}
