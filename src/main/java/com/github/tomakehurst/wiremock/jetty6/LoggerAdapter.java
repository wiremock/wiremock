package com.github.tomakehurst.wiremock.jetty6;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import org.mortbay.log.Logger;

public class LoggerAdapter implements Logger {

    private final Notifier notifier;

    public LoggerAdapter() {
        this(new ConsoleNotifier(false));
    }

    public LoggerAdapter(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        // Nothing
    }

    @Override
    public void info(String msg, Object arg0, Object arg1) {
        // Nothing
    }

    @Override
    public void debug(String msg, Throwable th) {
        // Nothing
    }

    @Override
    public void debug(String msg, Object arg0, Object arg1) {
        // Nothing
    }

    @Override
    public void warn(String msg, Object arg0, Object arg1) {
        notifier.error(msg);
    }

    @Override
    public void warn(String msg, Throwable th) {
        notifier.error(msg, th);
    }

    @Override
    public Logger getLogger(String name) {
        return new LoggerAdapter(notifier);
    }
}
