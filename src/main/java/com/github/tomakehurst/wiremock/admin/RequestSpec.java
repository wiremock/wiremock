/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static java.util.Objects.requireNonNull;

import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.http.RequestMethod;

/**
 * A specification for an admin API request, defined by an HTTP method and a URL path template.
 *
 * <p>This class is used to map incoming admin requests to their corresponding {@link AdminTask}. It
 * supports path templates with placeholders (e.g., "/mappings/{id}") and wildcard matching for the
 * HTTP method.
 *
 * @see PathTemplate
 * @see RequestMethod
 */
public class RequestSpec {

  private final RequestMethod method;
  private final PathTemplate uriTemplate;

  /**
   * Constructs a new RequestSpec.
   *
   * @param method The HTTP request method.
   * @param uriTemplate The URL path template.
   */
  public RequestSpec(RequestMethod method, String uriTemplate) {
    requireNonNull(method);
    requireNonNull(uriTemplate);
    this.method = method;
    this.uriTemplate = new PathTemplate(uriTemplate);
  }

  /**
   * A convenience factory for creating a new RequestSpec.
   *
   * @param method The HTTP request method.
   * @param path The URL path template.
   * @return A new {@code RequestSpec} instance.
   */
  public static RequestSpec requestSpec(RequestMethod method, String path) {
    return new RequestSpec(method, path);
  }

  /**
   * Gets the HTTP request method.
   *
   * @return The HTTP request method.
   */
  public RequestMethod method() {
    return method;
  }

  public PathTemplate getUriTemplate() {
    return uriTemplate;
  }

  /**
   * Renders the path template with no parameters.
   *
   * @return The rendered path string.
   */
  public String path() {
    return path(PathParams.empty());
  }

  /**
   * Renders the path template with the given path parameters.
   *
   * @param pathParams The parameters to substitute into the template.
   * @return The rendered path string.
   */
  public String path(PathParams pathParams) {
    return uriTemplate.render(pathParams);
  }

  /**
   * Checks if a given request's method and path match this specification.
   *
   * @param method The method of the request to match.
   * @param path The path of the request to match.
   * @return true if the method and path match this specification, false otherwise.
   */
  public boolean matches(RequestMethod method, String path) {
    return (this.method.equals(ANY) || this.method.equals(method)) && uriTemplate.matches(path);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RequestSpec that = (RequestSpec) o;

    if (!method.equals(that.method)) {
      return false;
    }
    return uriTemplate.equals(that.uriTemplate);
  }

  @Override
  public int hashCode() {
    int result = method.hashCode();
    result = 31 * result + uriTemplate.hashCode();
    return result;
  }
}
