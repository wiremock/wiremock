package com.github.tomakehurst.wiremock.mapping;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonTest {

	private static final String TEST_VALUE = "test-value";
	private static final String JSON_WITH_NO_COMMENTS = 
			"{\n"
			+ "\"property\": \"" + TEST_VALUE + "\"\n"
			+ "}";
	
	private static final String JSON_WITH_COMMENTS =
			"// this is the first comment\n"
			+ "{\n"
			+ "//this is a comment\n"
			+ "\"property\": \"" + TEST_VALUE + "\"// comment on same line as code\n"
			+ "}\n"
			+ "//this is the last comment";
	
	@Before
	public void setup() {
	}
	
	@After
	public void cleanup() {
	}
	
	@Test
	public void testReadNoComments() {
		TestPojo pojo = (TestPojo)Json.read(JSON_WITH_NO_COMMENTS, TestPojo.class);
		Assert.assertNotNull(pojo);
		Assert.assertEquals(TEST_VALUE, pojo.property);
	}
	
	@Test
	public void testReadWithComments() {
		TestPojo pojo = (TestPojo)Json.read(JSON_WITH_COMMENTS, TestPojo.class);
		Assert.assertNotNull(pojo);
		Assert.assertEquals(TEST_VALUE, pojo.property);
	}
	
	private static class TestPojo {
		public String property;
	}
}
