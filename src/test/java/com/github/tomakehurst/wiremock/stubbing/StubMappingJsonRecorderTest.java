/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.common.Gzip.gzip;
import static com.github.tomakehurst.wiremock.http.CaseInsensitiveKey.TO_CASE_INSENSITIVE_KEYS;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.*;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT_ORDER;

import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.MockMultipart;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

class StubMappingJsonRecorderTest {

  private StubMappingJsonRecorder listener;

  private BlobStore mappingsBlobStore;
  private BlobStore filesBlobStore;

  private Admin admin;

  @BeforeEach
  public void init() {
    mappingsBlobStore = mock(BlobStore.class, "mappingsBlobStore");
    filesBlobStore = mock(BlobStore.class, "filesBlobStore");

    admin = mock(Admin.class);

    constructRecordingListener(Collections.emptyList());
  }

  private void constructRecordingListener(List<String> headersToRecord) {
    listener =
        new StubMappingJsonRecorder(
            mappingsBlobStore,
            filesBlobStore,
            admin,
            headersToRecord.stream()
                .map(TO_CASE_INSENSITIVE_KEYS)
                .collect(Collectors.toUnmodifiableList()));
    listener.setIdGenerator(fixedIdGenerator("1$2!3"));
  }

  private static final String SAMPLE_REQUEST_MAPPING =
      "{ 													             \n"
          + "	\"request\": {									             \n"
          + "		\"method\": \"GET\",						             \n"
          + "		\"url\": \"/recorded/content\"				             \n"
          + "	},												             \n"
          + "	\"response\": {									             \n"
          + "		\"status\": 200,							             \n"
          + "		\"bodyFileName\": \"body-recorded-content-1$2!3.txt\"    \n"
          + "	}												             \n"
          + "}													               ";

  @Test
  void writesMappingFileAndCorrespondingBodyFileOnRequest() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder().withMethod(RequestMethod.GET).withUrl("/recorded/content").build();

    Response response =
        response().status(200).fromProxy(true).body("Recorded body content").build();

    listener.requestReceived(request, response);

