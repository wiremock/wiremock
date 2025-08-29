/*
 * Copyright (C) 2015-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import java.util.Arrays;
import java.util.Objects;

/** The type Body. */
public class Body {

  private final byte[] content;
  private final boolean binary;
  private final boolean json;

  /**
   * Instantiates a new Body.
   *
   * @param content the content
   */
  public Body(byte[] content) {
    this(content, true);
  }

  /**
   * Instantiates a new Body.
   *
   * @param content the content
   */
  public Body(String content) {
    this.content = Strings.bytesFromString(content);
    binary = false;
    json = false;
  }

  private Body(byte[] content, boolean binary) {
    this.content = content;
    this.binary = binary;
    json = false;
  }

  private Body(byte[] content, boolean binary, boolean json) {
    this.content = content;
    this.binary = binary;
    this.json = json;
  }

  private Body(JsonNode content) {
    this.content = Json.toByteArray(content);
    binary = false;
    json = true;
  }

  /**
   * From bytes body.
   *
   * @param bytes the bytes
   * @return the body
   */
  static Body fromBytes(byte[] bytes) {
    return bytes != null ? new Body(bytes) : none();
  }

  /**
   * From json bytes body.
   *
   * @param bytes the bytes
   * @return the body
   */
  public static Body fromJsonBytes(byte[] bytes) {
    return bytes != null ? new Body(bytes, false, true) : none();
  }

  /**
   * From string body.
   *
   * @param str the str
   * @return the body
   */
  static Body fromString(String str) {
    return str != null ? new Body(str) : none();
  }

  /**
   * Of binary or text body.
   *
   * @param content the content
   * @param contentTypeHeader the content type header
   * @return the body
   */
  public static Body ofBinaryOrText(byte[] content, ContentTypeHeader contentTypeHeader) {
    return new Body(
        content, !ContentTypes.determineIsTextFromMimeType(contentTypeHeader.mimeTypePart()));
  }

  /**
   * From one of body.
   *
   * @param bytes the bytes
   * @param str the str
   * @param json the json
   * @param base64 the base 64
   * @return the body
   */
  public static Body fromOneOf(byte[] bytes, String str, JsonNode json, String base64) {
    if (bytes != null) return new Body(bytes);
    if (str != null) return new Body(str);
    if (json != null && !(json instanceof NullNode)) return new Body(json);
    if (base64 != null) return new Body(decodeBase64(base64), true);

    return none();
  }

  private static final Body EMPTY_BODY = new Body((byte[]) null);

  /**
   * None body.
   *
   * @return the body
   */
  public static Body none() {
    return EMPTY_BODY;
  }

  /**
   * As string string.
   *
   * @return the string
   */
  public String asString() {
    return content != null ? stringFromBytes(content) : null;
  }

  /**
   * As bytes byte [ ].
   *
   * @return the byte [ ]
   */
  public byte[] asBytes() {
    return content != null ? content : null;
  }

  /**
   * As base 64 string.
   *
   * @return the string
   */
  public String asBase64() {
    return encodeBase64(content);
  }

  /**
   * Is binary boolean.
   *
   * @return the boolean
   */
  public boolean isBinary() {
    return binary;
  }

  /**
   * As json json node.
   *
   * @return the json node
   */
  public JsonNode asJson() {
    return Json.node(asString());
  }

  /**
   * Is json boolean.
   *
   * @return the boolean
   */
  public boolean isJson() {
    return json;
  }

  /**
   * Is absent boolean.
   *
   * @return the boolean
   */
  public boolean isAbsent() {
    return content == null;
  }

  /**
   * Is present boolean.
   *
   * @return the boolean
   */
  public boolean isPresent() {
    return !isAbsent();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Body body = (Body) o;
    return Objects.equals(binary, body.binary) && Arrays.equals(content, body.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(content), binary);
  }

  @Override
  public String toString() {
    return "Body {" + "content=" + asString() + ", binary=" + binary + ", json=" + json + '}';
  }
}
