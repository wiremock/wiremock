/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.Network.findFreePort;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.io.File.separator;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit5.EnabledIfJettyVersion;
import com.github.tomakehurst.wiremock.standalone.MappingFileException;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StandaloneAcceptanceTest {
  private static final String FILES = "__files";
  private static final String MAPPINGS = "mappings";

  private static final File FILE_SOURCE_ROOT = new File("build/standalone-files");

  private WireMockServerRunner runner;
  private WireMockTestClient testClient;

  private WireMockServer otherServer;

  private final PrintStream stdOut = System.out;
  private ByteArrayOutputStream out;
  private final PrintStream stdErr = System.err;
  private ByteArrayOutputStream err;

  private File mappingsDirectory;
  private File filesDirectory;

  @BeforeEach
  public void init() throws Exception {
    if (FILE_SOURCE_ROOT.exists()) {
      try (Stream<Path> pathStream = Files.walk(FILE_SOURCE_ROOT.toPath().toAbsolutePath())) {
        pathStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      }
    }
    FILE_SOURCE_ROOT.mkdirs();

    mappingsDirectory = new File(FILE_SOURCE_ROOT, MAPPINGS);
    filesDirectory = new File(FILE_SOURCE_ROOT, FILES);

    runner = new WireMockServerRunner();

    WireMock.configure();
  }

  @AfterEach
  public void stopServerRunner() {
    runner.stop();
    if (otherServer != null) {
      otherServer.stop();
    }
    System.setOut(stdOut);
    System.setErr(stdErr);
  }

  @Test
  void acceptsMappingRequestOnDefaultPort() {
    startRunner();
    givenThat(
        get(urlEqualTo("/standalone/test/resource"))
            .willReturn(aResponse().withStatus(200).withBody("Content")));
    assertThat(testClient.get("/standalone/test/resource").content(), is("Content"));
  }

  private static final String MAPPING_REQUEST =
      "{ 													\n"
          + "	\"request\": {									\n"
          + "		\"method\": \"GET\",						\n"
          + "		\"url\": \"/resource/from/file\"			\n"
          + "	},												\n"
          + "	\"response\": {									\n"
          + "		\"status\": 200,							\n"
          + "		\"body\": \"Body from mapping file\"		\n"
          + "	}												\n"
          + "}													";

  @Test
  void readsMappingFromMappingsDir() {
    writeMappingFile("test-mapping-1.json", MAPPING_REQUEST);
    startRunner();
    assertThat(testClient.get("/resource/from/file").content(), is("Body from mapping file"));
  }

  @Test
  void readsMappingFromSpecifiedRecordingsPath() {
    String differentRoot = FILE_SOURCE_ROOT + separator + "differentRoot";
    writeFile(differentRoot + separator + underMappings("test-mapping-1.json"), MAPPING_REQUEST);
    startRunner("--root-dir", differentRoot);
    assertThat(testClient.get("/resource/from/file").content(), is("Body from mapping file"));
  }

  @Test
  void servesFileFromFilesDir() {
    writeFileToFilesDir("test-1.xml", "<content>Blah</content>");
    startRunner();
    WireMockResponse response = testClient.get("/test-1.xml");
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("<content>Blah</content>"));
    assertThat(response.firstHeader("Content-Type"), is("application/xml"));
  }

  @Test
  void servesFileFromSpecifiedRecordingsPath() {
    String differentRoot = FILE_SOURCE_ROOT + separator + "differentRoot";
    writeFile(differentRoot + separator + underFiles("test-1.xml"), "<content>Blah</content>");
    startRunner("--root-dir", differentRoot);
    WireMockResponse response = testClient.get("/test-1.xml");
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("<content>Blah</content>"));
    assertThat(response.firstHeader("Content-Type"), is("application/xml"));
  }

  @Test
  void servesFileAsJsonWhenNoFileExtension() {
    writeFileToFilesDir("json/12345", "{ \"key\": \"value\" }");
    startRunner();
    WireMockResponse response = testClient.get("/json/12345");
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("{ \"key\": \"value\" }"));
    // The "Content-Type" header may include charset, fe "application/json;charset=utf-8"
    assertThat(response.firstHeader("Content-Type"), startsWith("application/json"));
  }

  @Test
  void servesTheSwaggerUi() {
    startRunner();

    WireMockResponse response = testClient.get("/__admin/swagger-ui/");
    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), startsWith("text/html"));
    assertThat(
        response.content(), containsString("<title>WireMock Admin API | Swagger UI</title>"));

    response = testClient.get("/__admin/swagger-ui/swagger-ui-dist/swagger-ui-bundle.js");
    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), startsWith("text/javascript"));
  }

  @Test
  void servesTheRecordingUi() {
    startRunner();

    WireMockResponse response = testClient.get("/__admin/recorder/");
    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), startsWith("text/html"));
    assertThat(response.content(), containsString("<title>WireMock Recorder</title>"));

    response = testClient.get("/__admin/recorder/lib/jquery-3.6.0.min.js");
    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), startsWith("text/javascript"));
  }

  @Test
  void servesTheAdminApi() {
    startRunner();

    WireMockResponse response = testClient.get("/__admin/mappings");
    assertThat(response.statusCode(), is(200));
    assertThat(response.firstHeader("Content-Type"), startsWith("application/json"));
    assertThat(response.content(), containsString("mappings"));
  }

  @EnabledIfJettyVersion(
      major = 11,
      reason =
          "Jetty 12 and above always redirects when folder (without trailing slash) is accessed")
  @Test
  void shouldNotSend302WhenPathIsDirAndTrailingSlashNotPresent() {
    writeFileToFilesDir(
        "json/wire & mock directory/index.json", "{ \"key\": \"index page value\" }");
    startRunner();
    WireMockResponse response = testClient.get("/json/wire%20&%20mock%20directory");
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("{ \"key\": \"index page value\" }"));
  }

  @EnabledIfJettyVersion(
      major = 12,
      reason =
          "Jetty 12 and above always redirects when folder (without trailing slash) is accessed")
  @Test
  public void shouldSend302WhenPathIsDirAndTrailingSlashNotPresent() {
    writeFileToFilesDir(
        "json/wire & mock directory/index.json", "{ \"key\": \"index page value\" }");
    startRunner();
    WireMockResponse response = testClient.get("/json/wire%20&%20mock%20directory");
    assertThat(response.statusCode(), is(302));
  }

  @Test
  void servesJsonIndexFileWhenTrailingSlashPresent() {
    writeFileToFilesDir("json/23456/index.json", "{ \"key\": \"new value\" }");
    startRunner();
    WireMockResponse response = testClient.get("/json/23456/");
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("{ \"key\": \"new value\" }"));
    // The "Content-Type" header may include charset, fe "application/json;charset=utf-8"
    assertThat(response.firstHeader("Content-Type"), startsWith("application/json"));
  }

  @Test
  void servesXmlIndexFileWhenTrailingSlashPresent() {
    writeFileToFilesDir("json/34567/index.xml", "<blob>BLAB</blob>");
    startRunner();
    WireMockResponse response = testClient.get("/json/34567/");
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("<blob>BLAB</blob>"));
    assertThat(response.firstHeader("Content-Type"), is("application/xml;charset=utf-8"));
  }

  @Test
  void doesNotServeFileFromFilesDirWhenNotGET() {
    writeFileToFilesDir("json/should-not-see-this.json", "{}");
    startRunner();
    WireMockResponse response = testClient.put("/json/should-not-see-this.json");
    assertThat(
        response.statusCode(), is(404)); // Default servlet returns 405 if PUT is forwarded to it
  }

  private static final String BODY_FILE_MAPPING_REQUEST =
      "{ 													\n"
          + "	\"request\": {									\n"
          + "		\"method\": \"GET\",						\n"
          + "		\"url\": \"/body/file\"						\n"
          + "	},												\n"
          + "	\"response\": {									\n"
          + "		\"status\": 200,							\n"
          + "		\"bodyFileName\": \"body-test.xml\"			\n"
          + "	}												\n"
          + "}													";

  @Test
  public void readsBodyFileFromFilesDir() {
    writeMappingFile("test-mapping-2.json", BODY_FILE_MAPPING_REQUEST);
    writeFileToFilesDir("body-test.xml", "<body>Content</body>");
    startRunner();
    assertThat(testClient.get("/body/file").content(), is("<body>Content</body>"));
  }

  @Test
  public void readsBinaryBodyFileFromFilesDir() {
    writeMappingFile("test-mapping-2.json", BODY_FILE_MAPPING_REQUEST);
    writeFileToFilesDir("body-test.xml", MappingJsonSamples.BINARY_COMPRESSED_CONTENT);
    startRunner();
    byte[] returnedContent = testClient.get("/body/file").binaryContent();
    assertThat(returnedContent, is(MappingJsonSamples.BINARY_COMPRESSED_CONTENT));
    assertThat(
        decompress(returnedContent), is(MappingJsonSamples.BINARY_COMPRESSED_CONTENT_AS_STRING));
  }

  @Test
  public void logsVerboselyWhenVerboseSetInCommandLine() {
    startRecordingSystemOutAndErr();
    startRunner("--verbose");
    assertThat(systemOutText(), containsString("Verbose logging enabled"));
  }

  @Test
  public void doesNotLogVerboselyWhenVerboseNotSetInCommandLine() {
    startRecordingSystemOutAndErr();
    startRunner();
    assertThat(systemOutText(), not(containsString("Verbose logging enabled")));
  }

  @Test
  public void startsOnPortSpecifiedOnCommandLine() {
    int port = findFreePort();
    startRunner("--port", "" + port);
    WireMock client = WireMock.create().host("localhost").port(port).build();
    client.verifyThat(
        0,
        getRequestedFor(
            urlEqualTo("/bling/blang/blong"))); // Would throw an exception if couldn't connect
  }

  @Test
  void proxiesToHostSpecifiedOnCommandLine() {
    WireMock otherServerClient = startOtherServerAndClient();
    otherServerClient.register(
        get(urlEqualTo("/proxy/ok?working=yes")).willReturn(aResponse().withStatus(HTTP_OK)));
    startRunner("--proxy-all", "http://localhost:" + otherServer.port());

    WireMockResponse response = testClient.get("/proxy/ok?working=yes");
    assertThat(response.statusCode(), is(HTTP_OK));
  }

  @Test
  void respondsWithPreExistingRecordingInProxyMode() {
    writeMappingFile("test-mapping-2.json", BODY_FILE_MAPPING_REQUEST);
    writeFileToFilesDir("body-test.xml", "Existing recorded body");

    WireMock otherServerClient = startOtherServerAndClient();
    otherServerClient.register(
        get(urlEqualTo("/body/file"))
            .willReturn(aResponse().withStatus(HTTP_OK).withBody("Proxied body")));

    startRunner("--proxy-all", "http://localhost:" + otherServer.port());

    assertThat(testClient.get("/body/file").content(), is("Existing recorded body"));
  }

  @Test
  void recordsProxiedRequestsWhenSpecifiedOnCommandLineViaLegacyRecorder() throws Exception {
    WireMock otherServerClient = startOtherServerAndClient();
    startRunner("--record-mappings");
    givenThat(
        get(urlEqualTo("/please/record-this"))
            .willReturn(aResponse().proxiedFrom("http://localhost:" + otherServer.port())));
    otherServerClient.register(
        get(urlEqualTo("/please/record-this"))
            .willReturn(aResponse().withStatus(HTTP_OK).withBody("Proxied body")));

    testClient.get("/please/record-this");

    assertThat(mappingsDirectory, containsAFileContaining("/please/record-this"));
    assertThat(
        contentsOfFirstFileNamedLike("please-record-this"),
        containsString("bodyFileName\" : \"body-please-record-this"));
  }

  @Test
  void recordsRequestHeadersWhenSpecifiedOnCommandLineViaLegacyRecorder() throws Exception {
    WireMock otherServerClient = startOtherServerAndClient();
    startRunner("--record-mappings", "--match-headers", "Accept");
    givenThat(
        get(urlEqualTo("/please/record-headers"))
            .willReturn(aResponse().proxiedFrom("http://localhost:" + otherServer.port())));
    otherServerClient.register(
        get(urlEqualTo("/please/record-headers"))
            .willReturn(aResponse().withStatus(HTTP_OK).withBody("Proxied body")));

    testClient.get("/please/record-headers", withHeader("accept", "application/json"));

    assertThat(mappingsDirectory, containsAFileContaining("/please/record-headers"));
    assertThat(
        contentsOfFirstFileNamedLike("please-record-headers"), containsString("\"Accept\" : {"));
  }

  @Test
  void recordsGzippedResponseBodiesDecompressedViaLegacyRecorder() {
    WireMock otherServerClient = startOtherServerAndClient();
    startRunner("--record-mappings");
    givenThat(
        get(urlEqualTo("/record-zip"))
            .willReturn(aResponse().proxiedFrom("http://localhost:" + otherServer.port())));
    otherServerClient.register(
        get(urlEqualTo("/record-zip"))
            .willReturn(aResponse().withStatus(HTTP_OK).withBody("gzipped body")));

    testClient.get("/record-zip", withHeader("Accept-Encoding", "gzip,deflate"));

    assertThat(mappingsDirectory, containsAFileContaining("/record-zip"));
    assertThat(filesDirectory, containsAFileContaining("gzipped body"));
  }

  @Test
  void recordsGzippedResponseBodiesDecompressedViaNewRecorder(@TempDir Path tempFileRoot) {
    WireMock target = startOtherServerAndClient();

    startRunner("--root-dir", tempFileRoot.toString());

    target.register(get("/record-this").willReturn(ok("Recorded")));

    startRecording(otherServer.baseUrl());
    testClient.get("/record-this");
    stopRecording();

    assertThat(
        Arrays.stream(requireNonNull(tempFileRoot.resolve("mappings").toFile().list()))
            .filter(name -> name.contains("record-this"))
            .findFirst()
            .orElse(""),
        endsWith(".json"));
  }

  @Test
  void matchesVeryLongHeader() {
    startRunner("--jetty-header-buffer-size", "32678");

    String veryLongHeader = padRight("", 16336).replace(' ', 'h');
    givenThat(
        get(urlEqualTo("/some/big/header"))
            .withHeader("ExpectedHeader", equalTo(veryLongHeader))
            .willReturn(aResponse().withStatus(200)));

    WireMockResponse response =
        testClient.get("/some/big/header", withHeader("ExpectedHeader", veryLongHeader));

    assertThat(response.statusCode(), is(200));
  }

  @Test
  void performsBrowserProxyingWhenEnabled() {
    WireMock otherServerClient = startOtherServerAndClient();
    startRunner("--enable-browser-proxying");
    otherServerClient.register(
        get(urlEqualTo("/from/browser/proxy"))
            .willReturn(aResponse().withStatus(HTTP_OK).withBody("Proxied body")));

    assertThat(
        testClient
            .getViaProxy("http://localhost:" + otherServer.port() + "/from/browser/proxy")
            .content(),
        is("Proxied body"));
  }

  @Test
  void doesNotRecordRequestWhenNotProxied() {
    startRunner("--record-mappings");
    testClient.get("/try-to/record-this");
    assertThat(mappingsDirectory, doesNotContainAFileWithNameContaining("try-to-record"));
  }

  @Test
  void doesNotRecordRequestWhenAlreadySeen() {
    WireMock otherServerClient = startOtherServerAndClient();
    startRunner("--record-mappings");
    givenThat(
        get(urlEqualTo("/try-to/record-this"))
            .willReturn(aResponse().proxiedFrom("http://localhost:" + otherServer.port())));
    otherServerClient.register(
        get(urlEqualTo("/try-to/record-this"))
            .willReturn(aResponse().withStatus(HTTP_OK).withBody("Proxied body")));

    testClient.get("/try-to/record-this");
    testClient.get("/try-to/record-this");

    assertThat(mappingsDirectory, containsExactlyOneFileWithNameContaining("try-to-record"));
  }

  @Test
  void canBeShutDownRemotely() {
    startRunner();

    WireMock.shutdownServer();

    // Keep trying the server until it shuts down.
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < 5000) {
      if (!runner.isRunning()) {
        return;
      }
    }
    fail("WireMock did not shut down");
  }

  @Test
  void canBeShutDownRemotelyWhenAsyncResponsesEnabled() {
    startRunner("--async-response-enabled");

    stubFor(get("/delay-this").willReturn(ok().withFixedDelay(50)));
    testClient.get("/delay-this");
    testClient.get("/delay-this");
    testClient.get("/delay-this");

    WireMock.shutdownServer();

    // Keep trying the server until it shuts down.
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < 5000) {
      if (!runner.isRunning()) {
        return;
      }
    }
    fail("WireMock did not shut down");
  }

  @Test
  void isRunningReturnsFalseBeforeRunMethodIsExecuted() {
    runner = new WireMockServerRunner();
    assertThat(runner.isRunning(), is(false));
  }

  private static final String BAD_MAPPING =
      "{ 													\n"
          + "	\"requesttttt\": {      						\n"
          + "		\"method\": \"GET\",						\n"
          + "		\"url\": \"/resource/from/file\"			\n"
          + "	},												\n"
          + "	\"response\": {									\n"
          + "		\"status\": 200,							\n"
          + "		\"body\": \"Body from mapping file\"		\n"
          + "	}												\n"
          + "}													";

  @Test
  void failsWithUsefulErrorMessageWhenMappingFileIsInvalid() {
    writeMappingFile("bad-mapping.json", BAD_MAPPING);

    MappingFileException exception = assertThrows(MappingFileException.class, this::startRunner);
    assertThat(
        exception.getMessage(),
        allOf(
            containsString("Error loading file"),
            containsString("bad-mapping.json"),
            containsString("Unrecognized field \"requesttttt\""),
            containsString("class com.github.tomakehurst.wiremock.stubbing.StubMapping"),
            containsString("not marked as ignorable")));
  }

  @Test
  void savesMappingFileOnCreationOfPersistentStub() {
    startRunner();

    stubFor(
        get(urlPathEqualTo("/one/two/three"))
            .withName("Named stuff here __$$ things!")
            .persistent()
            .willReturn(ok()));

    assertThat(mappingsDirectory, containsExactlyOneFileWithNameContaining("named-stuff-here"));
  }

  private String contentsOfFirstFileNamedLike(String namePart) throws IOException {
    return Files.readString(
        requireNonNull(firstFileWithNameLike(mappingsDirectory, namePart)).toPath());
  }

  private File firstFileWithNameLike(File directory, String namePart) {
    for (File file : requireNonNull(directory.listFiles(namedLike(namePart)))) {
      return file;
    }

    fail(
        String.format(
            "Couldn't find a file under %s named like %s", directory.getPath(), namePart));
    return null;
  }

  private FilenameFilter namedLike(final String namePart) {
    return (file, name) -> name.contains(namePart);
  }

  private WireMock startOtherServerAndClient() {
    otherServer = new WireMockServer(Options.DYNAMIC_PORT);
    otherServer.start();
    return WireMock.create().port(otherServer.port()).build();
  }

  private void writeFileToFilesDir(String name, String contents) {
    writeFile(underFileSourceRoot(underFiles(name)), contents);
  }

  private void writeFileToFilesDir(String name, byte[] contents) {
    try {
      String filePath = underFileSourceRoot(underFiles(name));
      Path file = Paths.get(filePath);
      createDirectories(file.getParent());
      write(file, contents);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeMappingFile(String name, String contents) {
    writeFile(underFileSourceRoot(underMappings(name)), contents);
  }

  private void writeFile(String absolutePath, String contents) {
    try {
      Path file = Paths.get(absolutePath);
      createDirectories(file.getParent());
      write(file, contents.getBytes(UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String underFiles(String name) {
    return FILES + separator + name;
  }

  private String underMappings(String name) {
    return MAPPINGS + separator + name;
  }

  private String underFileSourceRoot(String relativePath) {
    return FILE_SOURCE_ROOT + separator + relativePath;
  }

  private void startRunner(String... args) {
    runner = new WireMockServerRunner();
    runner.run(argsWithPort(argsWithRecordingsPath(args)));

    int port = runner.port();
    testClient = new WireMockTestClient(port);
    WireMock.configureFor(port);
  }

  private String[] argsWithRecordingsPath(String[] args) {
    List<String> argsAsList = new ArrayList<>(asList(args));
    if (!argsAsList.contains("--root-dir")) {
      argsAsList.addAll(asList("--root-dir", FILE_SOURCE_ROOT.getPath()));
    }
    return argsAsList.toArray(new String[] {});
  }

  private String[] argsWithPort(String[] args) {
    List<String> argsAsList = new ArrayList<>(asList(args));
    if (!argsAsList.contains("--port")) {
      argsAsList.addAll(asList("--port", "" + Options.DYNAMIC_PORT));
    }
    return argsAsList.toArray(new String[] {});
  }

  private void startRecordingSystemOutAndErr() {
    out = new ByteArrayOutputStream();
    err = new ByteArrayOutputStream();

    System.setOut(new PrintStream(out));
    System.setErr(new PrintStream(err));
  }

  private String systemOutText() {
    return out.toString();
  }

  private String systemErrText() {
    return err.toString();
  }

  private Matcher<File> containsAFileContaining(final String expectedContents) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("a file containing " + expectedContents);
      }

      @Override
      public boolean matchesSafely(File dir) {
        for (File file : requireNonNull(dir.listFiles())) {
          try {
            if (Files.readString(file.toPath()).contains(expectedContents)) {
              return true;
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        return false;
      }
    };
  }

  private Matcher<File> doesNotContainAFileWithNameContaining(final String namePart) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("no file named like " + namePart);
      }

      @Override
      public boolean matchesSafely(File dir) {
        return Arrays.stream(requireNonNull(dir.list())).noneMatch(contains(namePart));
      }
    };
  }

  private Matcher<File> containsExactlyOneFileWithNameContaining(final String namePart) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("exactly one file named like " + namePart);
      }

      @Override
      public boolean matchesSafely(File dir) {
        return (int) Arrays.stream(requireNonNull(dir.list())).filter(contains(namePart)).count()
            == 1;
      }
    };
  }

  private static Predicate<String> contains(final String part) {
    return s -> s.contains(part);
  }

  /**
   * Decompress the binary gzipped content into a String.
   *
   * @param content the gzipped content to decompress
   * @return decompressed String.
   */
  private String decompress(byte[] content) {
    GZIPInputStream gin = null;
    try {
      gin = new GZIPInputStream(new ByteArrayInputStream(content));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      byte[] buf = new byte[8192];

      int read;
      while ((read = gin.read(buf)) != -1) {
        baos.write(buf, 0, read);
      }

      return baos.toString(UTF_8);

    } catch (IOException e) {
      return null;
    } finally {
      if (gin != null)
        try {
          gin.close();
        } catch (IOException ignored) {
          // ignored
        }
    }
  }

  public static String padRight(String s, int paddingLength) {
    return String.format("%1$-" + paddingLength + "s", s);
  }
}
