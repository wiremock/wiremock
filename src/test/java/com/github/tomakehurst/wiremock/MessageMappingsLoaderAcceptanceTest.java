/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageMappingsLoaderAcceptanceTest {

  private WireMockConfiguration configuration;
  private WireMockServer wireMockServer;

  @BeforeEach
  public void init() {
    configuration = wireMockConfig().dynamicPort();
  }

  @AfterEach
  public void stopWireMock() {
    if (wireMockServer != null && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
  }

  private void buildWireMock(Options options) {
    wireMockServer = new WireMockServer(options);
    wireMockServer.start();
  }

  @Test
  public void messageMappingsLoadedFromJsonFiles() {
    buildWireMock(configuration);
    wireMockServer.loadMessageMappingsUsing(
        new JsonFileMappingsSource(
            new SingleRootFileSource(filePath("test-requests")),
            new SingleRootFileSource(filePath("message-stub-test")),
            new FilenameMaker()));

    List<MessageStubMapping> stubs = wireMockServer.getMessageStubMappingsList();

    assertThat(stubs.size(), is(4));
    assertThat(stubs, hasItem(messageStubMappingWithName("Single message stub")));
    assertThat(stubs, hasItem(messageStubMappingWithName("Multi message stub 1")));
    assertThat(stubs, hasItem(messageStubMappingWithName("Multi message stub 2")));
    assertThat(stubs, hasItem(messageStubMappingWithName("Multi message stub 3")));

    assertThat(
        stubs,
        hasItem(messageStubMappingWithId(UUID.fromString("22222222-2222-2222-2222-222222222222"))));

    MessageStubMapping multiStub2 =
        stubs.stream()
            .filter(stub -> stub.getName().equals("Multi message stub 2"))
            .findFirst()
            .get();
    assertThat(multiStub2.getId(), is(notNullValue()));
  }

  @Test
  public void loadsMessageStubMappingsFromAMixtureOfSingleAndMultiStubFiles() {
    buildWireMock(configuration);
    wireMockServer.resetMessageStubMappings();
    wireMockServer.loadMessageMappingsUsing(
        new JsonFileMappingsSource(
            new SingleRootFileSource(filePath("test-requests")),
            new SingleRootFileSource(filePath("message-stub-test")),
            new FilenameMaker()));

    List<MessageStubMapping> stubs = wireMockServer.getMessageStubMappingsList();

    assertThat(stubs.size(), is(4));
    assertThat(
        stubs,
        hasItem(messageStubMappingWithId(UUID.fromString("11111111-1111-1111-1111-111111111111"))));
    assertThat(
        stubs,
        hasItem(messageStubMappingWithId(UUID.fromString("22222222-2222-2222-2222-222222222222"))));
    assertThat(
        stubs,
        hasItem(messageStubMappingWithId(UUID.fromString("33333333-3333-3333-3333-333333333333"))));
    assertThat(
        stubs,
        hasItem(messageStubMappingWithId(UUID.fromString("44444444-4444-4444-4444-444444444444"))));
  }

  @Test
  public void noMessageMappingsLoadedWhenDirectoryDoesNotExist() {
    buildWireMock(configuration);
    wireMockServer.resetMessageStubMappings();
    wireMockServer.loadMessageMappingsUsing(
        new JsonFileMappingsSource(
            new SingleRootFileSource(filePath("test-requests")), new FilenameMaker()));

    List<MessageStubMapping> stubs = wireMockServer.getMessageStubMappingsList();

    assertThat(stubs.size(), is(0));
  }

  @Test
  public void messageMappingsLoadedFromClasspath() {
    buildWireMock(configuration.usingFilesUnderClasspath("classpath-filesource"));

    List<MessageStubMapping> stubs = wireMockServer.getMessageStubMappingsList();

    assertThat(stubs.size(), is(1));
    assertThat(stubs, hasItem(messageStubMappingWithName("Classpath message stub")));
    assertThat(
        stubs,
        hasItem(messageStubMappingWithId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))));
  }

  private static Matcher<MessageStubMapping> messageStubMappingWithName(final String name) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("a message stub mapping with name ").appendValue(name);
      }

      @Override
      protected boolean matchesSafely(MessageStubMapping item, Description mismatchDescription) {
        if (name.equals(item.getName())) {
          return true;
        }
        mismatchDescription.appendText("name was ").appendValue(item.getName());
        return false;
      }
    };
  }

  private static Matcher<MessageStubMapping> messageStubMappingWithId(final UUID id) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("a message stub mapping with id ").appendValue(id);
      }

      @Override
      protected boolean matchesSafely(MessageStubMapping item, Description mismatchDescription) {
        if (id.equals(item.getId())) {
          return true;
        }
        mismatchDescription.appendText("id was ").appendValue(item.getId());
        return false;
      }
    };
  }
}
