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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.Log4jNotifier;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import static org.apache.log4j.Level.*;

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
