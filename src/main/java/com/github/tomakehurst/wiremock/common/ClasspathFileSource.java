/*
 * Copyright (C) 2014-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClasspathFileSource implements FileSource {

  private final String path;
  private final ClassLoader classLoader;
  private URI pathUri;
  private ZipFile zipFile;
  private File rootDirectory;

  public ClasspathFileSource(String path) {
    this((ClassLoader) null, path);
  }

  public ClasspathFileSource(Class<?> classpath, String path) {
    this(classpath.getClassLoader(), path);
  }

  public ClasspathFileSource(ClassLoader classLoader, String path) {
    this.path = path;
    this.classLoader = classLoader;

    try {
      URL resource = getClassLoader().getResource(path);

      if (resource == null) {
        rootDirectory = new File(path);
        this.pathUri = rootDirectory.toURI();
        return;
      }

      this.pathUri = resource.toURI();

      if (asList("jar", "war", "ear", "zip").contains(pathUri.getScheme())) {
        String jarFileUri = pathUri.getSchemeSpecificPart().split("!")[0];
        String jarFilePath = jarFileUri.replace("file:", "");
        File file = new File(jarFilePath);
        zipFile = new ZipFile(file);
      } else if (pathUri.getScheme().equals("file")) {
        rootDirectory = new File(pathUri);
      } else {
        throw new IllegalArgumentException(
            "ClasspathFileSource can't handle paths of type " + pathUri.getScheme());
      }

    } catch (Exception e) {
      throwUnchecked(e);
    }
  }

  private ClassLoader getClassLoader() {
    if (classLoader != null) return classLoader;
    return getFirstNonNull(
        currentThread().getContextClassLoader(), Resources.class.getClassLoader());
  }

  private boolean isFileSystem() {
    return rootDirectory != null;
  }

  @Override
  public BinaryFile getBinaryFileNamed(final String name) {
    if (isFileSystem()) {
      return new BinaryFile(new File(rootDirectory, name).toURI());
    }

    return new BinaryFile(getZipEntryUri(name));
  }

  @Override
  public TextFile getTextFileNamed(String name) {
    if (isFileSystem()) {
      return new TextFile(new File(rootDirectory, name).toURI());
    }

    return new TextFile(getZipEntryUri(name));
  }

  private URI getZipEntryUri(final String name) {
    final String lookFor = path + "/" + name;
    final Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
    StringBuilder candidates = new StringBuilder();
    while (enumeration.hasMoreElements()) {
      final ZipEntry candidate = enumeration.nextElement();
      if (candidate.getName().equals(lookFor)) {
        return getUriFor(candidate);
      }
      candidates.append(candidate.getName()).append("\n");
    }
    throw new RuntimeException(
        "Was unable to find entry: \"" + lookFor + "\", found:\n" + candidates);
  }

  @Override
  public void createIfNecessary() {}

  @Override
  public FileSource child(String subDirectoryName) {
    return new ClasspathFileSource(classLoader, path + "/" + subDirectoryName);
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return pathUri;
  }

  @Override
  public List<TextFile> listFilesRecursively() {
    if (isFileSystem()) {
      assertExistsAndIsDirectory();
      List<File> fileList = new ArrayList<>();
      recursivelyAddFilesToList(rootDirectory, fileList);
      return toTextFileList(fileList);
    }

    return zipFile.stream()
        .filter(jarEntry -> !jarEntry.isDirectory() && jarEntry.getName().startsWith(path))
        .map(jarEntry -> new TextFile(getUriFor(jarEntry)))
        .collect(Collectors.toList());
  }

  private URI getUriFor(ZipEntry jarEntry) {
    try {
      return Resources.getResource(jarEntry.getName()).toURI();
    } catch (URISyntaxException e) {
      return throwUnchecked(e, URI.class);
    }
  }

  private void recursivelyAddFilesToList(File root, List<File> fileList) {
    File[] files = Optional.ofNullable(root.listFiles()).orElse(new File[0]);
    for (File file : files) {
      if (file.isDirectory()) {
        recursivelyAddFilesToList(file, fileList);
      } else {
        fileList.add(file);
      }
    }
  }

  private List<TextFile> toTextFileList(List<File> fileList) {
    return fileList.stream().map(input -> new TextFile(input.toURI())).collect(Collectors.toList());
  }

  @Override
  public void writeTextFile(String name, String contents) {}

  @Override
  public void writeBinaryFile(String name, byte[] contents) {}

  @Override
  public boolean exists() {
    // It'll only be non-file system if finding the classpath resource succeeded in the constructor
    return (isFileSystem() && rootDirectory.exists()) || (!isFileSystem());
  }

  @Override
  public void deleteFile(String name) {}

  private void assertExistsAndIsDirectory() {
    if (rootDirectory.exists() && !rootDirectory.isDirectory()) {
      throw new RuntimeException(rootDirectory + " is not a directory");
    } else if (!rootDirectory.exists()) {
      throw new RuntimeException(rootDirectory + " does not exist");
    }
  }
}
