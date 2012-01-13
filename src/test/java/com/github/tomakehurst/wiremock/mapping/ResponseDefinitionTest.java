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
