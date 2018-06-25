package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * @author Christopher Holomek
 */
@RunWith(JMock.class)
public class ProxyHandlerTest {

    // must be set here because of JMock. local variable not possible.
    private Mockery context;

    private ProxyHandler proxyHandler;
    private static final UUID EXISTING_UUID_IS_PROXY = UUID.randomUUID();
    private static final UUID EXISTING_UUID_IS_PROXY_BINARY = UUID.randomUUID();
    private static final UUID EXISTING_UUID_IS_NO_PROXY = UUID.randomUUID();
    private static final UUID NOT_EXISTING_UUID = UUID.randomUUID();


    @Before
    public void before() {
        this.context = new Mockery();
        final Admin admin = this.context.mock(Admin.class);

        this.proxyHandler = new ProxyHandler(admin);

        this.context.checking(new Expectations() {
            {
                this.ignoring(admin).getStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_PROXY);
                this.will(returnValue(
                        new SingleStubMappingResult(
                                ProxyHandlerTest.this.createDefaultStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_PROXY, true))));
            }
        });

        this.context.checking(new Expectations() {
            {
                this.ignoring(admin).getStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_PROXY_BINARY);
                this.will(returnValue(
                        new SingleStubMappingResult(
                                ProxyHandlerTest.this.createBinaryStubMapping())));
            }
        });

        this.context.checking(new Expectations() {
            {
                this.ignoring(admin).getStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_NO_PROXY);
                this.will(returnValue(new SingleStubMappingResult(
                        ProxyHandlerTest.this.createDefaultStubMapping(ProxyHandlerTest.EXISTING_UUID_IS_NO_PROXY, false))));
            }
        });

        this.context.checking(new Expectations() {
            {
                this.ignoring(admin).getStubMapping(ProxyHandlerTest.NOT_EXISTING_UUID);
                this.will(returnValue(new SingleStubMappingResult(null)));
            }
        });

    }

    @Test
    public void testClear() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assert.assertFalse(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.clear();

        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());
    }


    @Test
    public void testRemoveProxyConfig() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assert.assertFalse(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.removeProxyConfig(EXISTING_UUID_IS_PROXY);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));
    }

    @Test
    public void testDisableProxy() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assert.assertTrue(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));
    }

    @Test
    public void testDisableNoProxyMapping() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_NO_PROXY);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_NO_PROXY.toString()));
    }

    @Test
    public void testDisableNoMapping() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(NOT_EXISTING_UUID);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(NOT_EXISTING_UUID.toString()));
    }

    @Test
    public void testEnableProxy() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assert.assertTrue(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));

        final String url = this.proxyHandler.getConfig().get(EXISTING_UUID_IS_PROXY.toString());

        Assert.assertEquals("http://localhost:8080", url);

        this.proxyHandler.enableProxyUrl(EXISTING_UUID_IS_PROXY);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY.toString()));
    }

    @Test
    public void testEnableNoProxyMapping() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.enableProxyUrl(EXISTING_UUID_IS_NO_PROXY);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_NO_PROXY.toString()));
    }

    @Test
    public void testEnableNoMapping() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        this.proxyHandler.enableProxyUrl(NOT_EXISTING_UUID);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(NOT_EXISTING_UUID.toString()));
    }

    @Test
    public void testBinary() {
        Assert.assertTrue(this.proxyHandler.getConfig().isEmpty());

        // first we need to disable proxy so that we have something to remove
        this.proxyHandler.disableProxyUrl(EXISTING_UUID_IS_PROXY_BINARY);

        Assert.assertTrue(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY_BINARY.toString()));

        final String url = this.proxyHandler.getConfig().get(EXISTING_UUID_IS_PROXY_BINARY.toString());

        Assert.assertEquals("http://localhost:8080", url);

        this.proxyHandler.enableProxyUrl(EXISTING_UUID_IS_PROXY_BINARY);

        Assert.assertFalse(this.proxyHandler.getConfig().containsKey(EXISTING_UUID_IS_PROXY_BINARY.toString()));
    }

    private StubMapping createDefaultStubMapping(final UUID id, final boolean isProxy) {
        final StubMapping stubMapping = new StubMapping();
        final String proxyUrl = isProxy ? "http://localhost:8080" : null;
        final ResponseDefinition responseDefinition = new ResponseDefinition(200, "test", "test", null, null, null, null, null, null, null,
                                                                             null,
                                                                             proxyUrl, null, null, null, null);

        stubMapping.setResponse(responseDefinition);
        stubMapping.setId(id);

        return stubMapping;
    }

    private StubMapping createBinaryStubMapping() {
        final StubMapping stubMapping = new StubMapping();
        final ResponseDefinition responseDefinition = new ResponseDefinition(200, "test", new byte[0], null, null, null, null, null, null,
                                                                             null,
                                                                             null,
                                                                             "http://localhost:8080", null, null, null, null);

        stubMapping.setResponse(responseDefinition);
        stubMapping.setId(EXISTING_UUID_IS_PROXY_BINARY);

        return stubMapping;
    }

}