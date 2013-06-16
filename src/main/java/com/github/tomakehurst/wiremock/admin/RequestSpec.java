package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.RequestMethod;

import static com.google.common.base.Preconditions.checkNotNull;

public class RequestSpec {

    private final RequestMethod method;
    private final String path;

    public RequestSpec(RequestMethod method, String path) {
        checkNotNull(method);
        checkNotNull(path);
        this.method = method;
        this.path = path;
    }

    public static RequestSpec requestSpec(RequestMethod method, String path) {
        return new RequestSpec(method, path);
    }

    public RequestMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestSpec that = (RequestSpec) o;

        if (method != that.method) return false;
        if (!path.equals(that.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}
