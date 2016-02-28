package com.github.tomakehurst.wiremock.matching;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.CookiePattern.cookiePattern;
import static com.github.tomakehurst.wiremock.matching.ValuePattern.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CookiePatternTest {

    @Test
    public void matchesABasicSessionCookieWithNoAdditionalParameters() {
        CookiePattern cookiePattern = cookiePattern()
            .withValue(equalTo("mycookievalue"))
            .build();

        Cookie cookie = Cookie.cookie()
            .withName("my_cookie")
            .withValue("mycookievalue");

        assertTrue(cookiePattern.isMatchFor(cookie));
    }


}
