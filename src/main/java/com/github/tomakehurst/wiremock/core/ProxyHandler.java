package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.jetty9.websockets.Message;
import com.github.tomakehurst.wiremock.jetty9.websockets.WebSocketEndpoint;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christopher Holomek
 */
public class ProxyHandler {
    private final Map<UUID, String> proxyUrl = new ConcurrentHashMap<>();

    private final WireMockApp app;

    public ProxyHandler(final WireMockApp app) {
        this.app = app;
    }

    public void clear() {
        this.proxyUrl.clear();
    }

    public void removeProxyConfig(final UUID uuid) {
        this.proxyUrl.remove(uuid);
    }

    public void disableProxyUrl(final UUID uuid) {
        final List<StubMapping> mappings = this.app.listAllStubMappings().getMappings();

        for (final StubMapping mapping : mappings) {
            if (mapping.getUuid().equals(uuid) && mapping.getResponse().isProxyResponse()
                    && !this.proxyUrl.containsKey(uuid)) {
                this.storeProxyUrl(mapping);
                this.disableProxyUrlInMapping(mapping);
                break;
            }
        }
        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    public void enableProxyUrl(final UUID uuid) {
        final List<StubMapping> mappings = this.app.listAllStubMappings().getMappings();

        for (final StubMapping mapping : mappings) {
            if (mapping.getUuid().equals(uuid) && this.proxyUrl.containsKey(uuid)) {
                this.enableProxyUrlInMapping(mapping);
                this.removeProxyUrl(mapping);
                break;
            }
        }
        WebSocketEndpoint.broadcast(Message.MAPPINGS);
    }

    private void disableProxyUrlInMapping(final StubMapping mapping) {

        final ResponseDefinition response = mapping.getResponse();
        final ResponseDefinition copy = this.copyResponseDefinition(response, null);

        //we are manipulation the mapping directly instead of editing.
        //The editing function would replace the complete stubmapping. Not sure if this is a good idea.
        mapping.setResponse(copy);
    }

    private void enableProxyUrlInMapping(final StubMapping mapping) {

        final ResponseDefinition response = mapping.getResponse();
        final ResponseDefinition copy = this.copyResponseDefinition(response, this.proxyUrl.get(mapping.getUuid()));

        //we are manipulation the mapping directly instead of editing.
        //The editing function would replace the complete stubmapping. Not sure if this is a good idea.
        mapping.setResponse(copy);
    }

    private void storeProxyUrl(final StubMapping mapping) {
        this.proxyUrl.put(mapping.getUuid(), mapping.getResponse().getProxyBaseUrl());
    }

    private void removeProxyUrl(final StubMapping mapping) {
        this.proxyUrl.remove(mapping.getUuid());
    }

    private ResponseDefinition copyResponseDefinition(final ResponseDefinition response, final String proxyUrl) {
        final ResponseDefinition copy;
        if (response.getByteBodyIfBinary() != null) {
            //Binary body
            copy = new ResponseDefinition(response.getStatus(), response.getStatusMessage(),
                                          response.getByteBody(), null, null, response.getBodyFileName(), response.getHeaders(),
                                          response.getAdditionalProxyRequestHeaders(), response.getFixedDelayMilliseconds(),
                                          response.getDelayDistribution(),
                                          response.getChunkedDribbleDelay(),
                                          //proxy url
                                          proxyUrl,
                                          //end
                                          response.getFault(), response.getTransformers(), response.getTransformerParameters(),
                                          response.wasConfigured());
        } else {
            //String body
            copy = new ResponseDefinition(response.getStatus(), response.getStatusMessage(),
                                          response.getBody(), null, null, response.getBodyFileName(), response.getHeaders(),
                                          response.getAdditionalProxyRequestHeaders(), response.getFixedDelayMilliseconds(),
                                          response.getDelayDistribution(),
                                          response.getChunkedDribbleDelay(),
                                          //proxy url
                                          proxyUrl,
                                          //end
                                          response.getFault(), response.getTransformers(), response.getTransformerParameters(),
                                          response.wasConfigured());
        }

        return copy;
    }

    public Map<String, String> getConfig() {
        final Set<UUID> keys = this.proxyUrl.keySet();

        final HashMap<String, String> copy = new HashMap<>();

        for (final UUID uuid : keys) {
            final String s = this.proxyUrl.get(uuid);
            if (s != null) {
                copy.put(uuid.toString(), s);
            }
        }
        return copy;
    }
}
