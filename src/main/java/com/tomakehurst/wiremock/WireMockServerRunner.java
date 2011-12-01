package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.WireMockServer.DEFAULT_PORT;
import static com.tomakehurst.wiremock.http.RequestMethod.ANY;
import static java.lang.System.out;

import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.SingleRootFileSource;
import com.tomakehurst.wiremock.mapping.MappingFileWriterListener;
import com.tomakehurst.wiremock.mapping.Mappings;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;
import com.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.tomakehurst.wiremock.standalone.MappingsLoader;

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
			wireMockServer = new WireMockServer(options.portNumber(), fileSource);
		} else {
			wireMockServer = new WireMockServer(DEFAULT_PORT, fileSource);
		}
		
		if (options.recordMappingsEnabled()) {
			wireMockServer.addMockServiceRequestListener(new MappingFileWriterListener(mappingsFileSource, filesFileSource));
		}
		
		wireMockServer.setVerboseLogging(options.verboseLoggingEnabled());
		
		MappingsLoader mappingsLoader = new JsonFileMappingsLoader(mappingsFileSource);
		wireMockServer.loadMappingsUsing(mappingsLoader);
		
		if (options.specifiesProxyUrl()) {
			addProxyMapping(options.proxyUrl());
		}
		
		wireMockServer.start();
	}
	
	private void addProxyMapping(final String baseUrl) {
		wireMockServer.loadMappingsUsing(new MappingsLoader() {
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
