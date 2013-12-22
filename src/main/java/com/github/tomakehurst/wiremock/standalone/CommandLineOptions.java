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
package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.stubbing.StubMappingJsonRecorder;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.io.StringWriter;

public class CommandLineOptions implements Options {
	
	private static final String HELP = "help";
	private static final String RECORD_MAPPINGS = "record-mappings";
	private static final String UNGZIP_RECORDED_RESPONSES = "ungzip-recorded-responses";
	private static final String PROXY_ALL = "proxy-all";
    private static final String PROXY_VIA = "proxy-via";
	private static final String PORT = "port";
    private static final String HTTPS_PORT = "https-port";
    private static final String HTTPS_KEYSTORE = "https-keystore";
	private static final String VERBOSE = "verbose";
	private static final String ENABLE_BROWSER_PROXYING = "enable-browser-proxying";
    private static final String DISABLE_REQUEST_JOURNAL = "no-request-journal";

    private final FileSource fileSource;
	private final OptionSet optionSet;
	private String helpText;

    public CommandLineOptions(FileSource fileSource, String... args) {
        this.fileSource = fileSource;

		OptionParser optionParser = new OptionParser();
		optionParser.accepts(PORT, "The port number for the server to listen on").withRequiredArg();
        optionParser.accepts(HTTPS_PORT, "If this option is present WireMock will enable HTTPS on the specified port").withRequiredArg();
        optionParser.accepts(HTTPS_KEYSTORE, "Path to an alternative keystore for HTTPS. Must have a password of \"password\".").withRequiredArg();
		optionParser.accepts(PROXY_ALL, "Will create a proxy mapping for /* to the specified URL").withRequiredArg();
        optionParser.accepts(PROXY_VIA, "Specifies a proxy server to use when routing proxy mapped requests").withRequiredArg();
		optionParser.accepts(RECORD_MAPPINGS, "Enable recording of all (non-admin) requests as mapping files");
		optionParser.accepts(UNGZIP_RECORDED_RESPONSES, "Decompresses all gzip responses when recording");
		optionParser.accepts(VERBOSE, "Enable verbose logging to stdout");
		optionParser.accepts(ENABLE_BROWSER_PROXYING, "Allow wiremock to be set as a browser's proxy server");
        optionParser.accepts(DISABLE_REQUEST_JOURNAL, "Disable the request journal (to avoid heap growth when running wiremock for long periods without reset)");
		optionParser.accepts(HELP, "Print this message");
		
		optionSet = optionParser.parse(args);
        validate();
		captureHelpTextIfRequested(optionParser);
	}

    private void validate() {
        if (optionSet.has(HTTPS_KEYSTORE) && !optionSet.has(HTTPS_PORT)) {
            throw new IllegalArgumentException("HTTPS port number must be specified if specifying the keystore path");
        }

        if (optionSet.has(RECORD_MAPPINGS) && optionSet.has(DISABLE_REQUEST_JOURNAL)) {
            throw new IllegalArgumentException("Request journal must be enabled to record stubs");
        }
    }

    public CommandLineOptions(String... args) {
        this(new SingleRootFileSource("."), args);
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

    public StubMappingJsonRecorder.DecompressionMode decompressionMode() {
        if (optionSet.has(UNGZIP_RECORDED_RESPONSES)) {
            return StubMappingJsonRecorder.DecompressionMode.DECOMPRESS_GZIP;
        }
        return StubMappingJsonRecorder.DecompressionMode.NO_DECOMPRESSION;
    }
	
	private boolean specifiesPortNumber() {
		return optionSet.has(PORT);
	}
	
	@Override
    public int portNumber() {
        if (specifiesPortNumber()) {
            return Integer.parseInt((String) optionSet.valueOf(PORT));
        }

        return DEFAULT_PORT;
	}

    @Override
    public HttpsSettings httpsSettings() {
        if (!optionSet.has(HTTPS_PORT)) {
            return HttpsSettings.NO_HTTPS;
        }

        if (optionSet.has(HTTPS_KEYSTORE)) {
            return new HttpsSettings(httpsPortNumber(), (String) optionSet.valueOf(HTTPS_KEYSTORE));
        }

        return new HttpsSettings(httpsPortNumber());
    }

    private int httpsPortNumber() {
        return Integer.parseInt((String) optionSet.valueOf(HTTPS_PORT));
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
	
	@Override
    public boolean browserProxyingEnabled() {
		return optionSet.has(ENABLE_BROWSER_PROXYING);
	}

    @Override
    public ProxySettings proxyVia() {
        if (optionSet.has(PROXY_VIA)) {
            String proxyVia = (String) optionSet.valueOf(PROXY_VIA);
            return ProxySettings.fromString(proxyVia);
        }

        return ProxySettings.NO_PROXY;
    }

    @Override
    public FileSource filesRoot() {
        return fileSource;
    }

    @Override
    public Notifier notifier() {
        return new Log4jNotifier();
    }

    @Override
    public boolean requestJournalDisabled() {
        return optionSet.has(DISABLE_REQUEST_JOURNAL);
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("=").join(
                ImmutableMap.builder()
                        .put("port", portNumber())
                        .put("https", httpsSettings())
                        .put("fileSource", filesRoot())
                        .put("proxyVia", nullToString(proxyVia()))
                        .put("proxyUrl", nullToString(proxyUrl()))
                        .put("recordMappingsEnabled", recordMappingsEnabled())
                        .build());
    }

    private String nullToString(Object value) {
        if (value == null) {
            return "(null)";
        }

        return value.toString();
    }
}
