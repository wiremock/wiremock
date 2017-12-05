package com.github.tomakehurst.wiremock.common;

public class AsynchronousResponseSettings {

    private final boolean enabled;
    private final int threads;

    public AsynchronousResponseSettings(boolean enabled, int threads) {
        this.enabled = enabled;
        this.threads = threads;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getThreads() {
        return threads;
    }
}
