package com.github.tomakehurst.wiremock.common;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class UniqueFilenameGeneratorTest {

    private Mockery context;

    @Before
    public void init() {
        context = new Mockery();
    }

    @Test
    public void generatesValidNameWhenRequestHasUrlWithTwoPathNodes() {
        String fileName = UniqueFilenameGenerator.generate(
                aRequest(context).withUrl("/some/path").build(),
                "body",
                "random123");

        assertThat(fileName, is("body-some-path-random123.json"));
    }

    @Test
    public void generatesValidNameWhenRequestHasUrlWithOnePathNode() {
        String fileName = UniqueFilenameGenerator.generate(
                aRequest(context).withUrl("/thing").build(),
                "body",
                "random123");

        assertThat(fileName, is("body-thing-random123.json"));
    }

    @Test
    public void generatesValidNameWhenRequestHasRootPath() {
        String fileName = UniqueFilenameGenerator.generate(
                aRequest(context).withUrl("/").build(),
                "body",
                "random123");

        assertThat(fileName, is("body-(root)-random123.json"));
    }
}
