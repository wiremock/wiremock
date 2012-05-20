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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Log4jNotifier;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.mapping.MappingFileWriterListener;
import com.github.tomakehurst.wiremock.mapping.RequestListener;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;

public abstract class AbstractWireMockServer {

	public static final String FILES_ROOT = "__files";
	public static final int DEFAULT_PORT = 8080;
	protected static final String FILES_URL_MATCH = String.format("/%s/*", FILES_ROOT);
	
	protected final WireMockApp wireMockApp;
	
	protected final FileSource fileSource;
	protected final Log4jNotifier notifier;
	protected final int port;
	
	public AbstractWireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying) {
		notifier = new Log4jNotifier();
		this.fileSource = fileSource;
		this.port = port;
		
		wireMockApp = new WireMockApp(fileSource, notifier, enableBrowserProxying);
	}
	
	public AbstractWireMockServer(int port) {
		this(port, new SingleRootFileSource("src/test/resources"), false);
	}
	
	public AbstractWireMockServer() {
		this(DEFAULT_PORT);
	}
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		wireMockApp.loadMappingsUsing(mappingsLoader);
	}
	
	public void addMockServiceRequestListener(RequestListener listener) {
		wireMockApp.addMockServiceRequestListener(listener);
	}
	
	public void setVerboseLogging(boolean verbose) {
		notifier.setVerbose(verbose);
		if (verbose) {
		    notifier.info("Verbose logging enabled");
		}
	}
	
	public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
	    addMockServiceRequestListener(
                new MappingFileWriterListener(mappingsFileSource, filesFileSource, wireMockApp.getRequestJournal()));
	    notifier.info("Recording mappings to " + mappingsFileSource.getPath());
	}
	
	public abstract void start();
	
	public abstract void stop();
    
    
}
