package com.github.tomakehurst.wiremock.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger("WireMock");

    private final boolean verbose;

    public Slf4jNotifier(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void info(String message) {
        if (verbose) {
            log.info(message);
        }
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        log.error(message, t);
    }
}
