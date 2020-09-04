package com.github.tomakehurst.wiremock.stubbing;

public abstract class PersistStubMappingsWrapper implements Iterable<StubMapping> {

		public abstract  void add(StubMapping mapping);

		public abstract boolean remove(final StubMapping mappingToRemove);

		public abstract boolean replace(StubMapping existingStubMapping, StubMapping newStubMapping);

		public abstract void clear();

}
