/*
 * Copyright (C) 2013-2023 Thomas Akehurst
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

public class RequestSpec {

  private final RequestMethod method;
  private final PathTemplate uriTemplate;

  public RequestSpec(RequestMethod method, String uriTemplate) {
    requireNonNull(method);
    requireNonNull(uriTemplate);
    this.method = method;
    this.uriTemplate = new PathTemplate(uriTemplate);
  }

  public static RequestSpec requestSpec(RequestMethod method, String path) {
    return new RequestSpec(method, path);
  }

  public RequestMethod method() {
    return method;
  }

  public PathTemplate getUriTemplate() {
    return uriTemplate;
  }

  public String path() {
    return path(PathParams.empty());
  }

  public String path(PathParams pathParams) {
    return uriTemplate.render(pathParams);
  }

  public boolean matches(RequestMethod method, String path) {
    return (this.method.equals(ANY) || this.method.equals(method)) && uriTemplate.matches(path);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RequestSpec that = (RequestSpec) o;

    if (!method.equals(that.method)) return false;
    return uriTemplate.equals(that.uriTemplate);
  }

  @Override
  public int hashCode() {
    int result = method.hashCode();
    result = 31 * result + uriTemplate.hashCode();
    return result;
  }
}
