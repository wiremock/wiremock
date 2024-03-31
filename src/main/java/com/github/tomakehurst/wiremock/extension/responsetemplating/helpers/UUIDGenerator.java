package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import java.util.UUID;

public class UUIDGenerator implements RandomStringGenerator{
    @Override
    public String generate(int length) {
        return UUID.randomUUID().toString();
    }
}
