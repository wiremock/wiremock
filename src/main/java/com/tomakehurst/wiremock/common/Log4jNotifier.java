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
package com.tomakehurst.wiremock.common;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4jNotifier implements Notifier {
	
	private static final Logger log = Logger.getLogger(Log4jNotifier.class);
	
	public Log4jNotifier() {
		setVerbose(false);
	}
	
	public void setVerbose(final boolean verbose) {
		if (verbose) {
			PropertyConfigurator.configure(classPathFile("log4j-verbose.properties"));
		} else {
			PropertyConfigurator.configure(classPathFile("log4j-terse.properties"));
		}
	}
	
	private URL classPathFile(final String path) {
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}

	@Override
	public void info(final String message) {
		log.info(message);
		
	}

	@Override
	public void error(final String message) {
		log.error(message);
		
	}

	@Override
	public void error(final String message, final Throwable t) {
		log.error(message, t);
	}

    @Override
    public void error(final Throwable t) {
        log.error(t);
    }
}
