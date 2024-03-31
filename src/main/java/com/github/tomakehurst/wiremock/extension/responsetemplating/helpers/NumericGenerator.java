package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.common.Strings.randomNumeric;

public class NumericGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return randomNumeric(length);
    }
}
