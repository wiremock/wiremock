package com.github.tomakehurst.wiremock.store.files;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FileSourceBlobStoreTest {

  @Test
  public void createFileSourceBlobStoreWithEmptyDirectory() {
    String directoryPath = "src/test/java/com/github/tomakehurst/wiremock/store/files/test/empty";
    FileSourceBlobStore fileSourceBlobStore = new FileSourceBlobStore(directoryPath);

    assertDoesNotThrow(() -> {
      fileSourceBlobStore.get("any-key");
    });
  }
}