package com.github.tomakehurst.wiremock.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class HttpClientFactoryAcceptsTrustedCertificatesTest extends HttpClientFactoryCertificateVerificationTest {

    @Parameters(name = "{index}: trusted={0}, certificateCN={1}, validCertificate={2}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
               // trusted                     certificateCN validCertificate?
                { TRUST_NOBODY,               "localhost",  true  },
                { singletonList("other.com"), "localhost",  true  },
                { singletonList("localhost"), "other.com",  true  },
                { singletonList("localhost"), "other.com",  false },
                { singletonList("localhost"), "localhost",  true  },
                { singletonList("localhost"), "localhost",  false },
        });
    }

    @Test
    public void certificatesAreAccepted() throws Exception {

        server.stubFor(get("/whatever").willReturn(aResponse().withBody("Hello World")));

        HttpResponse response = client.execute(new HttpGet(server.url("/whatever")));

        String result = getEntityAsStringAndCloseStream(response);

        assertEquals("Hello World", result);
    }

    public HttpClientFactoryAcceptsTrustedCertificatesTest(
        List<String> trustedHosts,
        String certificateCN,
        boolean validCertificate
    ) {
        super(trustedHosts, certificateCN, validCertificate);
    }
}
