/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The type List or single.
 *
 * @param <T> the type parameter
 */
@JsonSerialize(using = ListOrSingleSerialiser.class)
@JsonDeserialize(using = ListOrStringDeserialiser.class)
public class ListOrSingle<T> extends ArrayList<T> {

  /**
   * Instantiates a new List or single.
   *
   * @param c the c
   */
  public ListOrSingle(Collection<? extends T> c) {
    super(c);
  }

  /**
   * Instantiates a new List or single.
   *
   * @param items the items
   */
  public ListOrSingle(T... items) {
    this(asList(items));
  }

  @Override
  public String toString() {
    return size() > 0 ? get(0).toString() : "";
  }

  /**
   * Of list or single.
   *
   * @param <T> the type parameter
   * @param items the items
   * @return the list or single
   */
  public static <T> ListOrSingle<T> of(T... items) {
    return new ListOrSingle<>(items);
  }

  /**
   * Of list or single.
   *
   * @param <T> the type parameter
   * @param items the items
   * @return the list or single
   */
  public static <T> ListOrSingle<T> of(List<T> items) {
    return new ListOrSingle<>(items);
  }

  /**
   * Gets first.
   *
   * @return the first
   */
  public T getFirst() {
    return get(0);
  }

  /**
   * Gets last.
   *
   * @return the last
   */
  public T getLast() {
    return get(size() - 1);
  }

  @Override
  public T get(int index) {
    if (index < 0) {
      index = size() - 1 + index;
    }

    return super.get(index);
  }

  /**
   * Is single boolean.
   *
   * @return the boolean
   */
  public boolean isSingle() {
    return size() == 1;
  }
}
