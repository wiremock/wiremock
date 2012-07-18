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

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.mapping.ResponseDefinition.copyOf;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static com.google.common.collect.Iterables.getFirst;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResponseDefinitionTest {

    @Test
    public void copyProducesEqualObject() {
        ResponseDefinition response = new ResponseDefinition();
        response.setBody("blah");
        response.setBodyFileName("name.json");
        response.setFault(Fault.EMPTY_RESPONSE);
        response.addHeader("thing", "thingvalue");
        response.setFixedDelayMilliseconds(1112);
        response.setProxyBaseUrl("http://base.com");
        response.setStatus(222);
        
        ResponseDefinition copiedResponse = copyOf(response);
        
        assertTrue(response.equals(copiedResponse));
    }
    
    @Test
    public void copyPreservesConfiguredFlag() {
        ResponseDefinition response = ResponseDefinition.notConfigured();
        ResponseDefinition copiedResponse = copyOf(response);
        assertFalse("Should be not configured", copiedResponse.wasConfigured());
    }

    private static final String SINGLE_VALUE_HEADER =
            "{	        					    			        \n" +
            "		\"status\": 200,  	    	    		        \n" +
            "		\"headers\": {    	    	    		        \n" +
            "		    \"Header-1\": \"only-value\"                \n" +
            "       }                                               \n" +
            "}										    	        ";

    @Test
    public void correctlyDeserializesWithSingleValueHeader() {
        ResponseDefinition responseDef = JsonMappingBinder.read(SINGLE_VALUE_HEADER, ResponseDefinition.class);
        HttpHeader header = getFirst(responseDef.getHeaders().all(), null);

        assertThat(header.key(), is("Header-1"));
        assertThat(header.firstValue(), is("only-value"));
        assertThat(header.values().size(), is(1));
    }

    @Test
    public void correctlySerializesSingleValueHeader() {
        ResponseDefinition responseDefinition = new ResponseDefinition();
        responseDefinition.addHeader("Header-1", "only-value");

        String json = JsonMappingBinder.write(responseDefinition);
        assertThat("Expected: " + SINGLE_VALUE_HEADER + "\nActual: " + json,
                json, equalToJson(SINGLE_VALUE_HEADER));
    }

}
