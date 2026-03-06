/*
 * Copyright (C) 2019-2026 Thomas Akehurst
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.NotPermittedException;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener.AlteredStubMapping;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener.StubMappingToAlter;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.AbstractStubMappings.CreateStubMapping;
import com.github.tomakehurst.wiremock.stubbing.AbstractStubMappings.EditStubMapping;
import com.github.tomakehurst.wiremock.stubbing.AbstractStubMappings.RemoveStubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubImport.Options;
import com.github.tomakehurst.wiremock.stubbing.StubImport.Options.DuplicatePolicy;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

public class StubLifecycleListenerAcceptanceTest {

  TestStubLifecycleListener loggingListener = new TestStubLifecycleListener();
  ExceptionThrowingStubLifecycleListener exceptionThrowingListener =
      new ExceptionThrowingStubLifecycleListener();
  StubLifecycleListener mockListener = mock();

  @TempDir public static File tempDir;

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .withRootDirectory(tempDir.getAbsolutePath())
                  .extensions(loggingListener, exceptionThrowingListener, mockListener))
          .build();

  @BeforeEach
  public void init() {
    loggingListener.events.clear();
    exceptionThrowingListener.throwException = false;
    doCallRealMethod().when(mockListener).beforeStubCreated(any());
    doCallRealMethod().when(mockListener).beforeStubEdited(any(), any());
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

  @Test
  void insertionIndexIsSetOnStubsByTheTimeTheyArePassedToAfterStubCreated() {
    wm.stubFor(get("/1").willReturn(ok()));
    wm.stubFor(get("/2").willReturn(ok()));
    wm.stubFor(get("/3").willReturn(ok()));

    assertThat(loggingListener.afterCreatedStubs.get(0).getInsertionIndex(), is(0L));
    assertThat(loggingListener.afterCreatedStubs.get(1).getInsertionIndex(), is(1L));
    assertThat(loggingListener.afterCreatedStubs.get(2).getInsertionIndex(), is(2L));
  }

  @Test
  void defaultBatchMethodsCallIndividualStubMethods() {
    var existingStub1 = get("/1").withName("/1").willReturn(ok()).build();
    var existingStub2 = get("/2").withName("/2").willReturn(ok()).build();
    var existingStub3 = get("/3").withName("/3").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport()
            .stub(existingStub1)
            .stub(existingStub2)
            .stub(existingStub3)
            .build());
    loggingListener.events.clear();

    var stub1 =
        get("/1-updated")
            .withName("/1-updated")
            .withId(existingStub1.getId())
            .willReturn(ok())
            .build();
    var stub2 =
        get("/2-updated")
            .withName("/2-updated")
            .withId(existingStub2.getId())
            .willReturn(ok())
            .build();
    var stub3 = get("/3-new").withName("/3-new").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport()
            .stub(stub1)
            .stub(stub2)
            .stub(stub3)
            .deleteAllExistingStubsNotInImport()
            .build());

    assertThat(
        loggingListener.events,
        contains(
            "beforeStubCreated, name: /3-new",
            "beforeStubEdited, old name: /2, new name: /2-updated",
            "beforeStubEdited, old name: /1, new name: /1-updated",
            "beforeStubRemoved, name: /3",
            "afterStubCreated, name: /3-new",
            "afterStubEdited, old name: /2, new name: /2-updated",
            "afterStubEdited, old name: /1, new name: /1-updated",
            "afterStubRemoved, name: /3"));
  }

  @Test
  void batchMethodsAreCalledForImportOfNewStubs() {
    var stub1 = get("/1").willReturn(ok()).build();
    var stub2 = get("/2").willReturn(ok()).build();
    var stub3 = get("/3").willReturn(ok()).build();
    wm.importStubs(StubImport.stubImport().stub(stub1).stub(stub2).stub(stub3).build());

    verify(mockListener)
        .beforeStubsAltered(
            List.of(
                new CreateStubMapping(stub3),
                new CreateStubMapping(stub2),
                new CreateStubMapping(stub1)));
    verify(mockListener)
        .afterStubsAltered(
            List.of(
                new CreateStubMapping(stub3),
                new CreateStubMapping(stub2),
                new CreateStubMapping(stub1)));
  }

  @Test
  void batchMethodsAreCalledForImportsOfExistingAndNewStubs() {
    var existingStub1 = get("/1").willReturn(ok()).build();
    var existingStub2 = get("/2").willReturn(ok()).build();
    var existingStub3 = get("/3").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport()
            .stub(existingStub1)
            .stub(existingStub2)
            .stub(existingStub3)
            .build());
    clearInvocations(mockListener);

    var stub1 = get("/1-updated").withId(existingStub1.getId()).willReturn(ok()).build();
    var stub2 = get("/2-updated").withId(existingStub2.getId()).willReturn(ok()).build();
    var stub3 = get("/3-new").willReturn(ok()).build();
    wm.importStubs(StubImport.stubImport().stub(stub1).stub(stub2).stub(stub3).build());

    verify(mockListener)
        .beforeStubsAltered(
            List.of(
                new CreateStubMapping(stub3),
                new EditStubMapping(existingStub2, stub2),
                new EditStubMapping(existingStub1, stub1)));
    verify(mockListener)
        .afterStubsAltered(
            List.of(
                new CreateStubMapping(stub3),
                new EditStubMapping(existingStub2, stub2),
                new EditStubMapping(existingStub1, stub1)));
  }

  @Test
  void batchMethodsAreCalledForImportsOfExistingAndNewAndRemovedStubs() {
    var existingStub1 = get("/1").willReturn(ok()).build();
    var existingStub2 = get("/2").willReturn(ok()).build();
    var existingStub3 = get("/3").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport()
            .stub(existingStub1)
            .stub(existingStub2)
            .stub(existingStub3)
            .build());
    clearInvocations(mockListener);

    var stub1 = get("/1-updated").withId(existingStub1.getId()).willReturn(ok()).build();
    var stub2 = get("/2-updated").withId(existingStub2.getId()).willReturn(ok()).build();
    var stub3 = get("/3-new").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport()
            .stub(stub1)
            .stub(stub2)
            .stub(stub3)
            .deleteAllExistingStubsNotInImport()
            .build());

    verify(mockListener)
        .beforeStubsAltered(
            List.of(
                new CreateStubMapping(stub3),
                new EditStubMapping(existingStub2, stub2),
                new EditStubMapping(existingStub1, stub1),
                new RemoveStubMapping(existingStub3)));
    verify(mockListener)
        .afterStubsAltered(
            List.of(
                new CreateStubMapping(stub3),
                new EditStubMapping(existingStub2, stub2),
                new EditStubMapping(existingStub1, stub1),
                new RemoveStubMapping(existingStub3)));
  }

  @Test
  void batchMethodsAreNotCalledForIgnoredImportStubs() {
    var existingStub1 = get("/1").willReturn(ok()).build();
    var existingStub2 = get("/2").willReturn(ok()).build();
    var existingStub3 = get("/3").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport()
            .stub(existingStub1)
            .stub(existingStub2)
            .stub(existingStub3)
            .build());
    clearInvocations(mockListener);

    var stub1 = get("/1-updated").withId(existingStub1.getId()).willReturn(ok()).build();
    var stub2 = get("/2-updated").withId(existingStub2.getId()).willReturn(ok()).build();
    var stub3 = get("/3-new").willReturn(ok()).build();
    wm.importStubs(
        StubImport.stubImport().stub(stub1).stub(stub2).stub(stub3).ignoreExisting().build());

    verify(mockListener).beforeStubsAltered(List.of(new CreateStubMapping(stub3)));
    verify(mockListener).afterStubsAltered(List.of(new CreateStubMapping(stub3)));
  }

  @ParameterizedTest
  @CsvSource({"false", "true"})
  void alteredStubListCannotBeModifiedByListenersOnImport(boolean deleteExistingStubs) {
    var existingStub1 = wm.stubFor(get("/1").willReturn(ok()));
    wm.stubFor(get("/2").willReturn(ok()));

    var stub1 = get("/1-updated").withId(existingStub1.getId()).willReturn(ok()).build();
    var stub2 = get("/2-new").willReturn(ok()).build();
    wm.importStubs(
        new StubImport(
            List.of(stub1, stub2), new Options(DuplicatePolicy.OVERWRITE, deleteExistingStubs)));

    ArgumentCaptor<List<StubMappingToAlter>> beforeListCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<List<AlteredStubMapping>> afterListCaptor = ArgumentCaptor.captor();
    verify(mockListener).beforeStubsAltered(beforeListCaptor.capture());
    assertThrows(UnsupportedOperationException.class, () -> beforeListCaptor.getValue().remove(0));
    verify(mockListener).afterStubsAltered(afterListCaptor.capture());
    assertThrows(UnsupportedOperationException.class, () -> afterListCaptor.getValue().remove(0));
  }

  @Test
  void alteredStubListCannotBeModifiedByListenersOnRemoval() {
    var existingStub1 = wm.stubFor(get("/1").willReturn(ok()));
    var existingStub2 = wm.stubFor(get("/2").willReturn(ok()));

    wm.removeStubMappings(List.of(existingStub1, existingStub2));

    ArgumentCaptor<List<StubMappingToAlter>> beforeListCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<List<AlteredStubMapping>> afterListCaptor = ArgumentCaptor.captor();
    verify(mockListener).beforeStubsAltered(beforeListCaptor.capture());
    assertThrows(UnsupportedOperationException.class, () -> beforeListCaptor.getValue().remove(0));
    verify(mockListener).afterStubsAltered(afterListCaptor.capture());
    assertThrows(UnsupportedOperationException.class, () -> afterListCaptor.getValue().remove(0));
  }

  @Test
  void ignoredExistingStubsAreNotHandedToListenersWhenAllNonImportedStubsAreDeleted() {
    var existingStub1 = wm.stubFor(get("/1").willReturn(ok()));
    var existingStub2 = wm.stubFor(get("/2").willReturn(ok()));
    clearInvocations(mockListener);

    var stub1 = get("/1-updated").withId(existingStub1.getId()).willReturn(ok()).build();
    var stub2 = get("/2-new").willReturn(ok()).build();
    wm.importStubs(
        new StubImport(List.of(stub1, stub2), new Options(DuplicatePolicy.IGNORE, true)));

    verify(mockListener)
        .beforeStubsAltered(
            List.of(new CreateStubMapping(stub2), new RemoveStubMapping(existingStub2)));
    verify(mockListener)
        .afterStubsAltered(
            List.of(new CreateStubMapping(stub2), new RemoveStubMapping(existingStub2)));
  }

  @NullMarked
  public static class TestStubLifecycleListener implements StubLifecycleListener {

    public List<String> events = new ArrayList<>();
    public List<StubMapping> afterCreatedStubs = new ArrayList<>();

    @Override
    public StubMapping beforeStubCreated(StubMapping stub) {
      events.add("beforeStubCreated, name: " + stub.getName());
      return stub;
    }

    @Override
    public void afterStubCreated(StubMapping stub) {
      events.add("afterStubCreated, name: " + stub.getName());
      afterCreatedStubs.add(stub);
    }

    @Override
    public StubMapping beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
      events.add(
          "beforeStubEdited, old name: " + oldStub.getName() + ", new name: " + newStub.getName());
      return newStub;
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

  @NullMarked
  public static class ExceptionThrowingStubLifecycleListener implements StubLifecycleListener {

    public boolean throwException = false;

    @Override
    public StubMapping beforeStubCreated(StubMapping stub) {
      throwIfRequired();
      return stub;
    }

    @Override
    public void afterStubCreated(StubMapping stub) {}

    @Override
    public StubMapping beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
      throwIfRequired();
      return newStub;
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
