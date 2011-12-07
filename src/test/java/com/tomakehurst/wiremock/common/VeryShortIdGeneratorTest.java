package com.tomakehurst.wiremock.common;

import static com.tomakehurst.wiremock.testsupport.WireMatchers.matches;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class VeryShortIdGeneratorTest {

    @Test
    public void IdsGeneratedContainOnlyLegalCharsAndAreRightLength() {
        IdGenerator generator = new VeryShortIdGenerator();
        
        for (int i = 0; i < 1000; i++) {
            String id = generator.generate();
            assertThat(id, matches("[A-Za-z0-9$#&+!()-{}]{5}"));
        }
    }
    
}
