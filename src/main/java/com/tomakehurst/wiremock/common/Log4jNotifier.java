package com.tomakehurst.wiremock.common;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4jNotifier implements Notifier {
	
	private static final Logger log = Logger.getLogger(Log4jNotifier.class);
	
	public Log4jNotifier() {
		setVerbose(false);
	}
	
	public void setVerbose(boolean verbose) {
		if (verbose) {
			PropertyConfigurator.configure(classPathFile("log4j-verbose.properties"));
		} else {
			PropertyConfigurator.configure(classPathFile("log4j-terse.properties"));
		}
	}
	
	private URL classPathFile(String path) {
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}

	@Override
	public void info(String message) {
		log.info(message);
		
	}

	@Override
	public void error(String message) {
		log.error(message);
		
	}

	@Override
	public void error(String message, Throwable t) {
		log.error(message, t);
	}
}
