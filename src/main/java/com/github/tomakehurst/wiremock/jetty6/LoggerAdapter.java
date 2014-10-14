/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
