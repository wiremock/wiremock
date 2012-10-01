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

import static com.github.tomakehurst.wiremock.WireMockServer.DEFAULT_PORT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static java.lang.System.out;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.mapping.Mappings;
import com.github.tomakehurst.wiremock.mapping.RequestPattern;
import com.github.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.github.tomakehurst.wiremock.mapping.ResponseDefinition;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;

public class WireMockServerRunner {
	
	public static final String FILES_ROOT = "__files";
	public static final String MAPPINGS_ROOT = "mappings";
	
	private WireMockServer wireMockServer;
	
	public void run(String fileSourcesRoot, String... args) {
		CommandLineOptions options = new CommandLineOptions(args);
		if (options.help()) {
			out.println(options.helpText());
			return;
		}
		
		FileSource fileSource = new SingleRootFileSource(fileSourcesRoot);
		fileSource.createIfNecessary();
		FileSource filesFileSource = fileSource.child(FILES_ROOT);
		filesFileSource.createIfNecessary();
		FileSource mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
		mappingsFileSource.createIfNecessary();
		
		if (options.specifiesPortNumber()) {
			wireMockServer = new WireMockServer(options.portNumber(), fileSource, options.browserProxyingEnabled());
		} else {
			wireMockServer = new WireMockServer(DEFAULT_PORT, fileSource, options.browserProxyingEnabled());
		}
		
		if (options.recordMappingsEnabled()) {
		    wireMockServer.enableRecordMappings(mappingsFileSource, filesFileSource);
		}
		
		wireMockServer.setVerboseLogging(options.verboseLoggingEnabled());
		
		wireMockServer.loadMappingsUsing(new JsonFileMappingsLoader(mappingsFileSource));
		
		if (options.specifiesProxyUrl()) {
			addProxyMapping(options.proxyUrl());
		}
		
		wireMockServer.start();
	}
	
	private void addProxyMapping(final String baseUrl) {
		wireMockServer.loadMappingsUsing(new MappingsLoader() {
			@Override
			public void loadMappingsInto(Mappings mappings) {
				RequestPattern requestPattern = new RequestPattern(ANY);
				requestPattern.setUrlPattern(".*");
				ResponseDefinition responseDef = new ResponseDefinition();
				responseDef.setProxyBaseUrl(baseUrl);
				mappings.addMapping(new RequestResponseMapping(requestPattern, responseDef));
			}
		});
	}
	
	public void stop() {
		wireMockServer.stop();
	}

	public static void main(String... args) {
		new WireMockServerRunner().run(".", args);
	}
}
