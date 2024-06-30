/*
 * Copyright (C) 2012-2024 Thomas Akehurst
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
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.security.NotAuthorisedException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractFileSource implements FileSource {

  protected final File rootDirectory;

  protected AbstractFileSource(File rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  protected abstract boolean readOnly();

  @Override
  public BinaryFile getBinaryFileNamed(final String name) {
    assertFilePathIsUnderRoot(name);
    return new BinaryFile(new File(rootDirectory, name).toURI());
  }

  @Override
  public TextFile getTextFileNamed(String name) {
    assertFilePathIsUnderRoot(name);
    return new TextFile(new File(rootDirectory, name).toURI());
  }

  @Override
  public void createIfNecessary() {
    if (rootDirectory.isDirectory()) {
      return;
    }

    if (rootDirectory.exists() && rootDirectory.isFile()) {
      throw new IllegalStateException(rootDirectory + " already exists and is a file");
    } else if (!rootDirectory.exists() && !readOnly()) {
      rootDirectory.mkdirs();
    }
  }

  @Override
  public String getPath() {
    return rootDirectory.getPath();
  }

  @Override
  public URI getUri() {
    return rootDirectory.toURI();
  }

  @Override
  public List<TextFile> listFilesRecursively() {
    assertExistsAndIsDirectory();
    List<File> fileList = new ArrayList<>();
    recursivelyAddFilesToList(rootDirectory, fileList);
    return toTextFileList(fileList);
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
  public void writeTextFile(String name, String contents) {
    writeBinaryFile(name, contents.getBytes(UTF_8));
  }

  @Override
  public void writeBinaryFile(String name, byte[] contents) {
    createIfNecessary();
    writeBinaryFileAndTranslateExceptions(contents, writableFileFor(name));
  }

  @Override
  public void deleteFile(String name) {
    writableFileFor(name).delete();
  }

  @Override
  public boolean exists() {
    return rootDirectory.exists();
  }

  private File writableFileFor(String name) {
    assertExistsAndIsDirectory();
    assertFilePathIsUnderRoot(name);
    assertWritable();
    final File filePath = new File(name);

    if (filePath.isAbsolute()) {
      return filePath;
    } else {
      // Convert to absolute path
      return new File(rootDirectory, name);
    }
  }

  private void assertExistsAndIsDirectory() {
    if (rootDirectory.exists() && !rootDirectory.isDirectory()) {
      throw new RuntimeException(rootDirectory + " is not a directory");
    } else if (!rootDirectory.exists()) {
      throw new RuntimeException(rootDirectory + " does not exist");
    }
  }

  private void assertWritable() {
    if (readOnly()) {
      throw new UnsupportedOperationException("Can't write to read only file sources");
    }
  }

  private void assertFilePathIsUnderRoot(String path) {
    try {
      String rootPath = rootDirectory.getCanonicalPath();

      File file = new File(path);
      String filePath =
          file.isAbsolute()
              ? new File(path).getCanonicalPath()
              : new File(rootDirectory, path).getCanonicalPath();

      if (!Paths.get(filePath).normalize().startsWith(rootPath)) {
        throw new NotAuthorisedException(
            "Access to file "
                + path
                + " is not permitted. An absolute path from the filesystem root might be specified. "
                + "Your file must be in "
                + rootDirectory.getAbsolutePath());
      }
    } catch (IOException ioe) {
      throw new NotAuthorisedException("File " + path + " cannot be accessed", ioe);
    }
  }

  private void writeBinaryFileAndTranslateExceptions(byte[] contents, File toFile) {
    try {
      ensureDirectoryExists(toFile);
      Files.write(toFile.toPath(), contents);
    } catch (IOException ioe) {
      throwUnchecked(ioe);
    }
  }

  private void ensureDirectoryExists(File toFile) throws IOException {
    Path toPath = toFile.toPath();
    if (!java.nio.file.Files.exists(toPath)) {
      Path toParentPath = toPath.getParent();
      java.nio.file.Files.createDirectories(toParentPath);
    }
  }

  public static Predicate<BinaryFile> byFileExtension(final String extension) {
    return input -> input.name().endsWith("." + extension);
  }
}
