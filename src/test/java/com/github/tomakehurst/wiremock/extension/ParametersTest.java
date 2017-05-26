package com.github.tomakehurst.wiremock.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ParametersTest {

    @Test
    public void convertsParametersToAnObject() {
        MyData myData = Parameters.from(ImmutableMap.<String, Object>of(
            "name", "Tom",
            "num", 27
        )).as(MyData.class);

        assertThat(myData.getName(), is("Tom"));
        assertThat(myData.getNum(), is(27));
    }

    @Test
    public void convertsToParametersFromAnObject() {
        MyData myData = new MyData("Mark", 12);

        Parameters parameters = Parameters.of(myData);

        assertThat(parameters.getString("name"), is("Mark"));
        assertThat(parameters.getInt("num"), is(12));
    }

    public static class MyData {

        private final String name;
        private final Integer num;

        @JsonCreator
        public MyData(@JsonProperty("name") String name,
                      @JsonProperty("num") Integer num) {
            this.name = name;
            this.num = num;
        }

        public String getName() {
            return name;
        }

        public Integer getNum() {
            return num;
        }
    }
}
