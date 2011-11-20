package com.tomakehurst.wiremock;

import static com.google.common.io.Files.createParentDirs;
import static com.google.common.io.Files.write;
import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.io.File.separator;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class StandaloneAcceptanceTest {
	
	private static final String FILES = "__files";
	private static final String MAPPINGS = "mappings";

	private static final File FILE_SOURCE_ROOT = new File("build/standalone-files");
	
	private WireMockServerRunner runner;
	private WireMockTestClient testClient;
	
	@Before
	public void init() {
		if (FILE_SOURCE_ROOT.exists()) {
			FILE_SOURCE_ROOT.delete();
		}
		
		FILE_SOURCE_ROOT.mkdirs();
		new File(FILE_SOURCE_ROOT, MAPPINGS).mkdir();
		new File(FILE_SOURCE_ROOT, FILES).mkdir();
		
		runner = new WireMockServerRunner();
		testClient = new WireMockTestClient();
	}
	
	@After
	public void stopServerRunner() {
		runner.stop();
	}

	@Test
	public void acceptsMappingRequestOnDefaultPort() {
		startRunnerOnDefaultPort();
		givenThat(get(urlEqualTo("/standalone/test/resource")).willReturn(aResponse().withStatus(200).withBody("Content")));
		assertThat(testClient.get("/standalone/test/resource").content(), is("Content"));
	}
	
	private static final String MAPPING_REQUEST =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/resource/from/file\"			\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 200,							\n" +
		"		\"body\": \"Body from mapping file\"		\n" +
		"	}												\n" +
		"}													";
	
	@Test
	public void readsMapppingFromMappingsDir() {
		writeMappingFile("test-mapping-1.json", MAPPING_REQUEST);
		startRunnerOnDefaultPort();
		assertThat(testClient.get("/resource/from/file").content(), is("Body from mapping file"));
	}
	
	@Test
	public void servesFileFromFilesDir() {
		writeFileToFilesDir("test-1.xml", "<content>Blah</content>");
		startRunnerOnDefaultPort();
		WireMockResponse response = testClient.get("/test-1.xml");
		assertThat(response.statusCode(), is(200));
		assertThat(response.content(), is("<content>Blah</content>"));
		assertThat(response.header("Content-Type"), is("application/xml"));
	}
	
	@Test
	public void servesFileAsJsonWhenNoFileExtension() {
		writeFileToFilesDir("json/12345", "{ \"key\": \"value\" }");
		startRunnerOnDefaultPort();
		WireMockResponse response = testClient.get("/json/12345");
		assertThat(response.statusCode(), is(200));
		assertThat(response.content(), is("{ \"key\": \"value\" }"));
		assertThat(response.header("Content-Type"), is("application/json"));
	}
	
	@Test
	public void servesJsonIndexFileWhenTrailingSlashPresent() {
		writeFileToFilesDir("json/23456/index.json", "{ \"key\": \"new value\" }");
		startRunnerOnDefaultPort();
		WireMockResponse response = testClient.get("/json/23456/");
		assertThat(response.statusCode(), is(200));
		assertThat(response.content(), is("{ \"key\": \"new value\" }"));
		assertThat(response.header("Content-Type"), is("application/json"));
	}
	
	@Test
	public void servesXmlIndexFileWhenTrailingSlashPresent() {
		writeFileToFilesDir("json/34567/index.xml", "<blob>BLAB</blob>");
		startRunnerOnDefaultPort();
		WireMockResponse response = testClient.get("/json/34567/");
		assertThat(response.statusCode(), is(200));
		assertThat(response.content(), is("<blob>BLAB</blob>"));
		assertThat(response.header("Content-Type"), is("application/xml"));
	}
	
	private static final String BODY_FILE_MAPPING_REQUEST =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/body/file\"						\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 200,							\n" +
		"		\"bodyFileName\": \"body-test.xml\"			\n" +
		"	}												\n" +
		"}													";
	
	@Test
	public void readsBodyFileFromFilesDir() {
		writeMappingFile("test-mapping-2.json", BODY_FILE_MAPPING_REQUEST);
		writeFileToFilesDir("body-test.xml", "<body>Content</body>");
		startRunnerOnDefaultPort();
		assertThat(testClient.get("/body/file").content(), is("<body>Content</body>"));
	}
	
	private void writeFileToFilesDir(String name, String contents) {
		writeFileUnderFileSourceRoot(FILES + separator + name, contents);
	}
	
	private void writeMappingFile(String name, String contents) {
		writeFileUnderFileSourceRoot(MAPPINGS + separator + name, contents);
	}
	
	private void writeFileUnderFileSourceRoot(String relativePath, String contents) {
		try {
			String filePath = FILE_SOURCE_ROOT + separator + relativePath;
			File file = new File(filePath);
			createParentDirs(file);
			write(contents, file, Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void startRunnerOnDefaultPort() {
		runner.run(FILE_SOURCE_ROOT.getPath());
	}
}
