package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CookieTest {

    @Test
    public void serialisesCorrectlyWithSingleValue() {
        Cookie cookie = new Cookie("my_cookie", "one");
        assertThat(Json.write(cookie), is("\"one\""));
    }

    @Test
    public void serialisesCorrectlyWithManyValues() {
        Cookie cookie = new Cookie("my_cookie", "one", "two", "three");
        assertThat(Json.write(cookie), equalToJson("[\"one\", \"two\", \"three\"]"));
    }

    @Test
    public void serialisesCorrectlyWithNoValues() {
        Cookie cookie = new Cookie("my_cookie", new String[] {});
        assertThat(Json.write(cookie), equalToJson("[]"));
    }

    @Test
    public void deserialisesCorrectlyWithSingleValue() {
        String json = "\"one\"";

        Cookie cookie = Json.read(json, Cookie.class);

        assertThat(cookie.getValues().size(), is(1));
        assertThat(cookie.getValue(), is("one"));
    }

    @Test
    public void deserialisesCorrectlyWithManyValues() {
        String json = "[\"one\", \"two\", \"three\"]";

        Cookie cookie = Json.read(json, Cookie.class);

        assertThat(cookie.getValues().size(), is(3));
        assertThat(cookie.getValues(), hasItems("one", "two", "three"));
    }

    @Test
    public void deserialisesCorrectlyWithNoValues() {
        String json = "[]";

        Cookie cookie = Json.read(json, Cookie.class);

        assertThat(cookie.getValues().size(), is(0));
        assertThat(cookie.isAbsent(), is(true));
    }
}
