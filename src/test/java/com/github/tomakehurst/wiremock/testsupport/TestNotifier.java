package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;

import java.util.ArrayList;
import java.util.List;

public class TestNotifier implements Notifier {

    private List<String> info;
    private List<String> error;

    private Notifier previousNotifier;

    private final ConsoleNotifier consoleNotifier = new ConsoleNotifier(true);

    public TestNotifier() {
        this.info = new ArrayList<>();
        this.error = new ArrayList<>();
    }

    public static TestNotifier createAndSet() {
        TestNotifier testNotifier = new TestNotifier();
        testNotifier.previousNotifier = LocalNotifier.notifier();
        LocalNotifier.set(testNotifier);
        return testNotifier;
    }

    public void revert() {
        LocalNotifier.set(previousNotifier);
    }

    @Override
    public void info(String message) {
        this.info.add(message);
        consoleNotifier.info(message);
    }

    @Override
    public void error(String message) {
        this.error.add(message);
        consoleNotifier.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        this.error.add(message);
        consoleNotifier.error(message, t);
    }

    public List<String> getInfoMessages() { return info; }

    public List<String> getErrorMessages() { return error; }

    public void reset() {
        info.clear();
        error.clear();
    }
}
