/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.security.NotAuthorisedException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SingleRootFileSourceTest {

  public static final String EXIST_FILES_ROOT_PATH = filePath("filesource");

  @SuppressWarnings("unchecked")
  @Test
  void listsTextFilesRecursively() {
    SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);

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
  void writesTextFileEvenWhenRootIsARelativePath() {
    String relativeRootPath = "./target/tmp/";
    new File(String.valueOf(Paths.get(relativeRootPath).toAbsolutePath())).mkdirs();
    SingleRootFileSource fileSource = new SingleRootFileSource(relativeRootPath);
    Path fileAbsolutePath = Paths.get(relativeRootPath).toAbsolutePath().resolve("myFile");
    fileSource.writeTextFile(fileAbsolutePath.toString(), "stuff");

    assertThat(Files.exists(fileAbsolutePath), is(true));
  }

  @Test
  void lazilyCreatesTheRootDirectorWhenTextWriteAttempted(@TempDir Path tempDir) {
    SingleRootFileSource fileSource =
        new SingleRootFileSource(new File(tempDir.toFile(), "child-dir"));

    File childDir = tempDir.resolve("child-dir").toFile();

    assertFalse(childDir.exists(), "The child directory shouldn't exist yet");

    fileSource.writeTextFile("my-file", "My text");
    assertTrue(childDir.exists(), "The child directory should exist after write attempt");
  }

  @Test
  void lazilyCreatesTheRootDirectorWhenBinaryWriteAttempted(@TempDir Path tempDir) {
    SingleRootFileSource fileSource =
        new SingleRootFileSource(new File(tempDir.toFile(), "child-dir"));

    File childDir = tempDir.resolve("child-dir").toFile();

    assertFalse(childDir.exists(), "The child directory shouldn't exist yet");

    fileSource.writeBinaryFile("my-file", "My text".getBytes());
    assertTrue(childDir.exists(), "The child directory should exist after write attempt");
  }

  @Test
  void listFilesRecursivelyThrowsExceptionWhenRootIsNotDir() {
    assertThrows(
        RuntimeException.class,
        () -> {
          SingleRootFileSource fileSource =
              new SingleRootFileSource("src/test/resources/filesource/one");
          fileSource.listFilesRecursively();
        });
  }

  @Test
  void writeThrowsExceptionWhenRootIsNotDir() {
    assertThrows(
        RuntimeException.class,
        () -> {
          SingleRootFileSource fileSource =
              new SingleRootFileSource("src/test/resources/filesource/one");
          fileSource.writeTextFile("thing", "stuff");
        });
  }

  @Test
  void listFilesRecursivelyThrowsExceptionWhenLastPathNodeIsSimilarToRootButWithExtraCharacters() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource =
              new SingleRootFileSource("src/test/resources/security-filesource/root");
          fileSource.getBinaryFileNamed("../rootdir/file.json");
        });
  }

  @Test
  void writeTextFileThrowsExceptionWhenGivenRelativePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          fileSource.writeTextFile("..", "stuff");
        });
  }

  @Test
  void writeTextFileThrowsExceptionWhenGivenAbsolutePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
          fileSource.writeTextFile(badPath, "stuff");
        });
  }

  @Test
  void writeBinaryFileThrowsExceptionWhenGivenRelativePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          fileSource.writeBinaryFile("..", "stuff".getBytes());
        });
  }

  @Test
  void writeBinaryFileThrowsExceptionWhenGivenAbsolutePathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
          fileSource.writeBinaryFile(badPath, "stuff".getBytes());
        });
  }

  @Test
  void deleteThrowsExceptionWhenGivenPathNotUnderRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          String badPath = Paths.get("..", "not-under-root").toAbsolutePath().toString();
          fileSource.deleteFile(badPath);
        });
  }

  @Test
  void readBinaryFileThrowsExceptionWhenRelativePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          fileSource.getBinaryFileNamed("../illegal.file");
        });
  }

  @Test
  void readTextFileThrowsExceptionWhenRelativePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          fileSource.getTextFileNamed("../illegal.file");
        });
  }

  @Test
  public void readBinaryFileThrowsExceptionWhenAbsolutePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          String badPath = new File(EXIST_FILES_ROOT_PATH, "../illegal.file").getCanonicalPath();
          fileSource.getBinaryFileNamed(badPath);
        });
  }

  @Test
  void readTextFileThrowsExceptionWhenAbsolutePathIsOutsideRoot() {
    assertThrows(
        NotAuthorisedException.class,
        () -> {
          SingleRootFileSource fileSource = new SingleRootFileSource(EXIST_FILES_ROOT_PATH);
          String badPath = new File(EXIST_FILES_ROOT_PATH, "../illegal.file").getCanonicalPath();
          fileSource.getTextFileNamed(badPath);
        });
  }
}
