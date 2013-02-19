package com.github.tomakehurst.wiremock;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.github.tomakehurst.wiremock.common.Log4jNotifier;

import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.INFO;
import static org.apache.log4j.Level.TRACE;

public class Log4jConfiguration {
    public static void configureLogging(boolean verbose) {
        ConsoleAppender appender = new ConsoleAppender();
        appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %m%n"));
        if (verbose) {
            appender.setThreshold(INFO);
        } else {
            appender.setThreshold(ERROR);
        }

        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);
        Logger.getRootLogger().setLevel(TRACE);
        if (verbose) {
            Logger.getLogger(Log4jNotifier.class).info("Verbose logging enabled");
        }
    }
}
