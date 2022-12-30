/*
 * Copyright (C) 2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store.files;

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.fileNamed;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BlobStoreFileSourceTest {

  static final String ROOT_PATH = filePath("filesource");

  BlobStoreFileSource fileSource;

  @BeforeEach
  void setup() {
    BlobStore blobStore = new FileSourceBlobStore(ROOT_PATH);
    fileSource = new BlobStoreFileSource(blobStore);
  }

  @SuppressWarnings("unchecked")
  @Test
  void list_all_files() {
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
  void get_single_file_bytes() {
    byte[] expected = "{}".getBytes();
    assertThat(fileSource.getBinaryFileNamed("subdir/deepfile.json").readContents(), is(expected));
  }

  @Test
  void get_single_stream() throws Exception {
    byte[] expected = "{}".getBytes();
    byte[] actual =
        ByteStreams.toByteArray(fileSource.getBinaryFileNamed("subdir/deepfile.json").getStream());
    assertThat(actual, is(expected));
  }

  @Test
  void write_binary_file(@TempDir Path tempDir) throws Exception {
    BlobStore blobStore = new FileSourceBlobStore(tempDir.toString());
    fileSource = new BlobStoreFileSource(blobStore);

    byte[] contents = "{}".getBytes();
    fileSource.writeBinaryFile("folder/file.json", contents);

    byte[] actual = Files.toByteArray(tempDir.resolve("folder/file.json").toFile());
    assertThat(actual, is(contents));
  }

  @Test
  void write_text_file(@TempDir Path tempDir) throws Exception {
    BlobStore blobStore = new FileSourceBlobStore(tempDir.toString());
    fileSource = new BlobStoreFileSource(blobStore);

    String contents = "{}";
    fileSource.writeTextFile("folder/text-file.json", contents);

    String actual =
        new String(Files.toByteArray(tempDir.resolve("folder/text-file.json").toFile()));
    assertThat(actual, is(contents));
  }

  @Test
  void delete_file(@TempDir Path tempDir) {
    BlobStore blobStore = new FileSourceBlobStore(tempDir.toString());
    fileSource = new BlobStoreFileSource(blobStore);

    String filePath = "folder/tmp-file.json";
    fileSource.writeTextFile(filePath, "{}");
    assertThat(tempDir.resolve(filePath).toFile().exists(), is(true));

    fileSource.deleteFile(filePath);
    assertThat(tempDir.resolve(filePath).toFile().exists(), is(false));
  }

  @Test
  void delete_all_files(@TempDir Path tempDir) {
    BlobStore blobStore = new FileSourceBlobStore(tempDir.toString());
    fileSource = new BlobStoreFileSource(blobStore);

    String filePath1 = "folder/tmp-file.json";
    String filePath2 = "root-tmp-file.json";
    fileSource.writeTextFile(filePath1, "{}");
    fileSource.writeTextFile(filePath2, "{}");

    assertThat(tempDir.resolve(filePath1).toFile().exists(), is(true));
    assertThat(tempDir.resolve(filePath2).toFile().exists(), is(true));

    blobStore.clear();
    assertThat(tempDir.resolve(filePath1).toFile().exists(), is(false));
    assertThat(tempDir.resolve(filePath2).toFile().exists(), is(false));
  }

  @Test
  void text_file_path() {
    //        fileSource.getPath()
  }
}
