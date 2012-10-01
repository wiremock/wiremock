package com.github.tomakehurst.wiremock.mapping;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonTest {

	private static final String TEST_VALUE = "test-value";
	private static final String JSON_WITH_NO_COMMENTS = 
			"{                                          \n" +
                "\"property\": \"" + TEST_VALUE + "\"   \n" +
            "}";
	
	private static final String JSON_WITH_COMMENTS =
			"// this is the first comment                                                   \n" +
            "{                                                                              \n" +
                    "//this is a comment                                                    \n" +
                    "\"property\": \"" + TEST_VALUE + "\"// comment on same line as code    \n" +
            "}                                                                              \n" +
             "//this is the last comment";
	
	@Test
	public void testReadNoComments() {
		TestPojo pojo = Json.read(JSON_WITH_NO_COMMENTS, TestPojo.class);
		assertNotNull(pojo);
		assertThat(TEST_VALUE, is(pojo.property));
	}
	
	@Test
	public void testReadWithComments() {
		TestPojo pojo = Json.read(JSON_WITH_COMMENTS, TestPojo.class);
		assertNotNull(pojo);
		assertThat(TEST_VALUE, is(pojo.property));
	}
	
	private static class TestPojo {
		public String property;
	}
}
