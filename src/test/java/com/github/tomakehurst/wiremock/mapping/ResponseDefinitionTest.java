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

import org.junit.Test;

import static com.github.tomakehurst.wiremock.mapping.ResponseDefinition.copyOf;
import static junit.framework.Assert.assertFalse;
import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResponseDefinitionTest {

    private static final String STRING_BODY =
            "{	        								\n" +
            "		\"status\": 200,    				\n" +
            "		\"body\": \"String content\" 		\n" +
            "}											";

    @Test
    public void correctlyUnmarshalsFromJsonWhenBodyIsAString() {
        ResponseDefinition responseDef = JsonMappingBinder.read(STRING_BODY, ResponseDefinition.class);
        assertThat(responseDef.getBase64Body(), is(nullValue()));
        assertThat(responseDef.getBody(), is("String content"));
    }

    @Test
    public void correctlyMarshalsToJsonWhenBodyIsAString() {
        ResponseDefinition responseDef = new ResponseDefinition();
        responseDef.setStatus(200);
        responseDef.setBody("String content");

        assertJsonEquals(STRING_BODY, JsonMappingBinder.write(responseDef));
    }

    private static final byte[] BODY = new byte[] {1, 2, 3};
    private static final String BASE64_BODY = "AQID";
    private static final String BINARY_BODY =
            "{	        								        \n" +
            "		\"status\": 200,    				        \n" +
            "		\"base64Body\": \"" + BASE64_BODY + "\"     \n" +
            "}											        ";

    @Test
    public void correctlyUnmarshalsFromJsonWhenBodyIsBinary() {
        ResponseDefinition responseDef = JsonMappingBinder.read(BINARY_BODY, ResponseDefinition.class);
        assertThat(responseDef.getBody(), is(nullValue()));
        assertThat(responseDef.getByteBody(), is(BODY));
    }

    @Test
    public void correctlyMarshalsToJsonWhenBodyIsBinary() {
        ResponseDefinition responseDef = new ResponseDefinition();
        responseDef.setStatus(200);
        responseDef.setBase64Body(BASE64_BODY);

        String actualJson = JsonMappingBinder.write(responseDef);
        assertJsonEquals("Expected: " + BINARY_BODY + "\nActual: " + actualJson,
                BINARY_BODY, actualJson);
    }

    @Test
    public void copyPreservesConfiguredFlag() {
        ResponseDefinition response = ResponseDefinition.notConfigured();
        ResponseDefinition copiedResponse = copyOf(response);
        assertFalse("Should be not configured", copiedResponse.wasConfigured());
    }

}
