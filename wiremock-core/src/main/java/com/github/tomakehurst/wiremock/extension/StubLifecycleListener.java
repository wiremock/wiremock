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

  default void beforeStubsAltered(List<StubMappingToAlter> stubs) {
    for (StubMappingToAlter alteredStub : stubs) {
      if (alteredStub instanceof StubMappingToCreate toCreate) {
        toCreate.setStub(beforeStubCreated(toCreate.getStub()));
      } else if (alteredStub instanceof StubMappingToEdit toEdit) {
        toEdit.setNewStub(beforeStubEdited(toEdit.getOldStub(), toEdit.getNewStub()));
      } else if (alteredStub instanceof StubMappingToRemove toRemove) {
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

  sealed interface StubMappingToAlter
      permits StubMappingToCreate, StubMappingToEdit, StubMappingToRemove {}

  non-sealed interface StubMappingToCreate extends StubMappingToAlter, CreatedStubMapping {
    void setStub(StubMapping stub);
  }

  non-sealed interface StubMappingToEdit extends StubMappingToAlter, EditedStubMapping {
    void setNewStub(StubMapping stub);
  }

  non-sealed interface StubMappingToRemove extends StubMappingToAlter, RemovedStubMapping {}
}
