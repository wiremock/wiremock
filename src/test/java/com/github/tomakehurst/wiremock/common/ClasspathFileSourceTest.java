/*
 * Copyright (C) 2014-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.fileNamed;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClasspathFileSourceTest {

  ClasspathFileSource classpathFileSource;

  @Test
  void listsFilesRecursivelyFromJar() {
    initForJar();

    List<TextFile> files = classpathFileSource.listFilesRecursively();

    assertThat(files, hasItems(fileNamed("pom.properties"), fileNamed("pom.xml")));
  }

  @Test
  void listsFilesRecursivelyFromFileSystem() {
    initForFileSystem();

    List<TextFile> files = classpathFileSource.listFilesRecursively();

    assertThat(
        files,
        hasItems(
            fileNamed("one"),
            fileNamed("two"),
            fileNamed("three"),
            fileNamed("four"),
            fileNamed("five"),
            fileNamed("six")));
  }

  @Test
  void listsReadableFilesFromAllMatchingClasspathDirectories(@TempDir Path tempDir)
      throws IOException {
    Path firstClasspathRoot = Files.createDirectories(tempDir.resolve("first/test-files"));
    Path secondClasspathRoot = Files.createDirectories(tempDir.resolve("second/test-files"));
    Files.writeString(firstClasspathRoot.resolve("first.txt"), "first only");
    Files.writeString(firstClasspathRoot.resolve("shared.txt"), "first shared");
    Files.writeString(secondClasspathRoot.resolve("second.txt"), "second only");
    Files.writeString(secondClasspathRoot.resolve("shared.txt"), "second shared");

    URL[] urls = {
      tempDir.resolve("first").toUri().toURL(), tempDir.resolve("second").toUri().toURL()
    };
    try (URLClassLoader classLoader = new URLClassLoader(urls, null)) {
      classpathFileSource = new ClasspathFileSource(classLoader, "test-files");

      List<TextFile> files = classpathFileSource.listFilesRecursively();

      assertThat(files, hasSize(4));
      assertThat(
          files.stream().map(TextFile::readContentsAsString).toList(),
          containsInAnyOrder("first only", "first shared", "second only", "second shared"));
      assertThat(
          classpathFileSource.getTextFileNamed("shared.txt").readContentsAsString(),
          is("first shared"));
      assertThat(
          new String(classpathFileSource.getBinaryFileNamed("shared.txt").readContents(), UTF_8),
          is("first shared"));
    }
  }

  @Test
  void listsReadableFilesFromMatchingDirectoryAndJar(@TempDir Path tempDir) throws IOException {
    Path directoryRoot = Files.createDirectories(tempDir.resolve("jar-filesource"));
    Files.writeString(directoryRoot.resolve("one.txt"), "one");
    Path archive =
        Files.copy(
            Path.of("src/test/resources/classpath-filesource.jar"),
            tempDir.resolve("classpath-filesource.jar"));

    URL[] urls = {tempDir.toUri().toURL(), archive.toUri().toURL()};
    try (URLClassLoader classLoader = new URLClassLoader(urls, null)) {
      classpathFileSource = new ClasspathFileSource(classLoader, "jar-filesource");

      List<TextFile> files = classpathFileSource.listFilesRecursively();

      assertThat(files, hasItems(fileNamed("one.txt"), fileNamed("stuff.txt")));
      assertThat(findFileNamed(files, "stuff.txt").readContentsAsString(), is("THINGS!"));
    }
  }

  @Test
  void closesArchiveResourcesAndHandlesSpacesInArchivePath(@TempDir Path tempDir)
      throws IOException {
    Path archive =
        Files.copy(
            Path.of("src/test/resources/classpath-filesource.jar"),
            tempDir.resolve("classpath archive with spaces.jar"));
    TextFile jarFile;

    try (URLClassLoader classLoader =
        new URLClassLoader(new URL[] {archive.toUri().toURL()}, null)) {
      classpathFileSource = new ClasspathFileSource(classLoader, "jar-filesource");
      jarFile = findFileNamed(classpathFileSource.listFilesRecursively(), "stuff.txt");
    }

    Path movedArchive = tempDir.resolve("moved archive.jar");
    Files.move(archive, movedArchive);
    Files.move(movedArchive, archive);
    assertThat(jarFile.readContentsAsString(), is("THINGS!"));
  }

  @Test
  void readsBinaryFileFromJar() {
    initForJar();

    BinaryFile binaryFile = classpathFileSource.getBinaryFileNamed("guava/pom.xml");

    assertThat("Expected a non zero length file", binaryFile.readContents().length, greaterThan(0));
  }

  @Test
  void readsBinaryFileFromCustomClassLoader() throws MalformedURLException {
    initForCustomClassLoader();

    BinaryFile binaryFile = classpathFileSource.child("__files").getBinaryFileNamed("stuff.txt");

    assertThat("Expected a non zero length file", binaryFile.readContents().length, greaterThan(0));
  }

  @Test
  void readsBinaryFileFromZip() {
    classpathFileSource = new ClasspathFileSource("zippeddir");

    BinaryFile binaryFile = classpathFileSource.getBinaryFileNamed("zippedfile.txt");

    String contents = new String(binaryFile.readContents());
    assertThat(contents, containsString("zip"));
  }

  @Test
  void readsBinaryFileFromZipWithoutMatch() {
    classpathFileSource = new ClasspathFileSource("zippeddir");
    try {
      classpathFileSource.getBinaryFileNamed("thisWillNotBeFound.txt");
      fail("Should have thrown exception.");
    } catch (Exception e) {
      assertThat(
          "Informative error",
          e.getMessage(),
          startsWith("File thisWillNotBeFound.txt not found on classpath in zippeddir"));
    }
  }

  @Test
  void readsBinaryFileFromFileSystem() {
    initForFileSystem();

    BinaryFile binaryFile = classpathFileSource.getBinaryFileNamed("subdir/deepfile.json");

    assertThat("Expected a non zero length file", binaryFile.readContents().length, greaterThan(0));
  }

  @Test
  void createsChildSource() {
    initForFileSystem();

    FileSource child = classpathFileSource.child("subdir");

    assertThat(child.getPath(), is("filesource/subdir"));
  }

  @Test
  void correctlyReportsExistence() {
    assertTrue(new ClasspathFileSource("filesource/subdir").exists(), "Expected to exist");
    assertTrue(
        new ClasspathFileSource("META-INF/maven/com.google.guava").exists(), "Expected to exist");
    assertFalse(new ClasspathFileSource("not/exist").exists(), "Expected not to exist");
  }

  @Test
  void failsSilentlyOnWrites() {
    assertDoesNotThrow(
        () -> {
          initForFileSystem();
          classpathFileSource.deleteFile("one");
          classpathFileSource.writeBinaryFile("any-bytes", new byte[] {});
          classpathFileSource.writeTextFile("any-text", "things");
          classpathFileSource.createIfNecessary();
        });
  }

  private void initForJar() {
    classpathFileSource = new ClasspathFileSource("META-INF/maven/com.google.guava");
  }

  private void initForFileSystem() {
    classpathFileSource = new ClasspathFileSource("filesource");
  }

  private void initForCustomClassLoader() throws MalformedURLException {
    URL[] urls = {new File("src/main/resources/classpath-filesource.jar").toURI().toURL()};
    ClassLoader cl = new URLClassLoader(urls);
    classpathFileSource = new ClasspathFileSource(cl, "jar-filesource");
  }

  private static TextFile findFileNamed(List<TextFile> files, String name) {
    return files.stream()
        .filter(file -> file.name().endsWith("/" + name))
        .findFirst()
        .orElseThrow();
  }
}
