package com.github.tomakehurst.wiremock.direct;

class SleepFacade {
    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
