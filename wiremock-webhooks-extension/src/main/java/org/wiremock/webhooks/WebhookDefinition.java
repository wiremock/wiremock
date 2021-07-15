package org.wiremock.webhooks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;

public class WebhookDefinition {
    
    private RequestMethod method;
    private URI url;
    private List<HttpHeader> headers;
    private Body body = Body.none();

    public static WebhookDefinition from(Parameters parameters) {
        return new WebhookDefinition(
                RequestMethod.fromString(parameters.getString("method", "GET")),
                URI.create(parameters.getString("url")),
                toHttpHeaders(parameters.getMetadata("headers", null)),
                parameters.getString("body", null),
                parameters.getString("base64Body", null)
        );
    }

    private static HttpHeaders toHttpHeaders(Metadata headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            return null;
        }

        return new HttpHeaders(
                headerMap.entrySet().stream()
                    .map(entry -> new HttpHeader(
                            entry.getKey(),
                            getHeaderValues(entry.getValue()))
                    )
                    .collect(Collectors.toList())
        );
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getHeaderValues(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof List) {
            return ((List<String>) obj);
        }

        return singletonList(obj.toString());
    }

    @JsonCreator
    public WebhookDefinition(@JsonProperty("method") RequestMethod method,
                             @JsonProperty("url") URI url,
                             @JsonProperty("headers") HttpHeaders headers,
                             @JsonProperty("body") String body,
                             @JsonProperty("base64Body") String base64Body) {
        this.method = method;
        this.url = url;
        this.headers = headers != null ? new ArrayList<>(headers.all()) : null;

        if (body != null) {
            this.body = new Body(body);
        } else if (base64Body != null) {
            this.body = new Body(decodeBase64(base64Body));
        }
    }

    public WebhookDefinition() {
    }

    public RequestMethod getMethod() {
        return method;
    }

    public URI getUrl() {
        return url;
    }

    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    public String getBase64Body() {
        return body.isBinary() ? body.asBase64() : null;
    }

    public String getBody() {
        return body.isBinary() ? null : body.asString();
    }

    @JsonIgnore
    public byte[] getBinaryBody() {
        return body.asBytes();
    }

    public WebhookDefinition withMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public WebhookDefinition withUrl(URI url) {
        this.url = url;
        return this;
    }

    public WebhookDefinition withUrl(String url) {
        withUrl(URI.create(url));
        return this;
    }

    public WebhookDefinition withHeaders(List<HttpHeader> headers) {
        this.headers = headers;
        return this;
    }

    public WebhookDefinition withHeader(String key, String... values) {
        if (headers == null) {
            headers = newArrayList();
        }

        headers.add(new HttpHeader(key, values));
        return this;
    }

    public WebhookDefinition withBody(String body) {
        this.body = new Body(body);
        return this;
    }

    public WebhookDefinition withBinaryBody(byte[] body) {
        this.body = new Body(body);
        return this;
    }

    @JsonIgnore
    public boolean hasBody() {
        return body != null && body.isPresent();
    }
}
