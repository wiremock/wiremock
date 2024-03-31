package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.common.Strings.randomAscii;

public class DefautGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return randomAscii(length);
    }
}
