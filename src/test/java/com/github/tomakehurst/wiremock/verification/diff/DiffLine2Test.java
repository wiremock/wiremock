package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.http.content.Content;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;

public class DiffLine2Test {

    @Test
    public void equalToWithKey() {
        DiffLine2 diffLine = new DiffLine2(
            "My-Header",
            new EqualToPattern("the expected value"),
            Content.fromString("the wrong value")
        );
    }
}
