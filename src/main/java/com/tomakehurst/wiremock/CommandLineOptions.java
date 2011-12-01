package com.tomakehurst.wiremock;

import java.io.IOException;
import java.io.StringWriter;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CommandLineOptions {
	
	private static final String HELP = "help";
	private static final String RECORD_MAPPINGS = "record-mappings";
	private static final String PROXY_ALL = "proxy-all";
	private static final String PORT = "port";
	private static final String VERBOSE = "verbose";
	
	private OptionSet optionSet;
	private String helpText;

	public CommandLineOptions(String... args) {
		OptionParser optionParser = new OptionParser();
		optionParser.accepts(PORT, "The port number for the server to listen on").withRequiredArg();
		optionParser.accepts(PROXY_ALL, "Will create a proxy mapping for /* to the specified URL").withRequiredArg();
		optionParser.accepts(RECORD_MAPPINGS, "Enable recording of all (non-admin) requests as mapping files");
		optionParser.accepts(VERBOSE, "Enable verbose logging to stdout");
		optionParser.accepts(HELP, "Print this message");
		
		optionSet = optionParser.parse(args);
		captureHelpTextIfRequested(optionParser);
	}

	private void captureHelpTextIfRequested(OptionParser optionParser) {
		if (optionSet.has(HELP)) {
			StringWriter out = new StringWriter();
			try {
				optionParser.printHelpOn(out);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			helpText = out.toString();
		}
	}
	
	public boolean verboseLoggingEnabled() {
		return optionSet.has(VERBOSE);
	}
	
	public boolean recordMappingsEnabled() {
		return optionSet.has(RECORD_MAPPINGS);
	}
	
	public boolean specifiesPortNumber() {
		return optionSet.has(PORT);
	}
	
	public int portNumber() {
		return Integer.parseInt((String) optionSet.valueOf(PORT));
	}
	
	public boolean help() {
		return optionSet.has(HELP);
	}
	
	public String helpText() {
		return helpText;
	}
	
	public boolean specifiesProxyUrl() {
		return optionSet.has(PROXY_ALL);
	}
	
	public String proxyUrl() {
		return (String) optionSet.valueOf(PROXY_ALL);
	}
}
