/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The type Errors. */
public class Errors {

  private final List<Error> errors;

  /**
   * Instantiates a new Errors.
   *
   * @param errors the errors
   */
  public Errors(@JsonProperty("errors") List<Error> errors) {
    this.errors = errors;
  }

  /**
   * Single errors.
   *
   * @param code the code
   * @param sourcePointer the source pointer
   * @param title the title
   * @return the errors
   */
  public static Errors single(Integer code, String sourcePointer, String title) {
    return new Errors(singletonList(new Error(code, new Error.Source(sourcePointer), title)));
  }

  /**
   * Single errors.
   *
   * @param code the code
   * @param sourcePointer the source pointer
   * @param title the title
   * @param detail the detail
   * @return the errors
   */
  public static Errors single(Integer code, String sourcePointer, String title, String detail) {
    return new Errors(
        singletonList(new Error(code, new Error.Source(sourcePointer), title, detail)));
  }

  /**
   * Single errors.
   *
   * @param code the code
   * @param title the title
   * @return the errors
   */
  public static Errors single(Integer code, String title) {
    return new Errors(singletonList(new Error(code, title)));
  }

  /**
   * Single with detail errors.
   *
   * @param code the code
   * @param title the title
   * @param detail the detail
   * @return the errors
   */
  public static Errors singleWithDetail(Integer code, String title, String detail) {
    return new Errors(singletonList(new Error(code, null, title, detail)));
  }

  /**
   * Not recording errors.
   *
   * @return the errors
   */
  public static Errors notRecording() {
    return single(30, "Not currently recording.");
  }

  /**
   * Validation errors.
   *
   * @param pointer the pointer
   * @param message the message
   * @return the errors
   */
  public static Errors validation(String pointer, String message) {
    return single(10, pointer, message);
  }

  /**
   * Not permitted errors.
   *
   * @param reason the reason
   * @return the errors
   */
  public static Errors notPermitted(String reason) {
    return single(50, reason);
  }

  /**
   * First error.
   *
   * @return the error
   */
  public Error first() {
    if (errors.isEmpty()) {
      throw new IllegalStateException("No errors are present");
    }

    return errors.get(0);
  }

  /**
   * Gets errors.
   *
   * @return the errors
   */
  public List<Error> getErrors() {
    return errors;
  }

  /** The type Error. */
  public static class Error {

    private final Integer code;
    private final Source source;
    private final String title;
    private final String detail;

    /**
     * Instantiates a new Error.
     *
     * @param code the code
     * @param source the source
     * @param title the title
     * @param detail the detail
     */
    public Error(
        @JsonProperty("code") Integer code,
        @JsonProperty("source") Source source,
        @JsonProperty("title") String title,
        @JsonProperty("detail") String detail) {
      this.code = code;
      this.source = source;
      this.title = title;
      this.detail = detail;
    }

    /**
     * Instantiates a new Error.
     *
     * @param code the code
     * @param source the source
     * @param title the title
     */
    public Error(int code, Source source, String title) {
      this(code, source, title, null);
    }

    /**
     * Instantiates a new Error.
     *
     * @param code the code
     * @param title the title
     */
    public Error(int code, String title) {
      this(code, null, title, null);
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public Integer getCode() {
      return code;
    }

    /**
     * Gets source.
     *
     * @return the source
     */
    public Source getSource() {
      return source;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
      return title;
    }

    /**
     * Gets detail.
     *
     * @return the detail
     */
    public String getDetail() {
      return detail;
    }

    /** The type Source. */
    public static class Source {

      private final String pointer;

      /**
       * Instantiates a new Source.
       *
       * @param pointer the pointer
       */
      public Source(@JsonProperty("pointer") String pointer) {
        this.pointer = pointer;
      }

      /**
       * Gets pointer.
       *
       * @return the pointer
       */
      public String getPointer() {
        return pointer;
      }
    }
  }
}
