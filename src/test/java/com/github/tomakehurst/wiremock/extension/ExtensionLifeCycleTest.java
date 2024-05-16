package com.github.tomakehurst.wiremock.extension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Notifier;
import org.junit.jupiter.api.Test;

public class ExtensionLifeCycleTest {

    @Test
    public void extensionStartIsCalled() {
        TestNotifier notifier = new TestNotifier();
        WireMockServer wm = new WireMockServer(wireMockConfig().dynamicPort().notifier(notifier).extensions(new StartStopLoggingExtension(notifier)));
        assertThat(notifier.infoMessages.size(), is(1));
        assertThat(notifier.infoMessages.get(0),containsString("Extension started"));
        wm.start();
        wm.shutdownServer();
    }

    public static class TestNotifier implements Notifier {

        final List<String> infoMessages = new ArrayList<>();

        @Override
        public void info(String message) {
            infoMessages.add(message);
        }

        @Override
        public void error(String message) {
        }

        @Override
        public void error(String message, Throwable t) {
        }
    }

    public class StartStopLoggingExtension implements Extension {
        private final Notifier notifier;

        public StartStopLoggingExtension(Notifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public String getName() {
            return "start-stop-logging";
        }

        @Override
        public void start() {
            notifier.info("Extension started");
        }

        @Override
        public void stop() {
            notifier.info("Extension stopped");
        }
    }
}
