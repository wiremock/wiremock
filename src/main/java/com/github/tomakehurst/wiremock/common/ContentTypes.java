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

import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Strings.substringAfterLast;
import static com.github.tomakehurst.wiremock.common.TextType.JSON;
import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import java.net.URI;
import java.util.List;
import java.util.Map;

/** The type Content types. */
public class ContentTypes {

  private ContentTypes() {}

  /** The constant CONTENT_TYPE. */
  public static final String CONTENT_TYPE = "Content-Type";

  /** The constant CONTENT_LENGTH. */
  public static final String CONTENT_LENGTH = "Content-Length";

  /** The constant CONTENT_ENCODING. */
  public static final String CONTENT_ENCODING = "Content-Encoding";

  /** The constant TRANSFER_ENCODING. */
  public static final String TRANSFER_ENCODING = "Transfer-Encoding";

  /** The constant OCTET_STREAM. */
  public static final String OCTET_STREAM = "application/octet-stream";

  /** The constant LOCATION. */
  public static final String LOCATION = "Location";

  /** The constant AUTHORIZATION. */
  public static final String AUTHORIZATION = "Authorization";

  /** The constant ACCEPT. */
  public static final String ACCEPT = "Accept";

  /** The constant COOKIE. */
  public static final String COOKIE = "Cookie";

  /** The constant APPLICATION_JSON. */
  public static final String APPLICATION_JSON = "application/json";

  private static final Map<String, String> COMMON_MIME_TYPES =
      Map.ofEntries(
          Map.entry("image/jpeg", "jpeg"),
          Map.entry("image/gif", "gif"),
          Map.entry("image/tiff", "tiff"),
          Map.entry("image/png", "png"),
          Map.entry("image/x-icon", "ico"),
          Map.entry("image/svg+xml", "svg"),
          Map.entry("audio/x-aiff", "aiff"),
          Map.entry("video/x-ms-asf", "asf"),
          Map.entry("video/mpeg", "mp2"),
          Map.entry("audio/mpeg", "mp3"),
          Map.entry("video/quicktime", "mov"),
          Map.entry("application/pdf", "pdf"));

  /** The constant TEXT_FILE_EXTENSIONS. */
  public static final List<String> TEXT_FILE_EXTENSIONS =
      asList("txt", "json", "xml", "html", "htm", "yaml", "csv");

  /** The constant TEXT_MIME_TYPE_PATTERNS. */
  public static final List<String> TEXT_MIME_TYPE_PATTERNS =
      asList(
          ".*text.*",
          ".*json.*",
          ".*xml.*",
          ".*html.*",
          ".*yaml.*",
          ".*csv.*",
          ".*x-www-form-urlencoded.*");

  /**
   * Determine file extension string.
   *
   * @param url the url
   * @param contentTypeHeader the content type header
   * @param responseBody the response body
   * @return the string
   */
  public static String determineFileExtension(
      String url, ContentTypeHeader contentTypeHeader, byte[] responseBody) {
    if (contentTypeHeader.isPresent()) {
      if (contentTypeHeader.mimeTypePart().contains("json")) {
        return "json";
      }
      if (contentTypeHeader.mimeTypePart().contains("xml")) {
        return "xml";
      }
      if (contentTypeHeader.mimeTypePart().contains("text")) {
        return "txt";
      }

      String extension = COMMON_MIME_TYPES.get(contentTypeHeader.mimeTypePart());
      if (extension != null) {
        return extension;
      }
    }

    String path = URI.create(url).getPath();
    String lastPathSegment = substringAfterLast(path, "/");
    if (lastPathSegment.indexOf('.') != -1) {
      return substringAfterLast(lastPathSegment, ".");
    }

    return determineTextFileExtension(stringFromBytes(responseBody, contentTypeHeader.charset()));
  }

  /**
   * Determine text type text type.
   *
   * @param content the content
   * @return the text type
   */
  public static TextType determineTextType(String content) {
    try {
      Json.read(content, JsonNode.class);
      return JSON;
    } catch (Exception e) {
      try {
        Xml.read(content);
        return TextType.XML;
      } catch (Exception e1) {
        return TextType.PLAIN_TEXT;
      }
    }
  }

  /**
   * Determine text file extension string.
   *
   * @param content the content
   * @return the string
   */
  public static String determineTextFileExtension(String content) {
    TextType textType = determineTextType(content);
    switch (textType) {
      case JSON:
        return "json";
      case XML:
        return "xml";
      default:
        return "txt";
    }
  }

  /**
   * Determine is text from extension boolean.
   *
   * @param extension the extension
   * @return the boolean
   */
  public static boolean determineIsTextFromExtension(String extension) {
    return TEXT_FILE_EXTENSIONS.contains(extension);
  }

  /**
   * Determine is text from mime type boolean.
   *
   * @param mimeType the mime type
   * @return the boolean
   */
  public static boolean determineIsTextFromMimeType(final String mimeType) {
    return TEXT_MIME_TYPE_PATTERNS.stream()
        .anyMatch(pattern -> mimeType != null && mimeType.matches(pattern));
  }

  /**
   * Determine is text boolean.
   *
   * @param extension the extension
   * @param mimeType the mime type
   * @return the boolean
   */
  public static boolean determineIsText(String extension, String mimeType) {
    return determineIsTextFromExtension(extension) || determineIsTextFromMimeType(mimeType);
  }
}
