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
package com.github.tomakehurst.wiremock.stubbing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;
import com.github.tomakehurst.wiremock.verification.VerificationResult;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.http.CaseInsensitiveKey.TO_CASE_INSENSITIVE_KEYS;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.transform;

@RunWith(JMock.class)
public class StubMappingJsonRecorderTest {
    
	private StubMappingJsonRecorder listener;
	private FileSource mappingsFileSource;
	private FileSource filesFileSource;
    private Admin admin;
	
	private Mockery context;
	
	@Before
	public void init() {
		context = new Mockery();
		mappingsFileSource = context.mock(FileSource.class, "mappingsFileSource");
		filesFileSource = context.mock(FileSource.class, "filesFileSource");
        admin = context.mock(Admin.class);

        constructRecordingListener(Collections.<String>emptyList());
	}

    private void constructRecordingListener(List<String> headersToRecord) {
        listener = new StubMappingJsonRecorder(mappingsFileSource, filesFileSource, admin, transform(headersToRecord, TO_CASE_INSENSITIVE_KEYS));
        listener.setIdGenerator(fixedIdGenerator("1$2!3"));
    }

    private static final String SAMPLE_REQUEST_MAPPING =
		"{ 													             \n" +
		"	\"request\": {									             \n" +
		"		\"method\": \"GET\",						             \n" +
		"		\"url\": \"/recorded/content\"				             \n" +
		"	},												             \n" +
		"	\"response\": {									             \n" +
		"		\"status\": 200,							             \n" +
		"		\"bodyFileName\": \"body-recorded-content-1$2!3.json\"   \n" +
		"	}												             \n" +
		"}													               ";
	
	@Test
	public void writesMappingFileAndCorrespondingBodyFileOnRequest() {
		context.checking(new Expectations() {{
		    allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
			one(mappingsFileSource).writeTextFile(with(equal("mapping-recorded-content-1$2!3.json")),
			        with(equalToJson(SAMPLE_REQUEST_MAPPING)));
			one(filesFileSource).writeBinaryFile(with(equal("body-recorded-content-1$2!3.json")),
                    with(equal("Recorded body content".getBytes(UTF_8))));
		}});
		
		Request request = new MockRequestBuilder(context)
			.withMethod(RequestMethod.GET)
			.withUrl("/recorded/content")
			.build();

        Response response = response()
                .status(200)
                .fromProxy(true)
                .body("Recorded body content")
                .build();

		listener.requestReceived(request, response);
	}
	
	private static final String SAMPLE_REQUEST_MAPPING_WITH_HEADERS =
        "{                                                                  \n" +
        "   \"request\": {                                                  \n" +
        "       \"method\": \"GET\",                                        \n" +
        "       \"url\": \"/headered/content\"                              \n" +
        "   },                                                              \n" +
        "   \"response\": {                                                 \n" +
        "       \"status\": 200,                                            \n" +
        "       \"bodyFileName\": \"body-headered-content-1$2!3.json\",     \n" +
        "       \"headers\": {                                              \n" +
        "            \"Content-Type\": \"text/plain\",                      \n" +
        "            \"Cache-Control\": \"no-cache\"                        \n" +
        "       }                                                           \n" +
        "   }                                                               \n" +
        "}                                                                  ";
	
