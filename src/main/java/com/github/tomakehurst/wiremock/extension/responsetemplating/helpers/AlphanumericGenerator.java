package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.common.Strings.randomAlphanumeric;

public class AlphanumericGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return randomAlphanumeric(length);
    }
}
