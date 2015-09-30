package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.http.Request;

public abstract class AbstractTransformer<T> implements Extension {

    public abstract T transform(Request request, T response, FileSource files, Parameters parameters);

    public boolean applyGlobally() {
        return true;
    }
}
