/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import java.net.URI;
import java.util.List;

/** The interface File source. */
public interface FileSource {

  /**
   * Gets binary file named.
   *
   * @param name the name
   * @return the binary file named
   */
  BinaryFile getBinaryFileNamed(String name);

  /**
   * Gets text file named.
   *
   * @param name the name
   * @return the text file named
   */
  TextFile getTextFileNamed(String name);

  /** Create if necessary. */
  void createIfNecessary();

  /**
   * Child file source.
   *
   * @param subDirectoryName the sub directory name
   * @return the file source
   */
  FileSource child(String subDirectoryName);

  /**
   * Gets path.
   *
   * @return the path
   */
  String getPath();

  /**
   * Gets uri.
   *
   * @return the uri
   */
  URI getUri();

  /**
   * List files recursively list.
   *
   * @return the list
   */
  List<TextFile> listFilesRecursively();

  /**
   * Write text file.
   *
   * @param name the name
   * @param contents the contents
   */
  void writeTextFile(String name, String contents);

  /**
   * Write binary file.
   *
   * @param name the name
   * @param contents the contents
   */
  void writeBinaryFile(String name, byte[] contents);

  /**
   * Exists boolean.
   *
   * @return the boolean
   */
  boolean exists();

  /**
   * Delete file.
   *
   * @param name the name
   */
  void deleteFile(String name);
}
