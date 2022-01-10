package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Christopher Holomek
 */
public class ProxyHandlerTest {

    private ProxyHandler proxyHandler;
    private static final UUID EXISTING_UUID_IS_PROXY = UUID.randomUUID();
    private static final UUID EXISTING_UUID_IS_PROXY_BINARY = UUID.randomUUID();
    private static final UUID EXISTING_UUID_IS_NO_PROXY = UUID.randomUUID();
    private static final UUID NOT_EXISTING_UUID = UUID.randomUUID();


    @BeforeEach
    public void before() {
        final Admin admin = mock(Admin.class);

        this.proxyHandler = new ProxyHandler(admin);

        when(admin.getStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_PROXY)).thenReturn(new SingleStubMappingResult(
                ProxyHandlerTest.this.createDefaultStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_PROXY, true)));

        when(admin.getStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_PROXY_BINARY)).thenReturn(new SingleStubMappingResult(
                ProxyHandlerTest.this.createBinaryStubMapping()));

        when(admin.getStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_NO_PROXY)).thenReturn(new SingleStubMappingResult(
                ProxyHandlerTest.this.createDefaultStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_NO_PROXY, false)));

        when(admin.getStubMapping(ProxyHandlerTest.NOT_EXISTING_UUID)).thenReturn(new SingleStubMappingResult(null));
    }

    @Test
    public void testClear() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assertions.assertFalse(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.clear();

        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());
    }


    @Test
    public void testRemoveProxyConfig() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assertions.assertFalse(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.removeProxyConfig(EXISTING_UUID_IS_PROXY);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));
    }

    @Test
    public void testDisableProxy() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assertions.assertTrue(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));
    }

    @Test
    public void testDisableNoProxyMapping() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_NO_PROXY);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_NO_PROXY.toString()));
    }

    @Test
    public void testDisableNoMapping() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(NOT_EXISTING_UUID);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(NOT_EXISTING_UUID.toString()));
    }

    @Test
    public void testEnableProxy() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assertions.assertTrue(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));

        final String url = this.proxyHandler.getConfig().get(EXISTING_UUID_IS_PROXY.toString());

        Assertions.assertEquals("http://localhost:8080", url);

        this.proxyHandler.enableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));
    }

    @Test
    public void testEnableNoProxyMapping() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.enableProxyUrl(EXISTING_UUID_IS_NO_PROXY);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_NO_PROXY.toString()));
    }

    @Test
    public void testEnableNoMapping() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.enableProxyUrl(NOT_EXISTING_UUID);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(NOT_EXISTING_UUID.toString()));
    }

    @Test
    public void testBinary() {
        Assertions.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY_BINARY);

        Assertions.assertTrue(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY_BINARY.toString()));

        final String url = this.proxyHandler.getConfig().get(EXISTING_UUID_IS_PROXY_BINARY.toString());

        Assertions.assertEquals("http://localhost:8080", url);

        this.proxyHandler.enableProxyUrl(EXISTING_UUID_IS_PROXY_BINARY);

        Assertions.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY_BINARY.toString()));
    }

    private StubMapping createDefaultStubMapping(final UUID id, final boolean isProxy) {
        final StubMapping stubMapping = new StubMapping();
        final String proxyUrl = isProxy ? "http://localhost:8080" : null;
        final ResponseDefinition responseDefinition = new ResponseDefinition(200, "test", "test", null, null, null, null, null, null, null,
                null,
                proxyUrl, null, null, null, null, null);

        stubMapping.setResponse(responseDefinition);
        stubMapping.setId(id);

        return stubMapping;
    }

    private StubMapping createBinaryStubMapping() {
        final StubMapping stubMapping = new StubMapping();
        final ResponseDefinition responseDefinition = new ResponseDefinition(200, "test", new byte[0], null, null, null, null, null, null,
                null,
                null,
                "http://localhost:8080", null, null, null, null, null);

        stubMapping.setResponse(responseDefinition);
        stubMapping.setId(EXISTING_UUID_IS_PROXY_BINARY);

        return stubMapping;
    }

}