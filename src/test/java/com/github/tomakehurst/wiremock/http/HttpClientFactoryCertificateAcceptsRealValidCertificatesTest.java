package com.github.tomakehurst.wiremock.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;

public class HttpClientFactoryCertificateAcceptsRealValidCertificatesTest extends HttpClientFactoryCertificateVerificationTest {

    public HttpClientFactoryCertificateAcceptsRealValidCertificatesTest() {
        super(Collections.<String>emptyList(), "localhost", true);
    }

    @Test
    public void realCertificateIsAccepted() throws Exception {

        HttpResponse response = client.execute(new HttpGet("https://www.example.com/"));

        getEntityAsStringAndCloseStream(response);
    }
}
