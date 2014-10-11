package com.github.tomakehurst.wiremock.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.System.err;
import static java.lang.System.out;

public class ConsoleNotifier implements Notifier {

    private final boolean verbose;

    public ConsoleNotifier(boolean verbose) {
        this.verbose = verbose;
        if (verbose) {
            info("Verbose logging enabled");
        }
    }

    @Override
    public void info(String message) {
        if (verbose) {
            out.println(formatMessage(message));
        }
    }

    @Override
    public void error(String message) {
        err.println(formatMessage(message));
    }

    @Override
    public void error(String message, Throwable t) {
        err.println(formatMessage(message));
        t.printStackTrace(err);
    }

    private static String formatMessage(String message) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String date = df.format(new Date());
        return String.format("%s %s", date, message);
    }
}
