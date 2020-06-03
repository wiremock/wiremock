package com.github.tomakehurst.wiremock.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import javax.net.ssl.SSLException;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static org.junit.Assert.assertThrows;

public class HttpClientFactoryCertificateAcceptsRealValidCertificatesTest extends HttpClientFactoryCertificateVerificationTest {

    public HttpClientFactoryCertificateAcceptsRealValidCertificatesTest() {
        super(Collections.<String>emptyList(), "localhost", true);
    }

    @Test
    public void realCertificateIsAccepted() throws Exception {

        HttpResponse response = client.execute(new HttpGet("https://www.example.com/"));

        getEntityAsStringAndCloseStream(response);
    }

    @Test
    public void invalidRealCertificateIsRejected() {

        assertThrows(SSLException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Exception {
                client.execute(new HttpGet("https://wiremock.org/"));
            }
        });
    }
}
