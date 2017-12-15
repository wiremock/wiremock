/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.common;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.fileNamed;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SingleRootFileSourceTest {

	@SuppressWarnings("unchecked")
	@Test
	public void listsTextFilesRecursively() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
		
		List<TextFile> files = fileSource.listFilesRecursively();
		
		assertThat(files, hasExactlyIgnoringOrder(
				fileNamed("one"), fileNamed("two"), fileNamed("three"), 
				fileNamed("four"), fileNamed("five"), fileNamed("six"), 
				fileNamed("seven"), fileNamed("eight"), fileNamed("deepfile.json")));
	}
	
	@Test(expected=RuntimeException.class)
	public void listFilesRecursivelyThrowsExceptionWhenRootIsNotDir() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource/one");
		fileSource.listFilesRecursively();
	}
	
	@Test(expected=RuntimeException.class)
	public void writeThrowsExceptionWhenRootIsNotDir() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource/one");
		fileSource.writeTextFile("thing", "stuff");
	}

	@Test(expected=IllegalArgumentException.class)
	public void writeThrowsExceptionWhenGivenPathNotUnderRoot() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
		String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
		fileSource.writeTextFile(badPath, "stuff");
	}

	@Test(expected=IllegalArgumentException.class)
	public void deleteThrowsExceptionWhenGivenPathNotUnderRoot() {
		SingleRootFileSource fileSource = new SingleRootFileSource("src/test/resources/filesource");
        String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
		fileSource.deleteFile(badPath);
	}

	@Test
	public void writesTextFileEvenWhenRootIsARelativePath() throws IOException {
		String relativeRootPath = "./target/tmp/";
		FileUtils.forceMkdir(new File(relativeRootPath));
		SingleRootFileSource fileSource = new SingleRootFileSource(relativeRootPath);
		Path fileAbsolutePath = Paths.get(relativeRootPath).toAbsolutePath().resolve("myFile");
		fileSource.writeTextFile(fileAbsolutePath.toString(), "stuff");

		assertThat(Files.exists(fileAbsolutePath), is(true));
	}
}
