package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestWrapper;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RequestFilterAcceptanceTest {

    private WireMockServer wm;
    private WireMockTestClient client;
    private String url;

    @Test
    public void filterCanContinueWithModifiedRequest() {
        initialise(new RequestHeaderModifyingFilter());

        wm.stubFor(get(url)
                .withHeader("X-Modify-Me", equalTo("modified"))
                .willReturn(ok()));

        WireMockResponse response = client.get(url, withHeader("X-Modify-Me", "original"));
        assertThat(response.statusCode(), is(200));
    }

    @Test
    public void filterCanStopWithResponse() {
        initialise(new AuthenticatingFilter());

        wm.stubFor(get(url).willReturn(ok()));

        WireMockResponse good = client.get(url, withHeader("Authorization", "Token 123"));
        assertThat(good.statusCode(), is(200));

        WireMockResponse bad = client.get(url);
        assertThat(bad.statusCode(), is(401));
    }

    @Test
    public void filtersAreChained() {
        initialise(
                new RequestHeaderAppendingFilter("A"),
                new RequestHeaderAppendingFilter("B"),
                new RequestHeaderAppendingFilter("C")
        );

        wm.stubFor(get(url)
                .withHeader("X-Modify-Me", equalTo("_ABC"))
                .willReturn(ok()));

        WireMockResponse response = client.get(url, withHeader("X-Modify-Me", "_"));
        assertThat(response.statusCode(), is(200));
    }

    @Before
    public void init() {
        url = "/" + RandomStringUtils.randomAlphabetic(5);
    }

    @After
    public void stopServer() {
        wm.stop();
    }

    private void initialise(RequestFilter... filters) {
        wm = new WireMockServer(wireMockConfig().dynamicPort().extensions(filters));
        wm.start();
        client = new WireMockTestClient(wm.port());
    }

    public static class RequestHeaderModifyingFilter implements RequestFilter {

        @Override
        public RequestFilterAction filter(Request request) {
            Request newRequest = new RequestWrapper(request) {
                @Override
                public HttpHeader header(String key) {
                    if (key.equals("X-Modify-Me")) {
                        return new HttpHeader("X-Modify-Me", "modified");
                    }

                    return super.header(key);
                }
            };

            return RequestFilterAction.continueWith(newRequest);
        }

        @Override
        public String getName() {
            return "request-header-modifier";
        }
    }

    public static class AuthenticatingFilter implements RequestFilter {

        @Override
        public RequestFilterAction filter(Request request) {
            HttpHeader authHeader = request.header("Authorization");
            if (!authHeader.isPresent() || !authHeader.firstValue().equals("Token 123")) {
                return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());
            }

            return RequestFilterAction.continueWith(request);
        }

        @Override
        public String getName() {
            return "authenticator";
        }
    }

    public static class RequestHeaderAppendingFilter implements RequestFilter {

        private final String value;

        public RequestHeaderAppendingFilter(String value) {
            this.value = value;
        }

        @Override
        public RequestFilterAction filter(Request request) {
            Request newRequest = new RequestWrapper(request) {
                @Override
                public HttpHeader header(String key) {
                    HttpHeader existingHeader = super.header(key);
                    if (key.equals("X-Modify-Me")) {
                        return new HttpHeader("X-Modify-Me", existingHeader.firstValue() + value);
                    }

                    return existingHeader;
                }
            };

            return RequestFilterAction.continueWith(newRequest);
        }

        @Override
        public String getName() {
            return "request-header-appender-" + value;
        }
    }


}
