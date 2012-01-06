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
package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.jsonEqualTo;
import static java.util.Collections.emptyList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;

@RunWith(JMock.class)
public class MappingFileWriterListenerTest {
    
	private MappingFileWriterListener listener;
	private FileSource mappingsFileSource;
	private FileSource filesFileSource;
	
	private Mockery context;
	
	@Before
	public void init() {
		context = new Mockery();
		mappingsFileSource = context.mock(FileSource.class, "mappingsFileSource");
		filesFileSource = context.mock(FileSource.class, "filesFileSource");
		
		listener = new MappingFileWriterListener(mappingsFileSource, filesFileSource);
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
			allowing(mappingsFileSource).listFiles(); will(returnValue(emptyList()));
			allowing(filesFileSource).listFiles(); will(returnValue(emptyList()));
			one(mappingsFileSource).writeTextFile(with(equal("mapping-recorded-content-1$2!3.json")),
			        with(jsonEqualTo(SAMPLE_REQUEST_MAPPING)));
			one(filesFileSource).writeTextFile(with(equal("body-recorded-content-1$2!3.json")),
			        with(equal("Recorded body content")));
		}});
		
		Request request = new MockRequestBuilder(context)
			.withMethod(RequestMethod.GET)
			.withUrl("/recorded/content")
			.build();
		
		Response response = new Response(200);
		response.setBody("Recorded body content");
		
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
            allowing(mappingsFileSource).listFiles(); will(returnValue(emptyList()));
            allowing(filesFileSource).listFiles(); will(returnValue(emptyList()));
            one(mappingsFileSource).writeTextFile(with(equal("mapping-headered-content-1$2!3.json")),
                    with(jsonEqualTo(SAMPLE_REQUEST_MAPPING_WITH_HEADERS)));
            one(filesFileSource).writeTextFile("body-headered-content-1$2!3.json", "Recorded body content");
        }});
        
        Request request = new MockRequestBuilder(context)
            .withMethod(RequestMethod.GET)
            .withUrl("/headered/content")
            .build();
        
        Response response = new Response(200);
        response.setBody("Recorded body content");
        response.addHeader("Content-Type", "text/plain");
        response.addHeader("Cache-Control", "no-cache");
        
        listener.requestReceived(request, response);
	}

	private IdGenerator fixedIdGenerator(final String id) {
	    return new IdGenerator() {
            public String generate() {
                return id;
            }
        };
	}
}
