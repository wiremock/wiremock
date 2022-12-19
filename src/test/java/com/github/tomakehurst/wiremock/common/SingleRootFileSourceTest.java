/*
 * Copyright (C) 2011-2022 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.fileNamed;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.security.NotAuthorisedException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class SingleRootFileSourceTest {

  public static final String ROOT_PATH = filePath("filesource");

  @SuppressWarnings("unchecked")
  @Test
  public void listsTextFilesRecursively() {
    SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);

    List<TextFile> files = fileSource.listFilesRecursively();

    assertThat(
        files,
        hasExactlyIgnoringOrder(
            fileNamed("one"),
            fileNamed("two"),
            fileNamed("three"),
            fileNamed("four"),
            fileNamed("five"),
            fileNamed("six"),
            fileNamed("seven"),
            fileNamed("eight"),
            fileNamed("deepfile.json")));
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

  @Test
  public void listFilesRecursivelyThrowsExceptionWhenRootIsNotDir() {
    assertThrows(
        RuntimeException.class,
        () -> {
          SingleRootFileSource fileSource =
              new SingleRootFileSource("src/test/resources/filesource/one");
          fileSource.listFilesRecursively();
        });
  }

  @Test
  public void writeThrowsExceptionWhenRootIsNotDir() {
    assertThrows(
        RuntimeException.class,
        () -> {
          SingleRootFileSource fileSource =
              new SingleRootFileSource("src/test/resources/filesource/one");
          fileSource.writeTextFile("thing", "stuff");
        });
  }

  @Test
  public void
      listFilesRecursivelyThrowsExceptionWhenLastPathNodeIsSimilarToRootButWithExtraCharacters() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource =
              new SingleRootFileSource("src/test/resources/security-filesource/root");
          fileSource.getBinaryFileNamed("../rootdir/file.json");
        });
  }

  @Test
  public void writeTextFileThrowsExceptionWhenGivenRelativePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          fileSource.writeTextFile("..", "stuff");
        });
  }

  @Test
  public void writeTextFileThrowsExceptionWhenGivenAbsolutePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
          fileSource.writeTextFile(badPath, "stuff");
        });
  }

  @Test
  public void writeBinaryFileThrowsExceptionWhenGivenRelativePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          fileSource.writeBinaryFile("..", "stuff".getBytes());
        });
  }

  @Test
  public void writeBinaryFileThrowsExceptionWhenGivenAbsolutePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
          fileSource.writeBinaryFile(badPath, "stuff".getBytes());
        });
  }

  @Test
  public void deleteThrowsExceptionWhenGivenPathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
          fileSource.deleteFile(badPath);
        });
  }

  @Test
  public void readBinaryFileThrowsExceptionWhenRelativePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          fileSource.getBinaryFileNamed("../illegal.file");
        });
  }

  @Test
  public void readTextFileThrowsExceptionWhenRelativePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          fileSource.getTextFileNamed("../illegal.file");
        });
  }

  @Test
  public void readBinaryFileThrowsExceptionWhenAbsolutePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          String badPath = new File(ROOT_PATH, "../illegal.file").getCanonicalPath();
          fileSource.getBinaryFileNamed(badPath);
        });
  }

  @Test
  public void readTextFileThrowsExceptionWhenAbsolutePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(ROOT_PATH);
          String badPath = new File(ROOT_PATH, "../illegal.file").getCanonicalPath();
          fileSource.getTextFileNamed(badPath);
        });
  }
}
