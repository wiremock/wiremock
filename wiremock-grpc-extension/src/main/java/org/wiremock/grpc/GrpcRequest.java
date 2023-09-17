package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.http.*;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.*;

public class GrpcRequest implements Request {

    private final String scheme;
    private final String host;
    private final int port;
    private final String serviceName;
    private final String methodName;

    private final String body;

    public GrpcRequest(String scheme, String host, int port, String serviceName, String methodName, DynamicMessage message) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.methodName = methodName;

        body = Exceptions.uncheck(() -> JsonFormat.printer().print(message), String.class);
    }

    @Override
    public String getUrl() {
        return "/" + serviceName + "/" + methodName;
    }

    @Override
    public String getAbsoluteUrl() {
        return scheme + "://" + host + ":" + port + getUrl();
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getClientIp() {
        return null;
    }

    @Override
    public String getHeader(String key) {
        return null;
    }

    @Override
    public HttpHeader header(String key) {
        return HttpHeader.absent(key);
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        return ContentTypeHeader.absent();
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.noHeaders();
    }

    @Override
    public boolean containsHeader(String key) {
        return false;
    }

    @Override
    public Set<String> getAllHeaderKeys() {
        return emptySet();
    }

    @Override
    public QueryParameter queryParameter(String key) {
        return QueryParameter.absent(key);
    }

    @Override
    public FormParameter formParameter(String key) {
        return FormParameter.absent(key);
    }

    @Override
    public Map<String, FormParameter> formParameters() {
        return emptyMap();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return emptyMap();
    }

    @Override
    public byte[] getBody() {
        return Strings.bytesFromString(body);
    }

    @Override
    public String getBodyAsString() {
        return body;
    }

    @Override
    public String getBodyAsBase64() {
        return Encoding.encodeBase64(getBody());
    }

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public Collection<Part> getParts() {
        return emptyList();
    }

    @Override
    public Part getPart(String name) {
        return null;
    }

    @Override
    public boolean isBrowserProxyRequest() {
        return false;
    }

    @Override
    public Optional<Request> getOriginalRequest() {
        return Optional.empty();
    }

    @Override
    public String getProtocol() {
        return "HTTP/2";
    }
}
