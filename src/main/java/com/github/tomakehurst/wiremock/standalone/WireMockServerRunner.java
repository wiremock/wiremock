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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;

import static com.github.tomakehurst.wiremock.WireMockServer.FILES_ROOT;
import static com.github.tomakehurst.wiremock.WireMockServer.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static java.lang.System.out;

public class WireMockServerRunner {

    private static final String BANNER = " /$$      /$$ /$$                     /$$      /$$                     /$$      \n" +
            "| $$  /$ | $$|__/                    | $$$    /$$$                    | $$      \n" +
            "| $$ /$$$| $$ /$$  /$$$$$$   /$$$$$$ | $$$$  /$$$$  /$$$$$$   /$$$$$$$| $$   /$$\n" +
            "| $$/$$ $$ $$| $$ /$$__  $$ /$$__  $$| $$ $$/$$ $$ /$$__  $$ /$$_____/| $$  /$$/\n" +
            "| $$$$_  $$$$| $$| $$  \\__/| $$$$$$$$| $$  $$$| $$| $$  \\ $$| $$      | $$$$$$/ \n" +
            "| $$$/ \\  $$$| $$| $$      | $$_____/| $$\\  $ | $$| $$  | $$| $$      | $$_  $$ \n" +
            "| $$/   \\  $$| $$| $$      |  $$$$$$$| $$ \\/  | $$|  $$$$$$/|  $$$$$$$| $$ \\  $$\n" +
            "|__/     \\__/|__/|__/       \\_______/|__/     |__/ \\______/  \\_______/|__/  \\__/";

    static {
        System.setProperty("org.mortbay.log.class", "com.github.tomakehurst.wiremock.jetty.LoggerAdapter");
    }

	private WireMockServer wireMockServer;
	
	public void run(String... args) {
		CommandLineOptions options = new CommandLineOptions(args);
		if (options.help()) {
			out.println(options.helpText());
			return;
		}

		FileSource fileSource = options.filesRoot();
		fileSource.createIfNecessary();
		FileSource filesFileSource = fileSource.child(FILES_ROOT);
		filesFileSource.createIfNecessary();
		FileSource mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
		mappingsFileSource.createIfNecessary();

        wireMockServer = new WireMockServer(options);

        if (options.recordMappingsEnabled()) {
            wireMockServer.enableRecordMappings(mappingsFileSource, filesFileSource);
        }

		if (options.specifiesProxyUrl()) {
			addProxyMapping(options.proxyUrl());
		}

        try {
            wireMockServer.start();
            out.println(BANNER);
            out.println();
            out.println(options);
        } catch (FatalStartupException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
	
	private void addProxyMapping(final String baseUrl) {
		wireMockServer.loadMappingsUsing(new MappingsLoader() {
			@Override
			public void loadMappingsInto(StubMappings stubMappings) {
				RequestPattern requestPattern = new RequestPattern(ANY);
				requestPattern.setUrlPattern(".*");
				ResponseDefinition responseDef = new ResponseDefinition();
				responseDef.setProxyBaseUrl(baseUrl);

				StubMapping proxyBasedMapping = new StubMapping(requestPattern, responseDef);
				proxyBasedMapping.setPriority(10); // Make it low priority so that existing stubs will take precedence
				stubMappings.addMapping(proxyBasedMapping);
			}
		});
	}
	
	public void stop() {
		wireMockServer.stop();
	}

    public boolean isRunning() {
        return wireMockServer.isRunning();
    }

    public int port() { return wireMockServer.port(); }

	public static void main(String... args) {
		new WireMockServerRunner().run(args);
	}
}
