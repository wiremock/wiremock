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

import static com.github.tomakehurst.wiremock.mapping.ResponseDefinition.copyOf;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.github.tomakehurst.wiremock.http.Fault;

public class ResponseDefinitionTest {

    @Test
    public void copyProducesEqualObject() {
        ResponseDefinition response = new ResponseDefinition();
        response.setBody("blah");
        response.setBodyFileName("name.json");
        response.setByteBody(new byte[]{1,2});
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
}
