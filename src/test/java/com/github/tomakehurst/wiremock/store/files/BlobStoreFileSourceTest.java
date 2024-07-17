/*
 * Copyright (C) 2022-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.store.BlobStore;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
  void list_all_files_returns_paths_relative_to_root_of_file_source() {
    List<TextFile> files = fileSource.listFilesRecursively();

    assertThat(
        files,
        hasExactlyIgnoringOrder(
            fileWithPath("one"),
            fileWithPath("two"),
            fileWithPath("three"),
            fileWithPath("subdir/four"),
            fileWithPath("subdir/five"),
            fileWithPath("anothersubdir/six"),
            fileWithPath("subdir/subsubdir/seven"),
            fileWithPath("subdir/subsubdir/eight"),
            fileWithPath("subdir/deepfile.json")));
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
        fileSource.getBinaryFileNamed("subdir/deepfile.json").getStream().readAllBytes();
    assertThat(actual, is(expected));
  }

  @Test
  void write_binary_file(@TempDir Path tempDir) throws Exception {
    BlobStore blobStore = new FileSourceBlobStore(tempDir.toString());
    fileSource = new BlobStoreFileSource(blobStore);

    byte[] contents = "{}".getBytes();
    fileSource.writeBinaryFile("folder/file.json", contents);

    byte[] actual = Files.readAllBytes(tempDir.resolve("folder/file.json"));
    assertThat(actual, is(contents));
  }

  @Test
  void write_text_file(@TempDir Path tempDir) throws Exception {
    BlobStore blobStore = new FileSourceBlobStore(tempDir.toString());
    fileSource = new BlobStoreFileSource(blobStore);

    String contents = "{}";
    fileSource.writeTextFile("folder/text-file.json", contents);

    String actual = new String(Files.readAllBytes(tempDir.resolve("folder/text-file.json")));
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
  void returns_file_path_when_backed_by_file_source() {
    assertThat(fileSource.getPath(), is(ROOT_PATH));
  }

  @Test
  void returns_empty_string_when_not_backed_by_file_source() {
    FileSource otherFileSource = new BlobStoreFileSource(new TestInMemoryBlobStore());
    assertThat(otherFileSource.getPath(), is(""));
  }

  static class TestInMemoryBlobStore implements BlobStore {

    @Override
    public Optional<InputStream> getStream(String key) {
      return Optional.empty();
    }

    @Override
    public InputStreamSource getStreamSource(String key) {
      return null;
    }

    @Override
    public Stream<String> getAllKeys() {
      return null;
    }

    @Override
    public Optional<byte[]> get(String key) {
      return Optional.empty();
    }

    @Override
    public void put(String key, byte[] content) {}

    @Override
    public void remove(String key) {}

    @Override
    public Optional<byte[]> getAndRemove(String key) {
      return Optional.empty();
    }

    @Override
    public void clear() {}
  }
}
