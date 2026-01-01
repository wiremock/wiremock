/*
 * Copyright (C) 2025 Thomas Akehurst
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
package org.wiremock.url;

/**
 * Represents a URL reference, which is a subset of URI references.
 *
 * <p>A UrlReference is either a {@link Url} or a {@link RelativeRef}. An {@link Url} is guaranteed
 * to resolve to an {@link Url} if resolved against a UrlReference, whereas it may resolve to an
 * {@link Urn} if resolved against an {@link UriReference}.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see UriReference
 * @see Url
 * @see RelativeRef
 */
public sealed interface UrlReference extends UriReference permits RelativeRef, Url {}
