package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.github.tomakehurst.wiremock.common.Strings;
import org.junit.Test;

import java.util.Base64;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BodyTest {

    @Test
    public void constructsFromBytes() {
        Body body = Body.fromOneOf("this content".getBytes(), "not this content", new IntNode(1), "lskdjflsjdflks");

        assertThat(body.asString(), is("this content"));
        assertThat(body.isBinary(), is(true));
    }

    @Test
    public void constructsFromString() {
        Body body = Body.fromOneOf(null, "this content", new IntNode(1), "lskdjflsjdflks");

        assertThat(body.asString(), is("this content"));
        assertThat(body.isBinary(), is(false));
    }

    @Test
    public void constructsFromJson() {
        Body body = Body.fromOneOf(null, null, new IntNode(1), "lskdjflsjdflks");

        assertThat(body.asString(), is("1"));
        assertThat(body.isBinary(), is(false));
    }

    @Test
    public void constructsFromBase64() {
        String encodedText = Strings.stringFromBytes(Base64.getEncoder().encode("this content".getBytes()));
        Body body = Body.fromOneOf(null, null, null, encodedText);

        assertThat(body.asString(), is("this content"));
        assertThat(body.isBinary(), is(true));
    }

}