	@Test
	public void addsResponseHeaders() {
	    context.checking(new Expectations() {{
	        allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(1)));
            one(mappingsFileSource).writeTextFile(with(equal("mapping-headered-content-1$2!3.json")),
                    with(equalToJson(SAMPLE_REQUEST_MAPPING_WITH_HEADERS)));
            one(filesFileSource).writeBinaryFile("body-headered-content-1$2!3.json", "Recorded body content".getBytes(UTF_8));
        }});
        
        Request request = new MockRequestBuilder(context)
            .withMethod(RequestMethod.GET)
            .withUrl("/headered/content")
            .build();

        Response response = response()
                .status(200)
                .fromProxy(true)
                .body("Recorded body content")
                .headers(new HttpHeaders(
                        httpHeader("Content-Type", "text/plain"),
                        httpHeader("Cache-Control", "no-cache")))
                .build();

        listener.requestReceived(request, response);
	}
	
	@Test
	public void doesNotWriteFileIfRequestAlreadyReceived() {
	    context.checking(new Expectations() {{
            atLeast(1).of(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(2)));
            never(mappingsFileSource).writeTextFile(with(any(String.class)), with(any(String.class)));
            never(filesFileSource).writeTextFile(with(any(String.class)), with(any(String.class)));
        }});
	    
	    listener.requestReceived(new MockRequestBuilder(context)
                .withMethod(RequestMethod.GET)
                .withUrl("/headered/content")
                .build(),
            response().status(200).build());
	}
	
	@Test
	public void doesNotWriteFileIfResponseNotFromProxy() {
	    context.checking(new Expectations() {{
            allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
            never(mappingsFileSource).writeTextFile(with(any(String.class)), with(any(String.class)));
            never(filesFileSource).writeTextFile(with(any(String.class)), with(any(String.class)));
        }});

        Response response = response()
                .status(200)
                .fromProxy(false)
                .build();
	    
        listener.requestReceived(new MockRequestBuilder(context)
                .withMethod(RequestMethod.GET)
                .withUrl("/headered/content")
                .build(),
            response);
	}

    private static final String SAMPLE_REQUEST_MAPPING_WITH_BODY =
            "{ 													             \n" +
            "	\"request\": {									             \n" +
            "		\"method\": \"POST\",						             \n" +
            "		\"url\": \"/body/content\",                              \n" +
            "       \"bodyPatterns\": [                                      \n" +
            "            { \"equalTo\": \"somebody\" }                       \n" +
            "        ]				                                         \n" +
            "	},												             \n" +
            "	\"response\": {									             \n" +
            "		\"status\": 200, 							             \n" +
            "		\"bodyFileName\": \"body-body-content-1$2!3.json\"       \n" +
            "	}												             \n" +
            "}													               ";

    @Test
    public void includesBodyInRequestPatternIfInRequest() {
        context.checking(new Expectations() {{
            allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
            one(mappingsFileSource).writeTextFile(
                    with(any(String.class)),
                    with(equalToJson(SAMPLE_REQUEST_MAPPING_WITH_BODY)));
            ignoring(filesFileSource);
        }});

        Request request = new MockRequestBuilder(context)
                .withMethod(POST)
                .withUrl("/body/content")
                .withHeader("Content-Type", "text/plain")
                .withBody("somebody")
                .build();

        listener.requestReceived(request,
                response().status(200).body("anything").fromProxy(true).build());
    }
    
    private static final String SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_1 =
            "{ 													             \n" +
            "	\"request\": {									             \n" +
            "		\"method\": \"GET\",						             \n" +
            "		\"url\": \"/same/url\",                             	 \n" +
            "       \"headers\": {                                       	 \n" +
            "			 \"Accept\":										 \n" +	
            "            	{ \"equalTo\": \"text/html\" }            		 \n" +
            "        }				                                         \n" +
            "	},												             \n" +
            "	\"response\": {									             \n" +
            "		\"status\": 200,							             \n" +
            "		\"bodyFileName\": \"body-same-url-1$2!3.json\"		 	 \n" +
            "	}												             \n" +
            "}													               ";
    
    private static final String SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_2 =
            "{ 													             \n" +
            "	\"request\": {									             \n" +
            "		\"method\": \"GET\",						             \n" +
            "		\"url\": \"/same/url\",                             	 \n" +
            "       \"headers\": {                                       	 \n" +
            "			 \"Accept\":										 \n" +	
            "            	{ \"equalTo\": \"application/json\" }            \n" +
            "        }				                                         \n" +
            "	},												             \n" +
            "	\"response\": {									             \n" +
            "		\"status\": 200, 							             \n" +
            "		\"bodyFileName\": \"body-same-url-1$2!3.json\"		 	 \n" +
            "	}												             \n" +
            "}													               ";
    
    private static final List<String> MATCHING_REQUEST_HEADERS = new ArrayList<String>(Arrays.asList("Accept"));
    
    @Test
    public void includesHeadersInRequestPatternIfHeaderMatchingEnabled() {
        constructRecordingListener(MATCHING_REQUEST_HEADERS);

        context.checking(new Expectations() {{
            allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
            one(mappingsFileSource).writeTextFile(
                    with(any(String.class)),
                    with(equalToJson(SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_1)));
            one(mappingsFileSource).writeTextFile(
                    with(any(String.class)),
                    with(equalToJson(SAMPLE_REQUEST_MAPPING_WITH_REQUEST_HEADERS_2)));
            ignoring(filesFileSource);
        }});

        Request request1 = new MockRequestBuilder(context, "MockRequestAcceptHtml")
                .withMethod(GET)
                .withUrl("/same/url")
                .withHeader("Accept", "text/html")
                .build();
        
        Request request2 = new MockRequestBuilder(context, "MockRequestAcceptJson")
		        .withMethod(GET)
		        .withUrl("/same/url")
		        .withHeader("Accept", "application/json")
		        .build();

        listener.requestReceived(request1,
                response().status(200).fromProxy(true).build());
        listener.requestReceived(request2,
                response().status(200).fromProxy(true).build());
    }

    private static final String SAMPLE_REQUEST_MAPPING_WITH_JSON_BODY =
            "{                                                          \n" +
            "  \"request\" : {                                          \n" +
            "    \"url\" : \"/json/content\",                           \n" +
            "    \"method\" : \"POST\",                                 \n" +
            "    \"bodyPatterns\" : [ {                                 \n" +
            "      \"equalToJson\" : \"{}\",                            \n" +
            "      \"jsonCompareMode\" : \"LENIENT\"                    \n" +
            "    } ]                                                    \n" +
            "  },                                                       \n" +
            "  \"response\" : {                                         \n" +
            "    \"status\" : 200,                                      \n" +
            "    \"bodyFileName\" : \"body-json-content-1$2!3.json\"    \n" +
            "  }                                                        \n" +
            "}";

    @Test
    public void matchesBodyOnEqualToJsonIfJsonInRequestContentTypeHeader() {
        context.checking(new Expectations() {{
            allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
            one(mappingsFileSource).writeTextFile(
                    with(any(String.class)),
                    with(equalToJson(SAMPLE_REQUEST_MAPPING_WITH_JSON_BODY)));
            ignoring(filesFileSource);
        }});

        Request request = new MockRequestBuilder(context)
                .withMethod(POST)
                .withUrl("/json/content")
                .withHeader("Content-Type", "application/json ")
                .withBody("{}")
                .build();

        listener.requestReceived(request,
                response().status(200).body("anything").fromProxy(true).build());
    }

    private static final String SAMPLE_REQUEST_MAPPING_WITH_XML_BODY =
            "{                                                                  \n" +
            "  \"request\" : {                                                  \n" +
            "    \"url\" : \"/xml/content\",                                    \n" +
            "    \"method\" : \"POST\",                                         \n" +
            "    \"bodyPatterns\" : [ {                                         \n" +
            "      \"equalToXml\" : \"<stuff />\"                               \n" +
            "    } ]                                                            \n" +
            "  },                                                               \n" +
            "  \"response\" : {                                                 \n" +
            "    \"status\" : 200,                                              \n" +
            "    \"bodyFileName\" : \"body-xml-content-1$2!3.json\"             \n" +
            "  }                                                                \n" +
            "}";

    @Test
    public void matchesBodyOnEqualToXmlIfXmlInRequestContentTypeHeader() {
        context.checking(new Expectations() {{
            allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
            one(mappingsFileSource).writeTextFile(
                    with(any(String.class)),
                    with(equalToJson(SAMPLE_REQUEST_MAPPING_WITH_XML_BODY)));
            ignoring(filesFileSource);
        }});

        Request request = new MockRequestBuilder(context)
                .withMethod(POST)
                .withUrl("/xml/content")
                .withHeader("Content-Type", "text/xml; content-type=utf-8")
                .withBody("<stuff />")
                .build();

        listener.requestReceived(request,
                response().status(200).body("anything").fromProxy(true).build());
    }
    
	private IdGenerator fixedIdGenerator(final String id) {
	    return new IdGenerator() {
            public String generate() {
                return id;
            }
        };
	}
}