    verify(mappingsBlobStore)
        .put(
            (eq("mapping-recorded-content-1$2!3.json")),
            argThat(equalToBinaryJson(SAMPLE_REQUEST_MAPPING, STRICT_ORDER)));
    verify(filesBlobStore)
        .put(
            (eq("body-recorded-content-1$2!3.txt")), (eq("Recorded body content".getBytes(UTF_8))));
  }

  private static final String SAMPLE_REQUEST_MAPPING_WITH_HEADERS =
      "{                                                                  \n"
          + "   \"request\": {                                                  \n"
          + "       \"method\": \"GET\",                                        \n"
          + "       \"url\": \"/headered/content\"                              \n"
          + "   },                                                              \n"
          + "   \"response\": {                                                 \n"
          + "       \"status\": 200,                                            \n"
          + "       \"bodyFileName\": \"body-headered-content-1$2!3.txt\",      \n"
          + "       \"headers\": {                                              \n"
          + "            \"Content-Type\": \"text/plain\",                      \n"
          + "            \"Cache-Control\": \"no-cache\"                        \n"
          + "       }                                                           \n"
          + "   }                                                               \n"
          + "}                                                                  ";

  @Test
  void addsResponseHeaders() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder().withMethod(RequestMethod.GET).withUrl("/headered/content").build();

    Response response =
        response()
            .status(200)
            .fromProxy(true)
            .body("Recorded body content")
            .headers(
                new HttpHeaders(
                    httpHeader("Content-Type", "text/plain"),
                    httpHeader("Cache-Control", "no-cache")))
            .build();

    listener.requestReceived(request, response);

    verify(mappingsBlobStore)
        .put(
            (eq("mapping-headered-content-1$2!3.json")),
            argThat(equalToBinaryJson(SAMPLE_REQUEST_MAPPING_WITH_HEADERS, STRICT_ORDER)));
    verify(filesBlobStore)
        .put("body-headered-content-1$2!3.txt", "Recorded body content".getBytes(UTF_8));
  }

  @Test
  void doesNotWriteFileIfRequestAlreadyReceived() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(1));

    listener.requestReceived(
        new MockRequestBuilder().withMethod(RequestMethod.GET).withUrl("/headered/content").build(),
        response().fromProxy(true).status(200).build());

    verifyNoInteractions(mappingsBlobStore);
    verifyNoInteractions(mappingsBlobStore);
  }

  @Test
  void doesNotWriteFileIfResponseNotFromProxy() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Response response = response().status(200).fromProxy(false).build();

    listener.requestReceived(
        new MockRequestBuilder().withMethod(RequestMethod.GET).withUrl("/headered/content").build(),
        response);

    verifyNoInteractions(mappingsBlobStore);
    verifyNoInteractions(filesBlobStore);
  }

  private static final String SAMPLE_REQUEST_MAPPING_WITH_BODY =
      "{ 													             \n"
          + "	\"request\": {									             \n"
          + "		\"method\": \"POST\",						             \n"
          + "		\"url\": \"/body/content\",                              \n"
          + "       \"bodyPatterns\": [                                      \n"
          + "            { \"equalTo\": \"somebody\" }                       \n"
          + "        ]				                                         \n"
          + "	},												             \n"
          + "	\"response\": {									             \n"
          + "		\"status\": 200, 							             \n"
          + "		\"bodyFileName\": \"body-body-content-1$2!3.txt\"        \n"
          + "	}												             \n"
          + "}													               ";

  @Test
  void includesBodyInRequestPatternIfInRequest() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder()
            .withMethod(POST)
            .withUrl("/body/content")
            .withHeader("Content-Type", "text/plain")
            .withBody("somebody")
            .build();

    listener.requestReceived(
        request, response().status(200).body("anything").fromProxy(true).build());

    verify(mappingsBlobStore)
        .put(
            (any(String.class)),
            argThat(equalToBinaryJson(SAMPLE_REQUEST_MAPPING_WITH_BODY, STRICT_ORDER)));
  }

  private static final String SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_1 =
      "{ 													             \n"
          + "	\"request\": {									             \n"
          + "		\"method\": \"GET\",						             \n"
          + "		\"url\": \"/same/url\",                             	 \n"
          + "       \"headers\": {                                       	 \n"
          + "			 \"Accept\":										 \n"
          + "            	{ \"equalTo\": \"text/html\" }            		 \n"
          + "        }				                                         \n"
          + "	},												             \n"
          + "	\"response\": {									             \n"
          + "		\"status\": 200,							             \n"
          + "		\"bodyFileName\": \"body-same-url-1$2!3.txt\"		 	 \n"
          + "	}												             \n"
          + "}													               ";

  private static final String SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_2 =
      "{ 													             \n"
          + "	\"request\": {									             \n"
          + "		\"method\": \"GET\",						             \n"
          + "		\"url\": \"/same/url\",                             	 \n"
          + "       \"headers\": {                                       	 \n"
          + "			 \"Accept\":										 \n"
          + "            	{ \"equalTo\": \"application/json\" }            \n"
          + "        }				                                         \n"
          + "	},												             \n"
          + "	\"response\": {									             \n"
          + "		\"status\": 200, 							             \n"
          + "		\"bodyFileName\": \"body-same-url-1$2!3.txt\"		 	 \n"
          + "	}												             \n"
          + "}													               ";

  private static final List<String> MATCHING_REQUEST_HEADERS =
      new ArrayList<>(Collections.singletonList("Accept"));

  @Test
  void includesHeadersInRequestPatternIfHeaderMatchingEnabled() {
    constructRecordingListener(MATCHING_REQUEST_HEADERS);

    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request1 =
        new MockRequestBuilder("MockRequestAcceptHtml")
            .withMethod(GET)
            .withUrl("/same/url")
            .withHeader("Accept", "text/html")
            .build();

    Request request2 =
        new MockRequestBuilder("MockRequestAcceptJson")
            .withMethod(GET)
            .withUrl("/same/url")
            .withHeader("Accept", "application/json")
            .build();

    listener.requestReceived(request1, response().status(200).fromProxy(true).build());
    listener.requestReceived(request2, response().status(200).fromProxy(true).build());

    verify(mappingsBlobStore)
        .put(
            (any(String.class)),
            argThat(
                equalToBinaryJson(SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_1, STRICT_ORDER)));
    verify(mappingsBlobStore)
        .put(
            (any(String.class)),
            argThat(
                equalToBinaryJson(SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_2, STRICT_ORDER)));
  }

  private static final String SAMPLE_REQUEST_MAPPING_WITH_JSON_BODY =
      "{                                                          \n"
          + "  \"request\" : {                                          \n"
          + "    \"url\" : \"/json/content\",                           \n"
          + "    \"method\" : \"POST\",                                 \n"
          + "    \"bodyPatterns\" : [ {                                 \n"
          + "      \"equalToJson\" : \"{}\",                            \n"
          + "      \"ignoreArrayOrder\" : true,                         \n"
          + "      \"ignoreExtraElements\" : true                       \n"
          + "    } ]                                                    \n"
          + "  },                                                       \n"
          + "  \"response\" : {                                         \n"
          + "    \"status\" : 200,                                      \n"
          + "    \"bodyFileName\" : \"body-json-content-1$2!3.txt\"     \n"
          + "  }                                                        \n"
          + "}";

  @Test
  void matchesBodyOnEqualToJsonIfJsonInRequestContentTypeHeader() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder()
            .withMethod(POST)
            .withUrl("/json/content")
            .withHeader("Content-Type", "application/json ")
            .withBody("{}")
            .build();

    listener.requestReceived(
        request, response().status(200).body("anything").fromProxy(true).build());
    verify(mappingsBlobStore)
        .put(
            (any(String.class)),
            argThat(equalToBinaryJson(SAMPLE_REQUEST_MAPPING_WITH_JSON_BODY, STRICT_ORDER)));
  }

  private static final String SAMPLE_REQUEST_MAPPING_WITH_XML_BODY =
      "{                                                                  \n"
          + "  \"request\" : {                                                  \n"
          + "    \"url\" : \"/xml/content\",                                    \n"
          + "    \"method\" : \"POST\",                                         \n"
          + "    \"bodyPatterns\" : [ {                                         \n"
          + "      \"equalToXml\" : \"<stuff />\"                               \n"
          + "    } ]                                                            \n"
          + "  },                                                               \n"
          + "  \"response\" : {                                                 \n"
          + "    \"status\" : 200,                                              \n"
          + "    \"bodyFileName\" : \"body-xml-content-1$2!3.txt\"              \n"
          + "  }                                                                \n"
          + "}";

  @Test
  void matchesBodyOnEqualToXmlIfXmlInRequestContentTypeHeader() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));
    Request request =
        new MockRequestBuilder()
            .withMethod(POST)
            .withUrl("/xml/content")
            .withHeader("Content-Type", "text/xml; content-type=utf-8")
            .withBody("<stuff />")
            .build();

    listener.requestReceived(
        request, response().status(200).body("anything").fromProxy(true).build());

    verify(mappingsBlobStore)
        .put(
            (any(String.class)),
            argThat(equalToBinaryJson(SAMPLE_REQUEST_MAPPING_WITH_XML_BODY, STRICT_ORDER)));
  }

  private static final String GZIP_REQUEST_MAPPING =
      "{ 													             \n"
          + "   \"id\": \"41544750-0c69-3fd7-93b1-f79499f987c3\",            \n"
          + "   \"uuid\": \"41544750-0c69-3fd7-93b1-f79499f987c3\",          \n"
          + "	\"request\": {									             \n"
          + "		\"method\": \"GET\",						             \n"
          + "		\"url\": \"/gzipped/content\"				             \n"
          + "	},												             \n"
          + "	\"response\": {									             \n"
          + "		\"status\": 200,							             \n"
          + "		\"bodyFileName\": \"body-gzipped-content-1$2!3.txt\"     \n"
          + "	}												             \n"
          + "}													               ";

  @Test
  void decompressesGzippedResponseBodyAndRemovesContentEncodingHeader() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder()
            .withHeader("Accept-Encoding", "gzip")
            .withMethod(RequestMethod.GET)
            .withUrl("/gzipped/content")
            .build();

    Response response =
        response()
            .status(200)
            .fromProxy(true)
            .headers(
                new HttpHeaders(
                    httpHeader("Content-Encoding", "gzip"), httpHeader("Content-Length", "123")))
            .body(gzip("Recorded body content"))
            .build();

    listener.requestReceived(request, response);

    verify(mappingsBlobStore)
        .put(
            (eq("mapping-gzipped-content-1$2!3.json")),
            argThat(equalToBinaryJson(GZIP_REQUEST_MAPPING, JSONCompareMode.STRICT)));
    verify(filesBlobStore)
        .put((eq("body-gzipped-content-1$2!3.txt")), (eq("Recorded body content".getBytes(UTF_8))));
  }

  private static final String MULTIPART_REQUEST_MAPPING =
      "{																	\n"
          + "	\"id\": \"41544750-0c69-3fd7-93b1-f79499f987c3\",				\n"
          + "	\"uuid\": \"41544750-0c69-3fd7-93b1-f79499f987c3\",				\n"
          + "	\"request\": {													\n"
          + "		\"method\": \"POST\",										\n"
          + "		\"url\": \"/multipart/content\",							\n"
          + "		\"multipartPatterns\" : [ {									\n"
          + "			\"name\" : \"binaryFile\",								\n"
          + "			\"matchingType\" : \"ALL\",								\n"
          + "			\"headers\" : {											\n"
          + "				\"Content-Disposition\" : {							\n"
          + "					\"contains\" : \"name=\\\"binaryFile\\\"\"		\n"
          + "			    }													\n"
          + "			},														\n"
          + "			\"bodyPatterns\" : [ {									\n"
          + "				\"binaryEqualTo\" : \"VGhpcyBhIGZpbGUgY29udGVudA==\"\n"
          + "			} ]														\n"
          + "		}, {														\n"
          + "			\"name\" : \"textFile\",								\n"
          + "			\"matchingType\" : \"ALL\",								\n"
          + "			\"headers\" : {											\n"
          + "				\"Content-Disposition\" : {							\n"
          + "					\"contains\" : \"name=\\\"textFile\\\"\"		\n"
          + "			    }													\n"
          + "			},														\n"
          + "			\"bodyPatterns\" : [ {									\n"
          + "				\"equalTo\" : \"This a file content\"				\n"
          + "			} ]														\n"
          + "		}, {														\n"
          + "			\"name\" : \"formInput\",								\n"
          + "			\"matchingType\" : \"ALL\",								\n"
          + "			\"headers\" : {											\n"
          + "				\"Content-Disposition\" : {							\n"
          + "					\"contains\" : \"name=\\\"formInput\\\"\"		\n"
          + "				},													\n"
          + "			},														\n"
          + "			\"bodyPatterns\" : [ {									\n"
          + "				\"equalTo\" : \"I am a field!\"						\n"
          + "			} ]														\n"
          + "		} ]															\n"
          + "	},												            	\n"
          + "	\"response\": {									            	\n"
          + "		\"status\": 200,							            	\n"
          + "		\"bodyFileName\": \"body-multipart-content-1$2!3.txt\"  	\n"
          + "	}												            	\n"
          + "}																	";

  @Test
  void multipartRequestProcessing() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder()
            .withMethod(RequestMethod.POST)
            .withHeader("Content-Type", "multipart/form-data")
            .withUrl("/multipart/content")
            .withMultiparts(
                Arrays.asList(
                    createPart(
                        "binaryFile",
                        "This a file content".getBytes(),
                        "application/octet-stream",
                        "binaryFile.raw"),
                    createPart(
                        "textFile", "This a file content".getBytes(), "text/plain", "textFile.txt"),
                    createPart("formInput", "I am a field!".getBytes(), null, null)))
            .build();

    listener.requestReceived(
        request, response().status(200).body("anything").fromProxy(true).build());

    verify(mappingsBlobStore)
        .put(
            eq("mapping-multipart-content-1$2!3.json"),
            argThat(equalToBinaryJson(MULTIPART_REQUEST_MAPPING, STRICT_ORDER)));
  }

  private static final String BAD_MULTIPART_REQUEST_MAPPING =
      "{ 													             \n"
          + "	\"id\": \"41544750-0c69-3fd7-93b1-f79499f987c3\",				\n"
          + "	\"uuid\": \"41544750-0c69-3fd7-93b1-f79499f987c3\",				\n"
          + "	\"request\": {									             \n"
          + "		\"url\": \"/multipart/content\",                         \n"
          + "		\"method\": \"POST\" 						             \n"
          + "	},												             \n"
          + "	\"response\": {									             \n"
          + "		\"status\": 200, 							             \n"
          + "		\"bodyFileName\": \"body-multipart-content-1$2!3.txt\"        \n"
          + "	}												             \n"
          + "}";

  @Test
  void multipartRequestProcessingWithNonFileMultipart() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder()
            .withMethod(RequestMethod.POST)
            .withHeader("Content-Type", "multipart/form-data; boundary=aBoundary")
            .withUrl("/multipart/content")
            .withBody("--aBoundary\r\nContent")
            .withMultiparts(null) // this is what request.getParts returns when parsing doesn't work
            .build();
    doReturn(true).when(request).isMultipart();

    listener.requestReceived(
        request, response().status(200).body("anything").fromProxy(true).build());

    verify(mappingsBlobStore)
        .put(
            eq("mapping-multipart-content-1$2!3.json"),
            argThat(bytesEqualToJson(BAD_MULTIPART_REQUEST_MAPPING, STRICT_ORDER)));
  }

  @Test
  void detectsJsonExtensionFromFileExtension() {
    assertResultingFileExtension("/my/file.json", "json");
  }

  @Test
  void detectsGifExtensionFromFileExtension() {
    assertResultingFileExtension("/my/file.gif", "gif");
  }

  @Test
  void detectsXmlExtensionFromResponseContentTypeHeader() {
    assertResultingFileExtension("/noext", "xml", "application/xml");
  }

  @Test
  void detectsJsonExtensionFromResponseContentTypeHeader() {
    assertResultingFileExtension("/noext", "json", "application/json");
  }

  @Test
  void detectsJsonExtensionFromCustomResponseContentTypeHeader() {
    assertResultingFileExtension("/noext", "json", "application/vnd.api+json");
  }

  @Test
  void detectsJpegExtensionFromResponseContentTypeHeader() {
    assertResultingFileExtension("/noext", "jpeg", "image/jpeg");
  }

  @Test
  void detectsIcoExtensionFromResponseContentTypeHeader() {
    assertResultingFileExtension("/noext", "ico", "image/x-icon");
  }

  @Test
  void sanitisesFilenamesBySwappingSymbolsForUnderscores() {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));

    Request request =
        new MockRequestBuilder()
            .withMethod(RequestMethod.GET)
            .withUrl("/my:oddly;~named!file/url")
            .build();

    Response.Builder responseBuilder = response().status(200).fromProxy(true);

    Response response = responseBuilder.build();

    listener.requestReceived(request, response);

    verify(filesBlobStore)
        .put(argThat(containsString("body-my_oddly__named_file-url")), (any(byte[].class)));
  }

  private void assertResultingFileExtension(String url, final String expectedExension) {
    assertResultingFileExtension(url, expectedExension, null);
  }

  private void assertResultingFileExtension(
      String url, final String expectedExension, String contentTypeHeader) {
    when(admin.countRequestsMatching((any(RequestPattern.class))))
        .thenReturn(VerificationResult.withCount(0));
    Request request = new MockRequestBuilder().withMethod(RequestMethod.GET).withUrl(url).build();

    byte[] body = new byte[] {1};

    Response.Builder responseBuilder = response().status(200).fromProxy(true).body(body);

    if (contentTypeHeader != null) {
      responseBuilder.headers(
          new HttpHeaders(HttpHeader.httpHeader("Content-Type", contentTypeHeader)));
    }

    Response response = responseBuilder.build();

    listener.requestReceived(request, response);

    verify(filesBlobStore).put(argThat(endsWith("." + expectedExension)), (any(byte[].class)));
  }

  private IdGenerator fixedIdGenerator(final String id) {
    return () -> id;
  }

  private static Request.Part createPart(
      final String name,
      final byte[] data,
      final String contentType,
      final String fileName,
      String... extraHeaderLines) {
    MockMultipart part = new MockMultipart().name(name).body(data);

    for (String headerLine : extraHeaderLines) {
      int i = headerLine.indexOf(':');

      if (i <= 0) {
        Assertions.fail("Invalid header expected line: " + headerLine);
      }

      Collection<String> params = new ArrayList<>();
      int start = i + 1;

      while (true) {
        int end = headerLine.indexOf(';', start);

        if (end > 0) {
          params.add(headerLine.substring(start, end).trim());
          start = end + 1;
        } else {
          break;
        }
      }

      part.header(headerLine.substring(0, i).trim(), params.toArray(new String[0]));
    }

    if (contentType != null) {
      part.header("Content-Type", contentType);
    }

    if (fileName == null) {
      part.header("Content-Disposition", "form-data", "name=\"" + name + "\"");
    } else {
      part.header(
          "Content-Disposition",
          "form-data",
          "name=\"" + name + "\"",
          "filename=\"" + fileName + "\"");
    }

    return part;
  }
}
