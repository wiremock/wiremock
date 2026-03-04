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
package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StubLifecycleListener extends Extension {

  default StubMapping beforeStubCreated(StubMapping stub) {
    return stub;
  }

  default void afterStubCreated(StubMapping stub) {}

  default StubMapping beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
    return newStub;
  }

  default void afterStubEdited(StubMapping oldStub, StubMapping newStub) {}

  default void beforeStubRemoved(StubMapping stub) {}

  default void afterStubRemoved(StubMapping stub) {}

  default void beforeStubsReset() {}

  default void afterStubsReset() {}

  default void beforeStubsAltered(List<ToAlterStubMapping> stubs) {
    for (ToAlterStubMapping alteredStub : stubs) {
      if (alteredStub instanceof ToCreateStubMapping toCreate) {
        toCreate.setStub(beforeStubCreated(toCreate.getStub()));
      } else if (alteredStub instanceof ToEditStubMapping toEdit) {
        toEdit.setNewStub(beforeStubEdited(toEdit.getOldStub(), toEdit.getNewStub()));
      } else if (alteredStub instanceof ToRemoveStubMapping toRemove) {
        beforeStubRemoved(toRemove.getStub());
      }
    }
  }

  default void afterStubsAltered(List<AlteredStubMapping> stubs) {
    for (AlteredStubMapping alteredStub : stubs) {
      if (alteredStub instanceof CreatedStubMapping created) {
        afterStubCreated(created.getStub());
      } else if (alteredStub instanceof EditedStubMapping edited) {
        afterStubEdited(edited.getOldStub(), edited.getNewStub());
      } else if (alteredStub instanceof RemovedStubMapping removed) {
        afterStubRemoved(removed.getStub());
      }
    }
  }

  sealed interface AlteredStubMapping
      permits CreatedStubMapping, EditedStubMapping, RemovedStubMapping {}

  non-sealed interface CreatedStubMapping extends AlteredStubMapping {
    StubMapping getStub();
  }

  non-sealed interface EditedStubMapping extends AlteredStubMapping {
    StubMapping getOldStub();

    StubMapping getNewStub();
  }

  non-sealed interface RemovedStubMapping extends AlteredStubMapping {
    StubMapping getStub();
  }

  sealed interface ToAlterStubMapping
      permits ToCreateStubMapping, ToEditStubMapping, ToRemoveStubMapping {}

  non-sealed interface ToCreateStubMapping extends ToAlterStubMapping, CreatedStubMapping {
    void setStub(StubMapping stub);
  }

  non-sealed interface ToEditStubMapping extends ToAlterStubMapping, EditedStubMapping {
    void setNewStub(StubMapping stub);
  }

  non-sealed interface ToRemoveStubMapping extends ToAlterStubMapping, RemovedStubMapping {}

  final class CreateStubMapping implements StubLifecycleListener.ToCreateStubMapping {
    private StubMapping stub;

    public CreateStubMapping(StubMapping stub) {
      this.stub = stub;
    }

    @Override
    public StubMapping getStub() {
      return stub;
    }

    @Override
    public void setStub(StubMapping stub) {
      this.stub = stub;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof CreateStubMapping that)) return false;
      return Objects.equals(stub, that.stub);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(stub);
    }

    @Override
    public String toString() {
      return "CreateStubMapping{" + "stub=" + stub + '}';
    }
  }

  final class EditStubMapping implements StubLifecycleListener.ToEditStubMapping {
    private final StubMapping oldStub;
    private StubMapping newStub;

    public EditStubMapping(StubMapping oldStub, StubMapping newStub) {
      this.oldStub = oldStub;
      this.newStub = newStub;
    }

    @Override
    public StubMapping getOldStub() {
      return oldStub;
    }

    @Override
    public StubMapping getNewStub() {
      return newStub;
    }

    @Override
    public void setNewStub(StubMapping newStub) {
      this.newStub = newStub;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof EditStubMapping that)) return false;
      return Objects.equals(oldStub, that.oldStub) && Objects.equals(newStub, that.newStub);
    }

    @Override
    public int hashCode() {
      return Objects.hash(oldStub, newStub);
    }

    @Override
    public String toString() {
      return "EditStubMapping{" + "oldStub=" + oldStub + ", newStub=" + newStub + '}';
    }
  }

  @SuppressWarnings("ClassCanBeRecord")
  final class RemoveStubMapping implements StubLifecycleListener.ToRemoveStubMapping {
    private final StubMapping stub;

    public RemoveStubMapping(StubMapping stub) {
      this.stub = stub;
    }

    @Override
    public StubMapping getStub() {
      return stub;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof RemoveStubMapping that)) return false;
      return Objects.equals(stub, that.stub);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(stub);
    }

    @Override
    public String toString() {
      return "RemoveStubMapping{" + "stub=" + stub + '}';
    }
  }
}
