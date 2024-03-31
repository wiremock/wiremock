package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.common.Strings.random;

public class AplhanumericAndSymbolsGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return random(length, 33, 126, false, false);
    }
}
