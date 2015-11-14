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
package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Objects;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.System.err;
import static java.lang.System.out;

public final class ConsoleNotifier implements Notifier {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsoleNotifier that = (ConsoleNotifier) o;
        return Objects.equal(verbose, that.verbose);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(verbose);
    }
}
