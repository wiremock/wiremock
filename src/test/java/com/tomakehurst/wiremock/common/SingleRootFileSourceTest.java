package com.tomakehurst.wiremock.common;

import static com.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class SingleRootFileSourceTest {

	@SuppressWarnings("unchecked")
	@Test
	public void listsTextFilesAtTopLevelIgnoringDirectories() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
		
		List<TextFile> files = fileSource.listFiles();
		
		assertThat(files, hasExactlyIgnoringOrder(
				fileNamed("one"), fileNamed("two"), fileNamed("three")));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void listsTextFilesRecursively() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
		
		List<TextFile> files = fileSource.listFilesRecursively();
		
		assertThat(files, hasExactlyIgnoringOrder(
				fileNamed("one"), fileNamed("two"), fileNamed("three"), 
				fileNamed("four"), fileNamed("five"), fileNamed("six"), 
				fileNamed("seven"), fileNamed("eight")));
	}
	
	@Test(expected=RuntimeException.class)
	public void throwsExceptionWhenRootIsNotDir() {
		new SingleRootFileSource("src/test/resources/filesource/one");
	}
	
	private Matcher<TextFile> fileNamed(final String name) {
		return new TypeSafeMatcher<TextFile>() {

			@Override
			public void describeTo(Description desc) {
			}

			@Override
			public boolean matchesSafely(TextFile textFile) {
				return textFile.name().equals(name);
			}
			
		};
	}
}
