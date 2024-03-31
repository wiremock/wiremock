package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.common.Strings.randomAlphabetic;

public class AlphabeticGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return randomAlphabetic(length);
    }
}
