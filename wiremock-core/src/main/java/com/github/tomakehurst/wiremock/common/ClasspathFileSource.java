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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getLoader;
import static java.util.Arrays.asList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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

      if (pathUri.getScheme().equals("file")) {
        rootDirectory = new File(pathUri);
      } else if (!isArchive(pathUri)) {
        throw new IllegalArgumentException(
            "ClasspathFileSource can't handle paths of type " + pathUri.getScheme());
      }

    } catch (Exception e) {
      throwUnchecked(e);
    }
  }

  private ClassLoader getClassLoader() {
    if (classLoader != null) return classLoader;
    return getLoader(ClasspathFileSource.class);
  }

  private boolean isFileSystem() {
    return rootDirectory != null;
  }

  @Override
  public BinaryFile getBinaryFileNamed(final String name) {
    if (isFileSystem()) {
      return new BinaryFile(new File(rootDirectory, name).toURI());
    }

    return getZipEntryUri(name)
        .map(BinaryFile::new)
        .orElseThrow(
            () -> new NotFoundException("File " + name + " not found on classpath in " + path));
  }

  @Override
  public TextFile getTextFileNamed(String name) {
    if (isFileSystem()) {
      return new TextFile(new File(rootDirectory, name).toURI());
    }

    return getZipEntryUri(name)
        .map(TextFile::new)
        .orElseThrow(
            () -> new NotFoundException("File " + name + " not found on classpath in " + path));
  }

  private Optional<URI> getZipEntryUri(final String name) {
    final String lookFor = path + "/" + name;
    try (ZipFile resourceZipFile = openZipFile(pathUri)) {
      final Enumeration<? extends ZipEntry> enumeration = resourceZipFile.entries();
      while (enumeration.hasMoreElements()) {
        final ZipEntry candidate = enumeration.nextElement();
        if (candidate.getName().equals(lookFor)) {
          return Optional.of(getUriFor(pathUri, candidate));
        }
      }

      return Optional.empty();
    } catch (Exception e) {
      return throwUnchecked(e, null);
    }
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
    try {
      Enumeration<URL> resources = getClassLoader().getResources(path);
      if (!resources.hasMoreElements()) {
        return listFilesRecursively(pathUri);
      }

      List<TextFile> files = new ArrayList<>();
      while (resources.hasMoreElements()) {
        files.addAll(listFilesRecursively(resources.nextElement().toURI()));
      }
      return files;
    } catch (Exception e) {
      return throwUnchecked(e, List.class);
    }
  }

  private List<TextFile> listFilesRecursively(URI resourceUri) throws Exception {
    if (resourceUri.getScheme().equals("file")) {
      File resourceRoot = new File(resourceUri);
      assertExistsAndIsDirectory(resourceRoot);
      List<File> fileList = new ArrayList<>();
      recursivelyAddFilesToList(resourceRoot, fileList);
      return toTextFileList(fileList);
    }

    if (!isArchive(resourceUri)) {
      throw new IllegalArgumentException(
          "ClasspathFileSource can't handle paths of type " + resourceUri.getScheme());
    }

    try (ZipFile resourceZipFile = openZipFile(resourceUri)) {
      return resourceZipFile.stream()
          .filter(jarEntry -> !jarEntry.isDirectory() && jarEntry.getName().startsWith(path))
          .map(jarEntry -> new TextFile(getUriFor(resourceUri, jarEntry)))
          .collect(Collectors.toList());
    }
  }

  private static boolean isArchive(URI resourceUri) {
    return asList("jar", "war", "ear", "zip").contains(resourceUri.getScheme());
  }

  private static ZipFile openZipFile(URI resourceUri) throws IOException {
    return new ZipFile(new File(getArchiveUri(resourceUri)));
  }

  private static URI getArchiveUri(URI resourceUri) {
    String schemeSpecificPart = resourceUri.getRawSchemeSpecificPart();
    int separatorIndex = schemeSpecificPart.indexOf("!/");
    if (separatorIndex < 0) {
      throw new IllegalArgumentException("Invalid archive URI: " + resourceUri);
    }

    return URI.create(schemeSpecificPart.substring(0, separatorIndex));
  }

  private static URI getUriFor(URI resourceUri, ZipEntry jarEntry) {
    try {
      String encodedEntryPath =
          new URI(null, null, "/" + jarEntry.getName(), null).getRawPath().substring(1);
      return URI.create(
          resourceUri.getScheme()
              + ":"
              + getArchiveUri(resourceUri).toASCIIString()
              + "!/"
              + encodedEntryPath);
    } catch (Exception e) {
      return throwUnchecked(e, URI.class);
    }
  }

  private void recursivelyAddFilesToList(File root, List<File> fileList) {
    File[] files = Optional.ofNullable(root.listFiles()).orElseGet(() -> new File[0]);
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
    return !isFileSystem() || rootDirectory.exists();
  }

  @Override
  public boolean fileExists(String name) {
    return false;
  }

  @Override
  public void deleteFile(String name) {}

  private void assertExistsAndIsDirectory(File directory) {
    if (directory.exists() && !directory.isDirectory()) {
      throw new IllegalStateException(directory + " is not a directory");
    } else if (!directory.exists()) {
      throw new IllegalStateException(directory + " does not exist");
    }
  }
}
