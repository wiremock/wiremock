package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.util.Objects;

public class TemplateCacheKey {

    public enum ResponseElement { BODY, PROXY_URL, HEADER }

    private final ResponseDefinition responseDefinition;
    private final ResponseElement element;
    private final String name;
    private final Integer index;

    public static TemplateCacheKey forInlineBody(ResponseDefinition responseDefinition) {
        return new TemplateCacheKey(responseDefinition, ResponseElement.BODY, "[inlineBody]", null);
    }

    public static TemplateCacheKey forFileBody(ResponseDefinition responseDefinition, String filename) {
        return new TemplateCacheKey(responseDefinition, ResponseElement.BODY, filename, null);
    }

    public static TemplateCacheKey forHeader(ResponseDefinition responseDefinition, String headerName, int valueIndex) {
        return new TemplateCacheKey(responseDefinition, ResponseElement.HEADER, headerName, valueIndex);
    }

    public static TemplateCacheKey forProxyUrl(ResponseDefinition responseDefinition) {
        return new TemplateCacheKey(responseDefinition, ResponseElement.PROXY_URL, "[proxyUrl]", null);
    }

    private TemplateCacheKey(ResponseDefinition responseDefinition, ResponseElement element, String name, Integer index) {
        this.responseDefinition = responseDefinition;
        this.element = element;
        this.name = name;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateCacheKey that = (TemplateCacheKey) o;
        return responseDefinition.equals(that.responseDefinition) &&
                element == that.element &&
                name.equals(that.name) &&
                Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseDefinition, element, name, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TemplateCacheKey{");
        sb.append("responseDefinition=").append(responseDefinition);
        sb.append(", element=").append(element);
        sb.append(", name='").append(name).append('\'');
        sb.append(", index=").append(index);
        sb.append('}');
        return sb.toString();
    }
}
