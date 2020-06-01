package com.github.tomakehurst.wiremock.http;

import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.net.ssl.SSLException;
import java.util.Collection;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThrows;

@RunWith(Parameterized.class)
public class HttpClientFactoryRejectsUntrustedCertificatesTest extends HttpClientFactoryCertificateVerificationTest {

    @Parameters(name = "{index}: trusted={0}, certificateCN={1}, validCertificate={2}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
               // trusted                     certificateCN validCertificate?
                { TRUST_NOBODY,               "other.com",  true  },
                { TRUST_NOBODY,               "other.com",  false },
                { TRUST_NOBODY,               "localhost",  false },
                { singletonList("other.com"), "other.com",  true  },
                { singletonList("other.com"), "other.com",  false },
                { singletonList("other.com"), "localhost",  false }
        });
    }

    @Test
    public void certificatesAreRejectedAsExpected() {

        server.stubFor(get("/whatever").willReturn(aResponse().withBody("Hello World")));

        assertThrows(SSLException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Exception {
                client.execute(new HttpGet(server.url("/whatever")));
            }
        });
    }

    public HttpClientFactoryRejectsUntrustedCertificatesTest(
        List<String> trustedHosts,
        String certificateCN,
        boolean validCertificate
    ) {
        super(trustedHosts, certificateCN, validCertificate);
    }
}
