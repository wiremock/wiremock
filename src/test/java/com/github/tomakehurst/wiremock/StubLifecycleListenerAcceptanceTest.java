/*
 * Copyright (C) 2019-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.NotPermittedException;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

public class StubLifecycleListenerAcceptanceTest {

  TestStubLifecycleListener loggingListener = new TestStubLifecycleListener();
  ExceptionThrowingStubLifecycleListener exceptionThrowingListener =
      new ExceptionThrowingStubLifecycleListener();

  @TempDir public static File tempDir;

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .withRootDirectory(tempDir.getAbsolutePath())
                  .extensions(loggingListener, exceptionThrowingListener))
          .build();

  @BeforeEach
  public void init() {
    loggingListener.events.clear();
    exceptionThrowingListener.throwException = false;
  }

  @Test
  public void beforeAndAfterMethodsAreCalledForStubCreation() {
    wm.stubFor(get("/test").withName("Created").willReturn(ok()));
    assertThat(loggingListener.events.get(0), is("beforeStubCreated, name: Created"));
    assertThat(loggingListener.events.get(1), is("afterStubCreated, name: Created"));
  }

  @Test
  public void beforeAndAfterMethodsAreCalledForStubEdit() {
    UUID id = UUID.randomUUID();
    wm.stubFor(get("/test").withId(id).withName("Created").willReturn(ok()));
    wm.editStub(get("/test").withId(id).withName("Edited").willReturn(ok()));
    assertThat(
        loggingListener.events.get(2), is("beforeStubEdited, old name: Created, new name: Edited"));
    assertThat(
        loggingListener.events.get(3), is("afterStubEdited, old name: Created, new name: Edited"));
  }

  @Test
  public void beforeAndAfterMethodsAreCalledForStubRemove() {
    StubMapping stub = wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
    wm.removeStub(stub);
    assertThat(loggingListener.events.get(2), is("beforeStubRemoved, name: To remove"));
    assertThat(loggingListener.events.get(3), is("afterStubRemoved, name: To remove"));
  }

  @Test
  public void beforeAndAfterMethodsAreCalledForStubsReset() {
    wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
    wm.resetMappings();
    assertThat(loggingListener.events.get(2), is("beforeStubsReset"));
    assertThat(loggingListener.events.get(3), is("afterStubsReset"));
  }

  @Test
  public void stubCreationCanBeVetoedWhenExceptionIsThrown() {
    exceptionThrowingListener.throwException = true;

    assertTrue(wm.listAllStubMappings().getMappings().isEmpty());

    try {
      wm.stubFor(get("/test").withName("Created").willReturn(ok()));
      fail("Expected an exception to be thrown");
    } catch (Exception e) {
      assertThat(e, Matchers.instanceOf(NotPermittedException.class));
    }

    assertTrue(wm.listAllStubMappings().getMappings().isEmpty());
  }

  @Test
  public void sensibleExceptionIsThrownWhenRemoteAndExceptionThrownFromListener() {
    WireMock wmRemote = new WireMock(wm.getPort());
    exceptionThrowingListener.throwException = true;

    try {
      wmRemote.register(get("/test").withName("Created").willReturn(ok()));
      fail("Expected an exception to be thrown");
    } catch (Exception e) {
      assertThat(e, Matchers.<Exception>instanceOf(NotPermittedException.class));
      assertThat(((NotPermittedException) e).getErrors().first().getTitle(), is("quota exhausted"));
    }
  }

  public static class TestStubLifecycleListener implements StubLifecycleListener {

    public List<String> events = new ArrayList<>();

    @Override
    public void beforeStubCreated(StubMapping stub) {
      events.add("beforeStubCreated, name: " + stub.getName());
    }

    @Override
    public void afterStubCreated(StubMapping stub) {
      events.add("afterStubCreated, name: " + stub.getName());
    }

    @Override
    public void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
      events.add(
          "beforeStubEdited, old name: " + oldStub.getName() + ", new name: " + newStub.getName());
    }

    @Override
    public void afterStubEdited(StubMapping oldStub, StubMapping newStub) {
      events.add(
          "afterStubEdited, old name: " + oldStub.getName() + ", new name: " + newStub.getName());
    }

    @Override
    public void beforeStubRemoved(StubMapping stub) {
      events.add("beforeStubRemoved, name: " + stub.getName());
    }

    @Override
    public void afterStubRemoved(StubMapping stub) {
      events.add("afterStubRemoved, name: " + stub.getName());
    }

    @Override
    public void beforeStubsReset() {
      events.add("beforeStubsReset");
    }

    @Override
    public void afterStubsReset() {
      events.add("afterStubsReset");
    }

    @Override
    public String getName() {
      return "test-stub-lifecycle-listener";
    }
  }

  public static class ExceptionThrowingStubLifecycleListener implements StubLifecycleListener {

    public boolean throwException = false;

    @Override
    public void beforeStubCreated(StubMapping stub) {
      throwIfRequired();
    }

    @Override
    public void afterStubCreated(StubMapping stub) {}

    @Override
    public void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
      throwIfRequired();
    }

    @Override
    public void afterStubEdited(StubMapping oldStub, StubMapping newStub) {}

    @Override
    public void beforeStubRemoved(StubMapping stub) {
      throwIfRequired();
    }

    @Override
    public void afterStubRemoved(StubMapping stub) {}

    @Override
    public void beforeStubsReset() {
      throwIfRequired();
    }

    @Override
    public void afterStubsReset() {}

    @Override
    public String getName() {
      return "exception-thrower";
    }

    private void throwIfRequired() {
      if (throwException) {
        throw new NotPermittedException(Errors.notPermitted("quota exhausted"));
      }
    }
  }
}
