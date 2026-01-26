/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
package org.wiremock.stringparser;

/**
 * A {@link StringParser} produces instances of {@link ParsedString}. The contract is that {@link
 * ParsedString#toString()} will return a String equal to the one passed to {@link
 * StringParser#parse(String)} to create it.
 *
 * <p>Serialization libraries can depend on this contract to serialize and deserialize subtypes of
 * {@link ParsedString}.
 */
public interface StringParser<T extends ParsedString> {
  Class<T> getType();

  T parse(String stringForm) throws ParseException;
}
