package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(JMock.class)
public class TemplateTransformerTest {

    private Mockery context;
    private TemplateTransformer transformer;

    @Before
    public void init() {
        context = new Mockery();
        transformer = new TemplateTransformer();
    }

    @Test
    public void readsRequestBody() {
        Request request = new MockRequestBuilder(context).withBody("request body").build();
        Response response = Response.response().status(200).body("$request.body$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("request body"));
    }

    @Test
    public void ignoresEscapedDelimiter() {
        Request request = new MockRequestBuilder(context).build();
        Response response = Response.response().status(200).body("\\$request.body\\$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("$request.body$"));
    }

    @Test
    public void readsRequestHeaders() {
        Request request = new MockRequestBuilder(context).withHeader("Accept", "text/plain").build();
        Response response = Response.response().status(200).body("$request.headers.Accept$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("text/plain"));
    }

    @Test
    public void readsRequestQueryParams() {
        Request request = new MockRequestBuilder(context).withUrl("/widgets?id=5").build();
        Response response = Response.response().status(200).body("$request.query.id$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("5"));
    }

    @Test
    public void readsRequestMethod() {
        Request request = new MockRequestBuilder(context).withMethod(RequestMethod.POST).build();
        Response response = Response.response().status(200).body("$request.method$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("POST"));
    }

    @Test
    public void convertsHyphensToUnderscoreInHeaderNames() {
        Request request = new MockRequestBuilder(context).withHeader("Content-Type", "text/plain").build();
        Response response = Response.response().status(200).body("$request.headers.Content_Type$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("text/plain"));
    }

    @Test
    public void readsRequestUrl() {
        Request request = new MockRequestBuilder(context).withUrl("/widgets?id=5").build();
        Response response = Response.response().status(200).body("$request.url$").build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getBodyAsString(), is("/widgets?id=5"));
    }

    @Test
    public void substitutesInResponseHeaders() {
        Request request = new MockRequestBuilder(context).withHeader("Accept", "text/plain").build();
        Response response = Response.response().status(200)
                .headers(new HttpHeaders(new HttpHeader("Content-Type", "$request.headers.Accept$"))).build();
        Response transformedResponse = transformer.transform(request, response, null, null);
        assertThat(transformedResponse.getHeaders().getContentTypeHeader().mimeTypePart(), is("text/plain"));
    }
}
