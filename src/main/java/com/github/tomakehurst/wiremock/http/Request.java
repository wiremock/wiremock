/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** The interface Request. */
public interface Request {

  /**
   * Gets id.
   *
   * @return the id
   */
  // This is populated by the serve event.
  @JsonIgnore
  default UUID getId() {
    return null;
  }

  /** The interface Part. */
  interface Part {
    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets file name.
     *
     * @return the file name
     */
    String getFileName();

    /**
     * Gets header.
     *
     * @param name the name
     * @return the header
     */
    HttpHeader getHeader(String name);

    /**
     * Gets headers.
     *
     * @return the headers
     */
    HttpHeaders getHeaders();

    /**
     * Gets body.
     *
     * @return the body
     */
    Body getBody();
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  String getUrl();

  /**
   * Gets absolute url.
   *
   * @return the absolute url
   */
  String getAbsoluteUrl();

  /**
   * Gets method.
   *
   * @return the method
   */
  RequestMethod getMethod();

  /**
   * Gets scheme.
   *
   * @return the scheme
   */
  String getScheme();

  /**
   * Gets host.
   *
   * @return the host
   */
  String getHost();

  /**
   * Gets port.
   *
   * @return the port
   */
  int getPort();

  /**
   * Gets client ip.
   *
   * @return the client ip
   */
  String getClientIp();

  /**
   * Gets header.
   *
   * @param key the key
   * @return the header
   */
  String getHeader(String key);

  /**
   * Header http header.
   *
   * @param key the key
   * @return the http header
   */
  HttpHeader header(String key);

  /**
   * Content type header content type header.
   *
   * @return the content type header
   */
  ContentTypeHeader contentTypeHeader();

  /**
   * Gets headers.
   *
   * @return the headers
   */
  HttpHeaders getHeaders();

  /**
   * Contains header boolean.
   *
   * @param key the key
   * @return the boolean
   */
  boolean containsHeader(String key);

  /**
   * Gets all header keys.
   *
   * @return the all header keys
   */
  Set<String> getAllHeaderKeys();

  /**
   * Gets path parameters.
   *
   * @return the path parameters
   */
  // These are calculated from other fields so should not be serialised
  @JsonIgnore
  default PathParams getPathParameters() {
    return PathParams.empty();
  }

  /**
   * Query parameter query parameter.
   *
   * @param key the key
   * @return the query parameter
   */
  QueryParameter queryParameter(String key);

  /**
   * Form parameter form parameter.
   *
   * @param key the key
   * @return the form parameter
   */
  FormParameter formParameter(String key);

  /**
   * Form parameters map.
   *
   * @return the map
   */
  Map<String, FormParameter> formParameters();

  /**
   * Gets cookies.
   *
   * @return the cookies
   */
  Map<String, Cookie> getCookies();

  /**
   * Get body byte [ ].
   *
   * @return the byte [ ]
   */
  byte[] getBody();

  /**
   * Gets body as string.
   *
   * @return the body as string
   */
  String getBodyAsString();

  /**
   * Gets body as base 64.
   *
   * @return the body as base 64
   */
  String getBodyAsBase64();

  /**
   * Is multipart boolean.
   *
   * @return the boolean
   */
  boolean isMultipart();

  /**
   * Gets parts.
   *
   * @return the parts
   */
  Collection<Part> getParts();

  /**
   * Gets part.
   *
   * @param name the name
   * @return the part
   */
  Part getPart(String name);

  /**
   * Is browser proxy request boolean.
   *
   * @return the boolean
   */
  boolean isBrowserProxyRequest();

  /**
   * Gets original request.
   *
   * @return the original request
   */
  Optional<Request> getOriginalRequest();

  /**
   * Gets protocol.
   *
   * @return the protocol
   */
  String getProtocol();
}
