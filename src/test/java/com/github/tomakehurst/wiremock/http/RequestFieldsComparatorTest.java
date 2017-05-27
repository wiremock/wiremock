package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RequestFieldsComparatorTest {
    @Test
    public void compareWithNoFields(){
        RequestFieldsComparator fields = new RequestFieldsComparator();
        assertEquals(0, fields.compare(new StubMapping(), new StubMapping()));
    }

    @Test
    public void compareUrlWithNull() {
        RequestFieldsComparator field = new RequestFieldsComparator("url");
        StubMapping one = newStubMapping(RequestPatternBuilder.allRequests());
        StubMapping two = newStubMapping(new RequestPatternBuilder().withUrl("FOO"));

        assertEquals(0, field.compare(one, one));
        assertEquals(-1, field.compare(one, two));
        assertEquals(1, field.compare(two, one));
    }

    @Test
    public void compareUrlWithEqualUrlsCaseInsenstive() {
        RequestFieldsComparator field = new RequestFieldsComparator("url");
        StubMapping one = newStubMapping(new RequestPatternBuilder().withUrl("foo"));
        StubMapping two = newStubMapping(new RequestPatternBuilder().withUrl("FOO"));

        assertEquals(0, field.compare(one, one));
        assertEquals(0, field.compare(one, two));
    }

    @Test
    public void compareUrlWithUnequalUrls() {
        RequestFieldsComparator field = new RequestFieldsComparator("url");
        StubMapping one = newStubMapping(new RequestPatternBuilder().withUrl("foo"));
        StubMapping two = newStubMapping(new RequestPatternBuilder().withUrl("bar"));

        assertThat(0, greaterThan(field.compare(two, one)));
        assertThat(0, lessThan(field.compare(one, two)));
    }

    @Test
    public void compareMethod() {
        RequestFieldsComparator field = new RequestFieldsComparator("method");
        StubMapping one = newStubMapping(new RequestPatternBuilder(RequestMethod.GET, null));
        StubMapping two = newStubMapping(new RequestPatternBuilder(RequestMethod.TRACE, null));

        assertEquals(0, field.compare(one, one));
        assertEquals(0, field.compare(one, newStubMapping(new RequestPatternBuilder(RequestMethod.GET, null))));

        assertNotEquals(0, field.compare(one, two));
        assertNotEquals(0, field.compare(two, one));
    }

    @Test
    public void compareHeaderWithOneMissingAllHeaders() {
        RequestFieldsComparator field = new RequestFieldsComparator("Accept");
        StubMapping one = newStubMapping(new RequestPatternBuilder().withHeader("foo", equalTo("bar")));
        StubMapping two = newStubMapping(new RequestPatternBuilder());

        assertEquals(1, field.compare(one, two));
        assertEquals(-1, field.compare(two, one));
    }

    @Test
    public void compareHeaderWithOneMissingHeader() {
        RequestFieldsComparator field = new RequestFieldsComparator("Accept");
        StubMapping one = newStubMapping(new RequestPatternBuilder().withHeader("foo", equalTo("bar")));
        StubMapping two = newStubMapping(new RequestPatternBuilder().withHeader("Accept", equalTo("bar")));

        assertEquals(-1, field.compare(one, two));
        assertEquals(1, field.compare(two, one));
    }

    @Test
    public void compareHeaderNotEqual() {
        RequestFieldsComparator field = new RequestFieldsComparator("Accept");
        StubMapping one = newStubMapping(new RequestPatternBuilder().withHeader("Accept", equalTo("foo")));
        StubMapping two = newStubMapping(new RequestPatternBuilder().withHeader("Accept", equalTo("bar")));

        assertThat(0, greaterThan(field.compare(two, one)));
        assertThat(0, lessThan(field.compare(one, two)));
    }

    @Test
    public void compareHeaderEqual() {
        RequestFieldsComparator field = new RequestFieldsComparator("Accept");
        StubMapping one = newStubMapping(new RequestPatternBuilder().withHeader("Accept", equalTo("foo")));
        StubMapping two = newStubMapping(new RequestPatternBuilder().withHeader("Accept", equalTo("FOo")));

        assertEquals(0, field.compare(one, one));
        assertEquals(0, field.compare(one, two));
        assertEquals(0, field.compare(two, one));
    }

    private static StubMapping newStubMapping(RequestPatternBuilder builder) {
        return new StubMapping(builder.build(), ResponseDefinition.ok());
    }
}