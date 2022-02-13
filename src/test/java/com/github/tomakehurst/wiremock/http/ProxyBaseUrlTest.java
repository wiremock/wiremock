/*
 * Copyright (C) 2020-2021 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.crypto.CertificateSpecification;
import com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore;
import com.github.tomakehurst.wiremock.crypto.Secret;
import com.github.tomakehurst.wiremock.crypto.X509CertificateSpecification;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.crypto.X509CertificateVersion.V3;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledForJreRange(
        min = JRE.JAVA_17,
        disabledReason = "does not support generating certificates at runtime")
public class ProxyBaseUrlTest {

    @RegisterExtension
    public WireMockExtension endInstance = newWireMockInstance();

    @RegisterExtension
    public WireMockExtension proxyInstance = newWireMockInstance();

    private final ProxyResponseRenderer proxyResponseRenderer = buildProxyResponseRenderer(false);

    private final String URL_PATH = "/api";
    private final String REQUEST_BODY = "{\"test\":true}";
    private final String RESPONSE_BODY = "Result";
    private final String HEADER_NAME = "testHeader";
    private final String HEADER_VALUE = "testHeaderValue";
    private final String QUERY_NAME = "testParam";
    private final String QUERY_VALUE = "testParamValue";

    @Test
    public void postMethodTest() {
        addProxyStub(postMappingBuilder());
        addEndStub(postMappingBuilder());
        renderAndCheckResponse(RequestMethod.POST);
    }

    @Test
    public void getMethodTest() {
        addProxyStub(getMappingBuilder());
        addEndStub(getMappingBuilder());
        renderAndCheckResponse(RequestMethod.GET);
    }

    @Test
    public void deleteMethodTest() {
        addProxyStub(deleteMappingBuilder());
        addEndStub(deleteMappingBuilder());
        renderAndCheckResponse(RequestMethod.DELETE);
    }

    @Test
    public void putMethodTest() {
        addProxyStub(putMappingBuilder());
        addEndStub(putMappingBuilder());
        renderAndCheckResponse(RequestMethod.PUT);
    }

    @Test
    public void patchMethodTest() {
        addProxyStub(patchMappingBuilder());
        addEndStub(patchMappingBuilder());
        renderAndCheckResponse(RequestMethod.PATCH);
    }

    private void renderAndCheckResponse(RequestMethod requestMethod) {
        ServeEvent serveEvent = serveEvent(requestMethod);
        Response response = proxyResponseRenderer.render(serveEvent);
        assertEquals(response.getBodyAsString(), RESPONSE_BODY);
    }

    private ServeEvent serveEvent(RequestMethod method) {
        String path = URL_PATH + "?" + QUERY_NAME + "=" + QUERY_VALUE;

        LoggedRequest loggedRequest =
                new LoggedRequest(
                        /* url = */ path,
                        /* absoluteUrl = */ proxyInstance.url(path),
                        /* method = */ method,
                        /* clientIp = */ "127.0.0.1",
                        /* headers = */ new HttpHeaders(HttpHeader.httpHeader(HEADER_NAME, HEADER_VALUE)),
                        /* cookies = */ new HashMap<String, Cookie>(),
                        /* isBrowserProxyRequest = */ false,
                        /* loggedDate = */ new Date(),
                        /* body = */ REQUEST_BODY.getBytes(),
                        /* multiparts = */ null);
        ResponseDefinition responseDefinition = aResponse().proxiedFrom(proxyInstance.baseUrl()).build();
        responseDefinition.setOriginalRequest(loggedRequest);

        return ServeEvent.of(loggedRequest, responseDefinition, new StubMapping());
    }

    private File generateKeystore() throws Exception {

        InMemoryKeyStore ks =
                new InMemoryKeyStore(InMemoryKeyStore.KeyStoreType.JKS, new Secret("password"));

        CertificateSpecification certificateSpecification =
                new X509CertificateSpecification(
                        /* version = */ V3,
                        /* subject = */ "CN=localhost",
                        /* issuer = */ "CN=wiremock.org",
                        /* notBefore = */ new Date(),
                        /* notAfter = */ new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)));
        KeyPair keyPair = generateKeyPair();
        ks.addPrivateKey("wiremock", keyPair, certificateSpecification.certificateFor(keyPair));

        File keystoreFile = File.createTempFile("wiremock-test", "keystore");

        ks.saveAs(keystoreFile);

        return keystoreFile;
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }

    private ProxyResponseRenderer buildProxyResponseRenderer(boolean trustAllProxyTargets) {
        return new ProxyResponseRenderer(
                ProxySettings.NO_PROXY,
                KeyStoreSettings.NO_STORE,
                /* preserveHostHeader = */ false,
                /* hostHeaderValue = */ null,
                new GlobalSettingsHolder(),
                trustAllProxyTargets,
                Collections.<String>emptyList());
    }

    private WireMockExtension newWireMockInstance() {
        WireMockExtension result = null;
        try {
            result = WireMockExtension.newInstance()
                    .options(
                            options()
                                    .httpDisabled(true)
                                    .dynamicHttpsPort()
                                    .keystorePath(generateKeystore().getAbsolutePath()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void addProxyStub(MappingBuilder stubBuilder) {
        proxyInstance.stubFor(
//                any(urlPathEqualTo("/api"))
                stubBuilder.willReturn(aResponse().proxiedFrom(endInstance.baseUrl()))
        );
    }

    private void addEndStub(MappingBuilder stubBuilder) {
        endInstance.stubFor(
                stubBuilder
                        .withRequestBody(equalToJson(REQUEST_BODY))
                        .withHeader(HEADER_NAME, equalTo(HEADER_VALUE))
                        .withQueryParam(QUERY_NAME, equalTo(QUERY_VALUE))
                        .willReturn(aResponse().withBody(RESPONSE_BODY))
        );
    }

    private UrlPathPattern getUrlPathPattern() {
        return urlPathEqualTo(URL_PATH);
    }

    private MappingBuilder getMappingBuilder() {
        return get(getUrlPathPattern());
    }

    private MappingBuilder postMappingBuilder() {
        return post(getUrlPathPattern());
    }

    private MappingBuilder deleteMappingBuilder() {
        return delete(getUrlPathPattern());
    }

    private MappingBuilder putMappingBuilder() {
        return put(getUrlPathPattern());
    }

    private MappingBuilder patchMappingBuilder() {
        return patch(getUrlPathPattern());
    }
}
