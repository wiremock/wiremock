package com.github.tomakehurst.wiremock.testsupport;

import org.apache.commons.lang3.SystemUtils;

import static org.junit.Assume.assumeFalse;

public class Assumptions {

    public static void doNotRunOnMacOSXInCI() {
        assumeFalse(SystemUtils.IS_OS_MAC_OSX && "true".equalsIgnoreCase(System.getenv("CI")));
    }
}
