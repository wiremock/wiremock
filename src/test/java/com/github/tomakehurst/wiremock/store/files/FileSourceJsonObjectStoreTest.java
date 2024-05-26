/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileSourceJsonObjectStoreTest {

  @TempDir Path storeDir;

  FileSourceJsonObjectStore store;

  @BeforeEach
  void init() {
    store = new FileSourceJsonObjectStore(storeDir.toFile().getAbsolutePath());
  }

  @Test
  void writeString() throws Exception {
    store.put("the_key", "Store this");

    Path filePath = storeDir.resolve("the_key.json");
    assertThat(Files.exists(filePath), is(true));
    assertThat(Files.readString(filePath), jsonEquals("\"Store this\""));
  }

  @Test
  void writeObject() throws Exception {
    EqualToPattern equalToPattern =
        new EqualToPattern("correct", false); // No significance, just using this as an example
    store.put("the_key", equalToPattern);

    Path filePath = storeDir.resolve("the_key.json");
    assertThat(
        Files.readString(filePath),
        jsonEquals(
            "{\n" + "  \"equalTo\": \"correct\",\n" + "  \"caseInsensitive\": false\n" + "}"));
  }

  @Test
  void readString() throws Exception {
    Path filePath = storeDir.resolve("the_key.json");
    Files.write(filePath, "\"this text\"".getBytes());

    store
        .get("the_key")
        .ifPresentOrElse(
            value -> {
              assertThat(value, instanceOf(String.class));
              assertThat(value, is("this text"));
            },
            Assertions::fail);
  }

  @Test
  void readObject() throws Exception {
    Path filePath = storeDir.resolve("the_key.json");
    Files.write(
        filePath,
        ("{\n" + "  \"equalTo\": \"correct\",\n" + "  \"caseInsensitive\": false\n" + "}")
            .getBytes());

    store
        .get("the_key")
        .ifPresentOrElse(value -> assertThat(value, instanceOf(Map.class)), Assertions::fail);

    store
        .get("the_key", EqualToPattern.class)
        .ifPresentOrElse(
            value -> {
              assertThat(value, instanceOf(EqualToPattern.class));
              assertThat(value.getEqualTo(), is("correct"));
              assertThat(value.getCaseInsensitive(), is(false));
            },
            Assertions::fail);
  }

  @Test
  void readListOfStrings() throws Exception {
    Path filePath = storeDir.resolve("the_key.json");
    Files.write(filePath, "[\"one\", \"two\", \"three\"]".getBytes());

    store
        .get("the_key")
        .ifPresentOrElse(
            value -> {
              assertThat(value, instanceOf(List.class));
              assertThat(value, is(List.of("one", "two", "three")));
            },
            Assertions::fail);
  }

  @Test
  void nonExistentKey() {
    assertThat(store.get("does_not_exist").isPresent(), is(false));
  }

  @Test
  void computeValueConcurrently() throws InterruptedException {
    store.put("count", 1);

    ExecutorService executorService = Executors.newFixedThreadPool(50);
    for (int i = 0; i < 100; i++) {
      executorService.submit(() -> store.compute("count", (Integer current) -> current + 1));
    }
    executorService.shutdown();
    executorService.awaitTermination(5, SECONDS);

    assertThat(store.get("count").get(), is(101));
  }

  @Test
  void getAllKeysReturnsTheCorrectKeys() {
    store.put("the_key", "Store this");
    store.put("that_key", "Store that");

    assertThat(store.getAllKeys().count(), is(2L));
    assertThat(store.getAllKeys().anyMatch("the_key.json"::equals), is(true));
    assertThat(store.getAllKeys().anyMatch("that_key.json"::equals), is(true));
  }

  @Test
  void remove() {
    store.put("the_key", "Store this");
    store.put("that_key", "Store that");

    assertThat(store.getAllKeys().count(), is(2L));
    assertThat(store.getAllKeys().anyMatch("the_key.json"::equals), is(true));
    assertThat(store.getAllKeys().anyMatch("that_key.json"::equals), is(true));

    store.remove("the_key");

    assertThat(store.getAllKeys().count(), is(1L));
    assertThat(store.getAllKeys().anyMatch("the_key.json"::equals), is(false));
    assertThat(store.getAllKeys().anyMatch("that_key.json"::equals), is(true));
  }

  @Test
  void clear() {
    store.put("the_key", "Store this");
    store.put("that_key", "Store that");

    assertThat(store.getAllKeys().count(), is(2L));
    assertThat(store.getAllKeys().anyMatch("the_key.json"::equals), is(true));
    assertThat(store.getAllKeys().anyMatch("that_key.json"::equals), is(true));

    store.clear();

    assertThat(store.getAllKeys().count(), is(0L));
  }

  @Test
  void getPath() {
    assertThat(store.getPath(), is(storeDir.toFile().getAbsolutePath()));
  }
}
