package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public interface AbstractTransformerV2<T> extends Extension {

    T transform(ServeEvent serveEvent, FileSource fileSource);

    default boolean applyGlobally() {
        return true;
    }
}
