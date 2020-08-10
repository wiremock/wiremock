/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import com.google.common.base.Predicate;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Iterables.removeIf;

public abstract class PersistStubMappingsWrapper implements Iterable<StubMapping> {

		public abstract  void add(StubMapping mapping);

		public abstract boolean remove(final StubMapping mappingToRemove);

		public abstract boolean replace(StubMapping existingStubMapping, StubMapping newStubMapping);

		public abstract void clear();

}
