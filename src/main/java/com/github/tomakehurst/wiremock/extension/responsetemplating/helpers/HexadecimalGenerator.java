package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.common.Strings.random;

public class HexadecimalGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return random(length, "ABCDEF0123456789");
    }
}
