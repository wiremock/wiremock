package com.github.tomakehurst.wiremock.http;

public enum CacheStrategy {
    Never, OnCall, Always;

    public static boolean isNever(CacheStrategy cacheStrategy) {
        return cacheStrategy == null || cacheStrategy == CacheStrategy.Never;
    }

    public static boolean isAlways(CacheStrategy cacheStrategy) {
        return cacheStrategy == Always;
    }
}
