package com.github.tomakehurst.wiremock.common;

import java.io.InputStream;

public interface StreamSource {
    InputStream getStream();
}
