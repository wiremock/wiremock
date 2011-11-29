package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.WireMockServer.DEFAULT_PORT;

import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.SingleRootFileSource;
import com.tomakehurst.wiremock.mapping.MappingFileWriterListener;
import com.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.tomakehurst.wiremock.standalone.MappingsLoader;

public class WireMockServerRunner {
	
	public static final String FILES_ROOT = "__files";
	public static final String MAPPINGS_ROOT = "mappings";
	private static final int PORT_NUMBER_ARG = 0;
	private static final int LOG_MAPPINGS_ARG = 1;
	
	private WireMockServer wireMockServer;
	
	public void run(String fileSourcesRoot, String... args) {
		FileSource fileSource = new SingleRootFileSource(fileSourcesRoot);
		fileSource.createIfNecessary();
		FileSource filesFileSource = fileSource.child(FILES_ROOT);
		filesFileSource.createIfNecessary();
		FileSource mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
		mappingsFileSource.createIfNecessary();
		
		if (args.length > 0) {
			int port = Integer.parseInt(args[PORT_NUMBER_ARG]);
			wireMockServer = new WireMockServer(port, fileSource);
		} else {
			wireMockServer = new WireMockServer(DEFAULT_PORT, fileSource);
		}
		
		if (args.length > 1 && args[LOG_MAPPINGS_ARG].equals("--log-mappings")) {
			wireMockServer.addMockServiceRequestListener(new MappingFileWriterListener(mappingsFileSource, filesFileSource));
		}
		
		MappingsLoader mappingsLoader = new JsonFileMappingsLoader(mappingsFileSource);
		wireMockServer.loadMappingsUsing(mappingsLoader);
		wireMockServer.setVerboseLogging(false);
		wireMockServer.start();
	}
	
	public void stop() {
		wireMockServer.stop();
	}

	public static void main(String... args) {
		new WireMockServerRunner().run(".", args);
	}
}
